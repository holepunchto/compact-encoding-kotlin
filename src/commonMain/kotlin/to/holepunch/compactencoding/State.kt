package to.holepunch.compactencoding

public class State(public var buffer: ByteArray = ByteArray(0)) {
  public var start: Int = 0
  public var end: Int = buffer.size
  public val remaining: Int
    get() = end - start

  public fun allocate() {
    buffer = ByteArray(end)
  }

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
