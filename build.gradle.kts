import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  kotlin("jvm") version "2.4.20"
  id("com.ncorti.ktfmt.gradle") version "0.27.0"
  id("maven-publish")
}

group = "com.github.holepunchto"

version = System.getenv("VERSION") ?: "0.0.0"

repositories { mavenCentral() }

dependencies { testImplementation(kotlin("test")) }

kotlin {
  jvmToolchain(21)
  explicitApi()

  compilerOptions {
    allWarningsAsErrors = true
    jvmTarget = JvmTarget.JVM_11
  }
}

ktfmt { googleStyle() }

java {
  targetCompatibility = JavaVersion.VERSION_11
  withSourcesJar()
}

publishing { publications { create<MavenPublication>("maven") { from(components["java"]) } } }

tasks.test { useJUnitPlatform() }

val checkStdlibOnly by tasks.registering {
  val sources = fileTree("src/main/kotlin")
  inputs.files(sources)
  doLast {
    val offenders = sources.files.filter { Regex("""\bjavax?\.""").containsMatchIn(it.readText()) }
    if (offenders.isNotEmpty()) {
      throw GradleException(
        "Codecs use the Kotlin stdlib only, but these reference the JDK: $offenders"
      )
    }
  }
}

tasks.check { dependsOn(checkStdlibOnly) }
