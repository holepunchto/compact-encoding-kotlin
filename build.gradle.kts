plugins {
  kotlin("jvm") version "2.4.20"
}

group = "to.holepunch"
version = "0.0.0"

repositories {
  mavenCentral()
}

dependencies {
  testImplementation(kotlin("test"))
}

kotlin {
  jvmToolchain(21)
  explicitApi()
}

tasks.test {
  useJUnitPlatform()
}
