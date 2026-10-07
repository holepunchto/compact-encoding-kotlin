# compact-encoding-kotlin

Compact encoding schemes for Kotlin with the same ABI as https://github.com/holepunchto/compact-encoding

## Install

Published through [JitPack](https://jitpack.io/#holepunchto/compact-encoding-kotlin):

```kotlin
repositories {
  maven("https://jitpack.io")
}

dependencies {
  implementation("com.github.holepunchto:compact-encoding-kotlin:<tag>")
}
```

## Usage

A codec sizes a value, writes it, and reads it back. Compose the built-in codecs into one for your own type:

```kotlin
import to.holepunch.compactencoding.Codec
import to.holepunch.compactencoding.State
import to.holepunch.compactencoding.decode
import to.holepunch.compactencoding.encode
import to.holepunch.compactencoding.uint
import to.holepunch.compactencoding.utf8

data class Greeting(val name: String, val count: ULong)

val greeting =
  object : Codec<Greeting> {
    override fun preencode(state: State, value: Greeting) {
      utf8.preencode(state, value.name)
      uint.preencode(state, value.count)
    }

    override fun encode(state: State, value: Greeting) {
      utf8.encode(state, value.name)
      uint.encode(state, value.count)
    }

    override fun decode(state: State) = Greeting(utf8.decode(state), uint.decode(state))
  }

val bytes = encode(greeting, Greeting("hello", 42uL)) // 05 68 65 6c 6c 6f 2a
val back = decode(greeting, bytes) // Greeting(name=hello, count=42)
```

## License

Apache-2.0
