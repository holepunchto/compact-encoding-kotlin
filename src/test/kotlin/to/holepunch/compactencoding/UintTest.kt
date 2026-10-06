package to.holepunch.compactencoding

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class UintTest {
  @Test fun `encodes 42 as one byte`() = assertContentEquals(byteArrayOf(0x2a), encode(uint, 42uL))

  @Test
  fun `encodes fd as three bytes including fd prefix`() =
    assertContentEquals(byteArrayOf(0xfd.toByte(), 0xfd.toByte(), 0x00), encode(uint, 0xfduL))

  @Test
  fun `decodes overlong fd 01 00 as 1`() =
    assertEquals(1uL, decode(uint, byteArrayOf(0xfd.toByte(), 0x1, 0x00)))
}
