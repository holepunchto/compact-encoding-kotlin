package to.holepunch.compactencoding

public val bool: Codec<Boolean> =
  object : Codec<Boolean> {
    override fun preencode(state: State, value: Boolean) {
      state.end += 1
    }

    override fun encode(state: State, value: Boolean) {
      state.buffer[state.start++] = if (value) 1 else 0
    }

    override fun decode(state: State): Boolean {
      if (state.remaining < 1) throw DecodingException("out of bounds")

      return state.buffer[state.start++] == 1.toByte()
    }
  }
