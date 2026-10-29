package to.holepunch.compactEncoding.usage

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import to.holepunch.compactEncoding.Codec
import to.holepunch.compactEncoding.State
import to.holepunch.compactEncoding.decode
import to.holepunch.compactEncoding.encode
import to.holepunch.compactEncoding.uint
import to.holepunch.compactEncoding.utf8

data class Greeting(val name: String, val count: ULong)

val greeting: Codec<Greeting> =
  object : Codec<Greeting> {
    override fun preencode(state: State, value: Greeting) {
      utf8.preencode(state, value.name)
      uint.preencode(state, value.count)
    }

    override fun encode(state: State, value: Greeting) {
      utf8.encode(state, value.name)
      uint.encode(state, value.count)
    }

    override fun decode(state: State) =
      Greeting(name = utf8.decode(state), count = uint.decode(state))
  }

class UsageTest {
  @Test
  fun `encodes a greeting as its name then its count`() =
    assertContentEquals(
      byteArrayOf(0x05, 0x68, 0x65, 0x6c, 0x6c, 0x6f, 0x2a),
      encode(greeting, Greeting("hello", 42uL)),
    )

  @Test
  fun `decodes a name then a count as a greeting`() =
    assertEquals(
      Greeting("hello", 42uL),
      decode(greeting, byteArrayOf(0x05, 0x68, 0x65, 0x6c, 0x6c, 0x6f, 0x2a)),
    )
}
