package to.holepunch.compactencoding

/**
 * A [ByteArray] as a [uint] count followed by that many bytes. Decoding copies the bytes out.
 *
 * Decoding throws [DecodingException] when the count runs past the bytes remaining.
 */
public val buffer: Codec<ByteArray> =
  object : Codec<ByteArray> {
    override fun preencode(state: State, value: ByteArray) {
      uint.preencode(state, value.size.toULong())

      state.end += value.size
    }

    override fun encode(state: State, value: ByteArray) {
      uint.encode(state, value.size.toULong())

      value.copyInto(state.buffer, state.start)

      state.start += value.size
    }

    override fun decode(state: State): ByteArray {
      val count = uint.decode(state)

      state.ensureCount("buffer", count)

      val end = state.start + count.toInt()
      val value = state.buffer.copyOfRange(state.start, end)

      state.start = end

      return value
    }
  }
