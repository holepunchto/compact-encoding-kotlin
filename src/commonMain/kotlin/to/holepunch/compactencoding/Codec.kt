package to.holepunch.compactencoding

/**
 * Reads and writes values of type [T] in the compact-encoding format.
 *
 * Encoding takes two passes over a [State]: [preencode] adds the size of the value to [State.end],
 * and once the state is allocated, [encode] writes the value at [State.start]. [decode] reads a
 * value at [State.start] and moves past it.
 */
public interface Codec<T> {
  /** Adds the encoded size of [value] to [State.end]. */
  public fun preencode(state: State, value: T)

  /**
   * Writes [value] at [State.start] and moves past it. [state] must have been sized by [preencode]
   * and allocated.
   *
   * @throws IndexOutOfBoundsException if [state] has less room than [value] needs.
   */
  public fun encode(state: State, value: T)

  /**
   * Reads a value at [State.start] and moves past it.
   *
   * @throws DecodingException if the bytes run out before the value does.
   */
  public fun decode(state: State): T
}

/**
 * A failure a codec reports about its input. Bytes that cannot be decoded throw
 * [DecodingException]. Encoding into a [State] that [Codec.preencode] did not size is a caller
 * error, and fails with the standard library exception instead.
 */
public sealed class CompactEncodingException(message: String) : Exception(message)

/**
 * Bytes that end before the value they hold. The message names the codec, what it needed and what
 * remained, such as `uint32 needs 4 bytes, 2 remain`.
 */
public class DecodingException(message: String) : CompactEncodingException(message)

/** Encodes [value] with [codec] into a buffer of exactly its size. */
public fun <T> encode(codec: Codec<T>, value: T): ByteArray {
  val state = State()

  codec.preencode(state, value)

  state.allocate()

  codec.encode(state, value)

  return state.buffer
}

/**
 * Decodes one value with [codec] from the start of [bytes]. Bytes after the value are ignored.
 *
 * @throws DecodingException if [bytes] end before the value does.
 */
public fun <T> decode(codec: Codec<T>, bytes: ByteArray): T {
  val state = State(bytes)

  return codec.decode(state)
}
