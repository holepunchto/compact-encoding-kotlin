package to.holepunch.compactencoding.usage

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import to.holepunch.compactencoding.Codec
import to.holepunch.compactencoding.State
import to.holepunch.compactencoding.decode
import to.holepunch.compactencoding.encode
import to.holepunch.compactencoding.uint
import to.holepunch.compactencoding.utf8

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
