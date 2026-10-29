# compact-encoding-kotlin

Compact encoding schemes for Kotlin. The Kotlin counterpart of [compact-encoding](https://github.com/holepunchto/compact-encoding), and wire compatible with it.

```groovy
repositories {
  maven { url "https://jitpack.io" }
}

dependencies {
  implementation "com.github.holepunchto.compact-encoding-kotlin:compact-encoding-kotlin:<tag>"
}
```

## Usage

```kotlin
import to.holepunch.compactEncoding.Codec
import to.holepunch.compactEncoding.State
import to.holepunch.compactEncoding.decode
import to.holepunch.compactEncoding.encode
import to.holepunch.compactEncoding.uint
import to.holepunch.compactEncoding.utf8

data class Greeting(val name: String, val count: ULong)

val greeting: Codec<Greeting> =
  object : Codec<Greeting> {
    override fun preencode(state: State, value: Greeting) {
      utf8.preencode(state, value.name)
      uint.preencode(state, value.count)
    }

    override fun encode(state: State, value: Greeting) {
      utf8.encode(state, value.name)
      uint.encode(state, value.count)
    }

    override fun decode(state: State) =
      Greeting(name = utf8.decode(state), count = uint.decode(state))
  }

val data = encode(greeting, Greeting("hello", 42uL))

decode(greeting, data) // Greeting(name=hello, count=42)
```

Codecs are composed from the built-in ones, which each implement `preencode`, `encode`, and `decode`.

## License

Apache-2.0
