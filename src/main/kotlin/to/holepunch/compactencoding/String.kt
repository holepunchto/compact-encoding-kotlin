package to.holepunch.compactencoding

public val utf8: Codec<String> =
  object : Codec<String> {
    override fun preencode(state: State, value: String) {
      val length = utf8Length(value)

      uint.preencode(state, length.toULong())

      state.end += length
    }

    override fun encode(state: State, value: String) {
      uint.encode(state, utf8Length(value).toULong())

      state.start = utf8Write(value, state.buffer, state.start)
    }

    override fun decode(state: State): String {
      val count = uint.decode(state)

      if (count > state.remaining.toULong()) throw DecodingException("out of bounds")

      val end = state.start + count.toInt()
      val value = utf8Read(state.buffer, state.start, end)

      state.start = end

      return value
    }
  }

private val REPLACEMENT = Char(0xfffd)

private fun isSurrogatePairAt(value: String, i: Int): Boolean =
  value[i].isHighSurrogate() && i + 1 < value.length && value[i + 1].isLowSurrogate()

private fun scalarAt(value: String, i: Int): Int =
  when {
    isSurrogatePairAt(value, i) ->
      0x10000 +
        ((value[i] - Char.MIN_HIGH_SURROGATE) shl 10) +
        (value[i + 1] - Char.MIN_LOW_SURROGATE)
    value[i].isSurrogate() -> REPLACEMENT.code
    else -> value[i].code
  }

private fun utf8Width(scalar: Int): Int =
  when {
    scalar < 0x80 -> 1
    scalar < 0x800 -> 2
    scalar < 0x10000 -> 3
    else -> 4
  }

private fun utf16Width(scalar: Int): Int = if (scalar < 0x10000) 1 else 2

private fun utf8Length(value: String): Int {
  var length = 0
  var i = 0

  while (i < value.length) {
    val scalar = scalarAt(value, i)

    length += utf8Width(scalar)
    i += utf16Width(scalar)
  }

  return length
}

private fun utf8Write(value: String, buffer: ByteArray, start: Int): Int {
  var at = start
  var i = 0

  while (i < value.length) {
    val c = scalarAt(value, i)

    when (utf8Width(c)) {
      1 -> buffer[at++] = c.toByte()
      2 -> {
        buffer[at++] = (0xc0 or (c shr 6)).toByte()
        buffer[at++] = (0x80 or (c and 0x3f)).toByte()
      }
      3 -> {
        buffer[at++] = (0xe0 or (c shr 12)).toByte()
        buffer[at++] = (0x80 or ((c shr 6) and 0x3f)).toByte()
        buffer[at++] = (0x80 or (c and 0x3f)).toByte()
      }
      else -> {
        buffer[at++] = (0xf0 or (c shr 18)).toByte()
        buffer[at++] = (0x80 or ((c shr 12) and 0x3f)).toByte()
        buffer[at++] = (0x80 or ((c shr 6) and 0x3f)).toByte()
        buffer[at++] = (0x80 or (c and 0x3f)).toByte()
      }
    }

    i += utf16Width(c)
  }

  return at
}

private fun StringBuilder.appendScalar(scalar: Int) {
  if (scalar < 0x10000) {
    append(scalar.toChar())
  } else {
    append(Char.MIN_HIGH_SURROGATE + ((scalar - 0x10000) shr 10))
    append(Char.MIN_LOW_SURROGATE + ((scalar - 0x10000) and 0x3ff))
  }
}

private fun utf8Read(buffer: ByteArray, start: Int, end: Int): String {
  val out = StringBuilder(end - start)
  var needed = 0
  var seen = 0
  var c = 0
  var lower = 0x80
  var upper = 0xbf
  var i = start

  while (i < end) {
    val b = buffer[i].toInt() and 0xff

    if (needed == 0) {
      if (b < 0x80) {
        out.append(b.toChar())
      } else if (b in 0xc2..0xdf) {
        needed = 1
        c = b and 0x1f
      } else if (b in 0xe0..0xef) {
        if (b == 0xe0) lower = 0xa0
        if (b == 0xed) upper = 0x9f
        needed = 2
        c = b and 0x0f
      } else if (b in 0xf0..0xf4) {
        if (b == 0xf0) lower = 0x90
        if (b == 0xf4) upper = 0x8f
        needed = 3
        c = b and 0x07
      } else {
        out.append(REPLACEMENT)
      }

      i++
      continue
    }

    val continues = b in lower..upper

    lower = 0x80
    upper = 0xbf

    if (!continues) {
      needed = 0
      seen = 0
      out.append(REPLACEMENT)
      continue
    }

    c = (c shl 6) or (b and 0x3f)

    if (++seen == needed) {
      out.appendScalar(c)
      needed = 0
      seen = 0
    }

    i++
  }

  if (needed != 0) out.append(REPLACEMENT)

  return out.toString()
}
