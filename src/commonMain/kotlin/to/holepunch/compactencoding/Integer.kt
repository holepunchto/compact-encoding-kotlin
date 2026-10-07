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
      state.ensureRemaining("uint16", 2)

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
      state.ensureRemaining("uint32", 4)

      var n = 0
      for (shift in 0..24 step 8) n = n or (state.buffer[state.start++].toUByte().toInt() shl shift)

      return n.toUInt()
    }
  }

public val uint64: Codec<ULong> =
  object : Codec<ULong> {
    override fun preencode(state: State, value: ULong) {
      state.end += 8
    }

    override fun encode(state: State, value: ULong) {
      for (shift in 0..56 step 8) state.buffer[state.start++] = (value shr shift).toByte()
    }

    override fun decode(state: State): ULong {
      state.ensureRemaining("uint64", 8)

      var n = 0uL
      for (shift in 0..56 step 8) n =
        n or (state.buffer[state.start++].toUByte().toULong() shl shift)

      return n
    }
  }

public val uint: Codec<ULong> =
  object : Codec<ULong> {
    override fun preencode(state: State, value: ULong) {
      state.end +=
        when {
          value <= 0xfcuL -> 1
          value <= 0xffffuL -> 3
          value <= 0xffffffffuL -> 5
          else -> 9
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

      if (value <= 0xffffffffuL) {
        state.buffer[state.start++] = 0xfe.toByte()
        uint32.encode(state, value.toUInt())
        return
      }

      state.buffer[state.start++] = 0xff.toByte()
      uint64.encode(state, value)
    }

    override fun decode(state: State): ULong {
      state.ensureRemaining("uint", 1)

      val prefix = state.buffer[state.start].toUByte().toInt()

      state.ensureRemaining("uint", uintWidth(prefix))
      state.start++

      return when (prefix) {
        0xfd -> uint16.decode(state).toULong()
        0xfe -> uint32.decode(state).toULong()
        0xff -> uint64.decode(state)
        else -> prefix.toULong()
      }
    }
  }

public val int: Codec<Long> =
  object : Codec<Long> {
    override fun preencode(state: State, value: Long) {
      uint.preencode(state, zigZagEncode(value))
    }

    override fun encode(state: State, value: Long) {
      uint.encode(state, zigZagEncode(value))
    }

    override fun decode(state: State): Long = zigZagDecode(uint.decode(state))
  }

private fun uintWidth(prefix: Int): Int =
  when (prefix) {
    0xfd -> 3
    0xfe -> 5
    0xff -> 9
    else -> 1
  }
