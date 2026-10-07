package to.holepunch.compactencoding

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DecodingExceptionTest {
  @Test
  fun `uint32 says how many bytes it needs and how many remain`() {
    val e = assertFailsWith<DecodingException> { decode(uint32, byteArrayOf(1, 2)) }
    assertEquals("uint32 needs 4 bytes, 2 remain", e.message)
  }

  @Test
  fun `buffer says its count passes what remain`() {
    val e = assertFailsWith<DecodingException> { decode(buffer, byteArrayOf(5, 1)) }
    assertEquals("buffer count 5 exceeds the 1 byte remaining", e.message)
  }

  @Test
  fun `uint counts its prefix in what it needs`() {
    val e = assertFailsWith<DecodingException> { decode(uint, byteArrayOf(0xfd.toByte(), 1)) }
    assertEquals("uint needs 3 bytes, 2 remain", e.message)
  }
}
