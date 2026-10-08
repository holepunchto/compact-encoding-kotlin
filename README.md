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

[`UsageTest.kt`](src/commonTest/kotlin/to/holepunch/compactencoding/usage/UsageTest.kt) composes the built-in codecs into one for a type of your own, and the build keeps it passing.

## Build

Building needs:

- JDK 21
- Node.js, whose `npm` fetches the conformance corpus
- An Android SDK with platform `android-36`, found through `ANDROID_HOME` or `sdk.dir` in `local.properties`

```sh
./gradlew check
```

## License

Apache-2.0
