package to.holepunch.compactencoding

public interface Codec<T> {
  public fun preencode(state: State, value: T)

  public fun encode(state: State, value: T)

  public fun decode(state: State): T
}

public sealed class CompactEncodingException(message: String) : Exception(message)

public class DecodingException(message: String) : CompactEncodingException(message)

public fun <T> encode(codec: Codec<T>, value: T): ByteArray {
  val state = State()

  codec.preencode(state, value)

  state.allocate()

  codec.encode(state, value)

  return state.buffer
}

public fun <T> decode(codec: Codec<T>, bytes: ByteArray): T {
  val state = State(bytes)

  return codec.decode(state)
}
