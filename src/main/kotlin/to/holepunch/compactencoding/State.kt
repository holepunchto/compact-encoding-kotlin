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
