package to.holepunch.compactencoding

import kotlin.test.Test
import kotlin.test.assertContentEquals

class UintTest {
  @Test fun `encodes 42 as one byte`() = assertContentEquals(byteArrayOf(0x2a), encode(uint, 42uL))
  @Test fun `encodes fd as three bytes including fd prefix`() = assertContentEquals(byteArrayOf(0xfd.toByte(), 0xfd.toByte(), 0x00), encode(uint, 0xfduL))
}

