package to.holepunch.compactencoding

import kotlin.test.Test
import kotlin.test.assertContentEquals

class UintTest {
  @Test fun `encodes 42 as one byte`() = assertContentEquals(byteArrayOf(0x2a), encode(uint, 42uL))
}

