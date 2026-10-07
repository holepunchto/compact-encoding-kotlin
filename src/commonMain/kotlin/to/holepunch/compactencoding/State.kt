package to.holepunch.compactencoding

/**
 * A buffer and the window into it that codecs read and write.
 *
 * @property buffer the bytes being encoded into or decoded from.
 */
public class State(public var buffer: ByteArray = ByteArray(0)) {
  /** Where the next read or write happens. */
  public var start: Int = 0
  /** Where the window ends. While preencoding, the size so far. */
  public var end: Int = buffer.size
  /** The bytes between [start] and [end]. */
  public val remaining: Int
    get() = end - start

  /** Replaces [buffer] with [end] zero bytes to encode into. */
  public fun allocate() {
    buffer = ByteArray(end)
  }

  /** Moves [start] back to the first byte. */
  public fun rewind() {
    start = 0
  }
}

internal fun State.ensureRemaining(codec: String, width: Int) {
  if (remaining < width) {
    val verb = if (remaining == 1) "remains" else "remain"
    throw DecodingException("$codec needs ${bytes(width)}, $remaining $verb")
  }
}

internal fun State.ensureCount(codec: String, count: ULong) {
  if (count > remaining.toULong()) {
    throw DecodingException("$codec count $count exceeds the ${bytes(remaining)} remaining")
  }
}

private fun bytes(n: Int): String = if (n == 1) "1 byte" else "$n bytes"
