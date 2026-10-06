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

private const val REPLACEMENT = '�'

private fun pairAt(value: String, i: Int): Boolean =
  value[i].isHighSurrogate() && i + 1 < value.length && value[i + 1].isLowSurrogate()

private fun utf8Length(value: String): Int {
  var length = 0
  var i = 0

  while (i < value.length) {
    val c = value[i].code

    if (c < 0x80) {
      length += 1
    } else if (c < 0x800) {
      length += 2
    } else if (pairAt(value, i)) {
      length += 4
      i++
    } else {
      length += 3
    }

    i++
  }

  return length
}

private fun utf8Write(value: String, buffer: ByteArray, start: Int): Int {
  var at = start
  var i = 0

  while (i < value.length) {
    var c = value[i].code

    if (pairAt(value, i)) {
      c = 0x10000 + ((c - 0xd800) shl 10) + (value[++i].code - 0xdc00)
    } else if (value[i].isSurrogate()) {
      c = REPLACEMENT.code
    }

    if (c < 0x80) {
      buffer[at++] = c.toByte()
    } else if (c < 0x800) {
      buffer[at++] = (0xc0 or (c shr 6)).toByte()
      buffer[at++] = (0x80 or (c and 0x3f)).toByte()
    } else if (c < 0x10000) {
      buffer[at++] = (0xe0 or (c shr 12)).toByte()
      buffer[at++] = (0x80 or ((c shr 6) and 0x3f)).toByte()
      buffer[at++] = (0x80 or (c and 0x3f)).toByte()
    } else {
      buffer[at++] = (0xf0 or (c shr 18)).toByte()
      buffer[at++] = (0x80 or ((c shr 12) and 0x3f)).toByte()
      buffer[at++] = (0x80 or ((c shr 6) and 0x3f)).toByte()
      buffer[at++] = (0x80 or (c and 0x3f)).toByte()
    }

    i++
  }

  return at
}

private fun StringBuilder.appendCodePoint(c: Int) {
  if (c < 0x10000) {
    append(c.toChar())
  } else {
    append((0xd800 + ((c - 0x10000) shr 10)).toChar())
    append((0xdc00 + ((c - 0x10000) and 0x3ff)).toChar())
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

    if (b !in lower..upper) {
      needed = 0
      seen = 0
      lower = 0x80
      upper = 0xbf
      out.append(REPLACEMENT)
      continue
    }

    lower = 0x80
    upper = 0xbf
    c = (c shl 6) or (b and 0x3f)

    if (++seen == needed) {
      out.appendCodePoint(c)
      needed = 0
      seen = 0
    }

    i++
  }

  if (needed != 0) out.append(REPLACEMENT)

  return out.toString()
}
