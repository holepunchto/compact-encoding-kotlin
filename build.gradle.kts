import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  kotlin("jvm") version "2.4.20"
  kotlin("multiplatform") version "2.4.20" apply false
  id("com.ncorti.ktfmt.gradle") version "0.27.0"
  id("maven-publish")
}

group = "com.github.holepunchto"

version = System.getenv("VERSION") ?: "0.0.0"

repositories { mavenCentral() }

dependencies {
  testImplementation(kotlin("test"))
  testImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
}

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

val corpusVersion = "0.1.0"

val corpusDir = layout.buildDirectory.dir("corpus")

val packCorpus =
  tasks.register<Exec>("packCorpus") {
    val npm =
      if (System.getProperty("os.name").startsWith("Windows")) listOf("cmd", "/c", "npm")
      else listOf("npm")
    inputs.property("version", corpusVersion)
    outputs.file(corpusDir.map { it.file("compact-encoding-test-$corpusVersion.tgz") })
    val dir = corpusDir.get().asFile
    workingDir(dir)
    doFirst { dir.mkdirs() }
    commandLine(npm + listOf("pack", "compact-encoding-test@$corpusVersion", "--silent"))
  }

val unpackCorpus =
  tasks.register<Sync>("unpackCorpus") {
    from(packCorpus.map { tarTree(it.outputs.files.singleFile) })
    into(corpusDir.map { it.dir("unpacked") })
  }

tasks.test {
  useJUnitPlatform()
  inputs.files(unpackCorpus)
  val fixtures = corpusDir.map { it.dir("unpacked/package/fixtures").asFile.path }
  jvmArgumentProviders.add(
    CommandLineArgumentProvider { listOf("-Dcorpus.fixtures=${fixtures.get()}") }
  )
}
