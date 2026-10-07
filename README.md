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

## Build

Building needs JDK 21, Node.js for `npm`, which fetches the conformance corpus, and an Android SDK, found through `ANDROID_HOME` or `sdk.dir` in `local.properties`.

```sh
./gradlew check
```

## License

Apache-2.0
