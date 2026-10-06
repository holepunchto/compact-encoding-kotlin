import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  kotlin("jvm") version "2.4.20"
  id("com.ncorti.ktfmt.gradle") version "0.27.0"
  id("maven-publish")
}

group = "to.holepunch"

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
