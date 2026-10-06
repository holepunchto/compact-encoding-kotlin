package to.holepunch.compactencoding

internal fun zigZagEncode(value: Long): ULong = ((value shl 1) xor (value shr 63)).toULong()

internal fun zigZagDecode(value: ULong): Long = (value shr 1).toLong() xor -(value and 1uL).toLong()
