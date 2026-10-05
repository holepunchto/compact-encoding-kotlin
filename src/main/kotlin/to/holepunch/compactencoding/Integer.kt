package to.holepunch.compactencoding

public val uint: Codec<ULong> = object : Codec<ULong> {
  override fun preencode(state: State, value: ULong) {
    state.end += 1
  }

  override fun encode(state: State, value: ULong) {
    if (value > 0xfcuL) throw EncodingException("uint above 0xfc is not supported yet")

    state.buffer[state.start++] = value.toByte()
  }

  override fun decode(state: State): ULong {
    if (state.remaining < 1) throw DecodingException("out of bounds")

    return state.buffer[state.start++].toUByte().toULong()
  }
}
