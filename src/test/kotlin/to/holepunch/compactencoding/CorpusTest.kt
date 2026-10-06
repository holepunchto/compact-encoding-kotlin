package to.holepunch.compactencoding

import java.io.File
import java.math.BigInteger
import kotlin.test.assertEquals
import kotlin.test.fail
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Assumptions.abort
import org.junit.jupiter.api.DynamicContainer.dynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory

private val fixtures = File(System.getProperty("corpus.fixtures"))

private fun read(path: String): JsonElement =
  Json.parseToJsonElement(File(fixtures, path).readText())

private val unsupported =
  setOf(
    "any",
    "array-uint",
    "arraybuffer",
    "ascii",
    "base64",
    "bigint",
    "bigint64",
    "bigint64array",
    "biguint",
    "biguint64",
    "biguint64array",
    "binary",
    "bitarray",
    "bool",
    "buffer",
    "date",
    "fixed-16",
    "fixed-24",
    "fixed-3",
    "fixed-32",
    "fixed-64",
    "fixed-8",
    "float32",
    "float32array",
    "float64",
    "float64array",
    "frame-uint",
    "hex",
    "int",
    "int16",
    "int16array",
    "int16be",
    "int24",
    "int32",
    "int32array",
    "int32be",
    "int40",
    "int48",
    "int56",
    "int64",
    "int64be",
    "int8",
    "int8array",
    "intbe",
    "ip",
    "ipAddress",
    "ipv4",
    "ipv4Address",
    "ipv6",
    "ipv6Address",
    "json",
    "lexint",
    "ndjson",
    "none",
    "optionalBuffer",
    "port",
    "raw",
    "record-utf8-uint",
    "string",
    "stringRecord",
    "ucs2",
    "uint16array",
    "uint16be",
    "uint24",
    "uint32",
    "uint32array",
    "uint32be",
    "uint40",
    "uint48",
    "uint56",
    "uint64",
    "uint64be",
    "uint8",
    "uint8array",
    "uintbe",
    "utf16le",
    "utf8",
  )

private class Adapter<T>(
  val codec: Codec<T>,
  val value: (JsonElement) -> T?,
  val show: (T) -> String,
)

private fun integer(element: JsonElement): BigInteger? =
  element.jsonPrimitive.content.removeSuffix("n").toBigIntegerOrNull()

private fun unsigned(element: JsonElement, bits: Int): ULong? =
  integer(element)?.takeIf { it.signum() >= 0 && it.bitLength() <= bits }?.toString()?.toULong()

private val adapters: Map<String, Adapter<*>> =
  mapOf(
    "uint" to Adapter(uint, { unsigned(it, 64) }, { it.toString() }),
    "uint16" to Adapter(uint16, { unsigned(it, 16)?.toUShort() }, { it.toString() }),
  )

private fun ByteArray.hex(): String =
  joinToString("") { (it.toInt() and 0xff).toString(16).padStart(2, '0') }

private fun bytes(hex: String): ByteArray =
  ByteArray(hex.length / 2) { hex.substring(it * 2, it * 2 + 2).toInt(16).toByte() }

private fun expected(answer: JsonObject): Map<String, String> = answer.mapValues { (key, v) ->
  if (key == "decodes") integer(v)?.toString() ?: v.jsonPrimitive.content
  else v.jsonPrimitive.content
}

private fun <T> outcome(adapter: Adapter<T>, input: JsonObject): Map<String, String>? {
  input["bytes"]?.let {
    val state = State(bytes(it.jsonPrimitive.content))
    return try {
      val decoded = adapter.codec.decode(state)
      mapOf("decodes" to adapter.show(decoded), "read" to state.start.toString())
    } catch (e: DecodingException) {
      mapOf("rejects" to "true")
    }
  }
  val value = adapter.value(input.getValue("value")) ?: return null
  return try {
    mapOf("hex" to encode(adapter.codec, value).hex())
  } catch (e: EncodingException) {
    mapOf("refused" to "true")
  }
}

class CorpusTest {
  private val unrepresentable =
    read("unrepresentable.json").jsonArray.map { it.jsonPrimitive.content }

  @TestFactory
  fun corpus(): List<DynamicNode> {
    val codecs = read("index.json").jsonObject.values.flatMap { it.jsonObject.keys }.sorted()
    return codecs.map { name ->
      val adapter = adapters[name]
      when {
        adapter != null -> dynamicContainer(name, cases(name, adapter))
        name in unsupported -> dynamicTest(name) { abort<Unit>("unsupported") }
        else -> dynamicTest(name) { fail("$name is neither supported nor declared unsupported") }
      }
    }
  }

  private fun cases(name: String, adapter: Adapter<*>): List<DynamicTest> {
    val answers = read("$name/answers.json").jsonObject
    return read("$name/cases.json").jsonObject.getValue("cases").jsonArray.map { element ->
      val case = element.jsonObject
      val id = case.getValue("id").jsonPrimitive.content
      dynamicTest(id) {
        val got =
          try {
            outcome(adapter, case.getValue("input").jsonObject)
          } catch (e: NotImplementedError) {
            abort<Nothing>("not yet built: ${e.message}")
          }
        if (got == null) {
          val rules = case.getValue("rules").jsonArray.map { it.jsonPrimitive.content }
          if (rules.none { it in unrepresentable })
            fail("$id: unrepresentable on a rule that is not marked")
          abort<Nothing>("unrepresentable")
        }
        assertEquals(expected(answers.getValue(id).jsonObject), got, id)
      }
    }
  }
}
