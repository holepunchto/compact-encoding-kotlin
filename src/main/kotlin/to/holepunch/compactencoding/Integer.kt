package to.holepunch.compactencoding

public val uint16: Codec<UShort> =
  object : Codec<UShort> {
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

public val uint32: Codec<UInt> =
  object : Codec<UInt> {
    override fun preencode(state: State, value: UInt) {
      state.end += 4
    }

    override fun encode(state: State, value: UInt) {
      val n = value.toInt()

      state.buffer[state.start++] = n.toByte()
      state.buffer[state.start++] = (n ushr 8).toByte()
      state.buffer[state.start++] = (n ushr 16).toByte()
      state.buffer[state.start++] = (n ushr 24).toByte()
    }

    override fun decode(state: State): UInt {
      if (state.remaining < 4) throw DecodingException("out of bounds")

      var n = 0
      for (shift in 0..24 step 8) n = n or (state.buffer[state.start++].toUByte().toInt() shl shift)

      return n.toUInt()
    }
  }

public val uint: Codec<ULong> =
  object : Codec<ULong> {
    override fun preencode(state: State, value: ULong) {
      if (value > 0xffffffffuL) TODO("uint above 0xffffffff")

      state.end +=
        when {
          value <= 0xfcuL -> 1
          value <= 0xffffuL -> 3
          else -> 5
        }
    }

    override fun encode(state: State, value: ULong) {
      if (value <= 0xfcuL) {
        state.buffer[state.start++] = value.toByte()
        return
      }

      if (value <= 0xffffuL) {
        state.buffer[state.start++] = 0xfd.toByte()
        uint16.encode(state, value.toUShort())
        return
      }

      state.buffer[state.start++] = 0xfe.toByte()
      uint32.encode(state, value.toUInt())
    }

    override fun decode(state: State): ULong {
      if (state.remaining < 1) throw DecodingException("out of bounds")

      val prefix = state.buffer[state.start++].toUByte().toInt()

      return when (prefix) {
        0xfd -> uint16.decode(state).toULong()
        0xfe -> uint32.decode(state).toULong()
        0xff -> TODO("uint 0xff form")
        else -> prefix.toULong()
      }
    }
  }
