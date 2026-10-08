# Changelog

## 0.1.0

Initial release.

- Kotlin Multiplatform library for JVM and Android (`minSdk` 29, Java 11 bytecode), wire-compatible with the JavaScript `compact-encoding` and checked against the `compact-encoding-test` 0.2.0 corpus.
- Codecs: `uint`, `uint16`, `uint32`, `uint64`, `int`, `bool`, `buffer` and `utf8`.
- One-shot `encode`/`decode` helpers plus the three-phase `State` API.
- `DecodingException` names the codec, the bytes it needed and the bytes that remained.
