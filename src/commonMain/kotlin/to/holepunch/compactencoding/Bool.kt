package to.holepunch.compactencoding

/**
 * A [Boolean] in one byte, 1 for true and 0 for false. Any byte other than 1 decodes as false.
 *
 * Decoding throws [DecodingException] when no byte remains.
 */
public val bool: Codec<Boolean> =
  object : Codec<Boolean> {
    override fun preencode(state: State, value: Boolean) {
      state.end += 1
    }

    override fun encode(state: State, value: Boolean) {
      state.buffer[state.start++] = if (value) 1 else 0
    }

    override fun decode(state: State): Boolean {
      state.ensureRemaining("bool", 1)

      return state.buffer[state.start++] == 1.toByte()
    }
  }
