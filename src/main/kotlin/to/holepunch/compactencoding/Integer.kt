package to.holepunch.compactencoding

internal val uint16: Codec<UShort> = object : Codec<UShort> {
  override fun preencode(state: State, value: UShort) {
    state.end += 2
  }

  override fun encode(state: State, value: UShort) {
    val n = value.toInt()

    state.buffer[state.start++] = n.toByte()
    state.buffer[state.start++] = (n ushr 8).toByte()
  }

  override fun decode(state: State): UShort {
    if (state.remaining < 2) throw DecodingException("out of bounds")

    val lo = state.buffer[state.start++].toUByte().toInt()
    val hi = state.buffer[state.start++].toUByte().toInt()

    return (lo or (hi shl 8)).toUShort()
  }
}

public val uint: Codec<ULong> = object : Codec<ULong> {
  override fun preencode(state: State, value: ULong) {
    if (value > 0xffffuL) TODO("uint above 0xffff")

    state.end += if (value <= 0xfcuL) 1 else 3
  }

  override fun encode(state: State, value: ULong) {
    if (value <= 0xfcuL) {
      state.buffer[state.start++] = value.toByte()
      return
    }

    state.buffer[state.start++] = 0xfd.toByte()

    uint16.encode(state, value.toUShort())
  }

  override fun decode(state: State): ULong {
    if (state.remaining < 1) throw DecodingException("out of bounds")

    return state.buffer[state.start++].toUByte().toULong()
  }
}
