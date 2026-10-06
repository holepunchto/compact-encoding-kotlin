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
  mapOf(
    "any" to "not ported yet",
    "array-uint" to "not ported yet",
    "arraybuffer" to "not ported yet",
    "ascii" to "not ported yet",
    "base64" to "not ported yet",
    "bigint" to "not ported yet",
    "bigint64" to "not ported yet",
    "bigint64array" to "not ported yet",
    "biguint" to "not ported yet",
    "biguint64" to "not ported yet",
    "biguint64array" to "not ported yet",
    "binary" to "not ported yet",
    "bitarray" to "not ported yet",
    "bool" to "not ported yet",
    "buffer" to "not ported yet",
    "date" to "not ported yet",
    "fixed-16" to "not ported yet",
    "fixed-24" to "not ported yet",
    "fixed-3" to "not ported yet",
    "fixed-32" to "not ported yet",
    "fixed-64" to "not ported yet",
    "fixed-8" to "not ported yet",
    "float32" to "not ported yet",
    "float32array" to "not ported yet",
    "float64" to "not ported yet",
    "float64array" to "not ported yet",
    "frame-uint" to "not ported yet",
    "hex" to "not ported yet",
    "int16" to "not ported yet",
    "int16array" to "not ported yet",
    "int16be" to "not ported yet",
    "int24" to "not ported yet",
    "int32" to "not ported yet",
    "int32array" to "not ported yet",
    "int32be" to "not ported yet",
    "int40" to "not ported yet",
    "int48" to "not ported yet",
    "int56" to "not ported yet",
    "int64" to "not ported yet",
    "int64be" to "not ported yet",
    "int8" to "not ported yet",
    "int8array" to "not ported yet",
    "intbe" to "not ported yet",
    "ip" to "not ported yet",
    "ipAddress" to "not ported yet",
    "ipv4" to "not ported yet",
    "ipv4Address" to "not ported yet",
    "ipv6" to "not ported yet",
    "ipv6Address" to "not ported yet",
    "json" to "not ported yet",
    "lexint" to "not ported yet",
    "ndjson" to "not ported yet",
    "none" to "not ported yet",
    "optionalBuffer" to "not ported yet",
    "port" to "not ported yet",
    "raw" to "not ported yet",
    "record-utf8-uint" to "not ported yet",
    "string" to "not ported yet",
    "stringRecord" to "not ported yet",
    "ucs2" to "not ported yet",
    "uint16array" to "not ported yet",
    "uint16be" to "not ported yet",
    "uint24" to "not ported yet",
    "uint32array" to "not ported yet",
    "uint32be" to "not ported yet",
    "uint40" to "not ported yet",
    "uint48" to "not ported yet",
    "uint56" to "not ported yet",
    "uint64be" to "not ported yet",
    "uint8" to "not ported yet",
    "uint8array" to "not ported yet",
    "uintbe" to "not ported yet",
    "utf16le" to "not ported yet",
    "utf8" to "not ported yet",
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

private fun signed(element: JsonElement): Long? =
  integer(element)?.takeIf { it.bitLength() < 64 }?.toLong()

private val adapters: Map<String, Adapter<*>> =
  mapOf(
    "int" to Adapter(int, { signed(it) }, { it.toString() }),
    "uint" to Adapter(uint, { unsigned(it, 64) }, { it.toString() }),
    "uint16" to Adapter(uint16, { unsigned(it, 16)?.toUShort() }, { it.toString() }),
    "uint32" to Adapter(uint32, { unsigned(it, 32)?.toUInt() }, { it.toString() }),
    "uint64" to Adapter(uint64, { unsigned(it, 64) }, { it.toString() }),
  )

private fun <T> expected(adapter: Adapter<T>, answer: JsonObject): Map<String, String> =
  answer.mapValues { (key, v) ->
    if (key == "decodes") adapter.value(v)?.let(adapter.show) ?: v.jsonPrimitive.content
    else v.jsonPrimitive.content
  }

private fun <T> outcome(adapter: Adapter<T>, input: JsonObject): Map<String, String>? {
  input["bytes"]?.let {
    val state = State(it.jsonPrimitive.content.hexToByteArray())
    return try {
      val decoded = adapter.codec.decode(state)
      mapOf("decodes" to adapter.show(decoded), "read" to state.start.toString())
    } catch (e: DecodingException) {
      mapOf("rejects" to "true")
    }
  }
  val value = adapter.value(input.getValue("value")) ?: return null
  return try {
    mapOf("hex" to encode(adapter.codec, value).toHexString())
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
        name in unsupported -> dynamicTest(name) { abort<Nothing>(unsupported.getValue(name)) }
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
        assertEquals(expected(adapter, answers.getValue(id).jsonObject), got, id)
      }
    }
  }
}
