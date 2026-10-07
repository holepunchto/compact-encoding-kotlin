import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

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

  @OptIn(org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation::class) abiValidation()

  compilerOptions {
    allWarningsAsErrors = true
    jvmTarget = JvmTarget.JVM_11
    apiVersion = KotlinVersion.KOTLIN_2_2
    languageVersion = KotlinVersion.KOTLIN_2_2
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
    commandLine(npm + listOf("pack", "compact-encoding-test@$corpusVersion", "--loglevel=error"))
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

val checkAscii =
  tasks.register("checkAscii") {
    val root = rootDir
    val files =
      fileTree(root) {
        include("**/*.kt", "**/*.kts", "**/*.md", "**/*.yml", "**/*.properties")
        exclude("**/build/**", "**/.gradle/**", "**/.kotlin/**")
      }
    inputs.files(files)
    doLast {
      val lines =
        files.files.flatMap { file ->
          file.readLines().mapIndexedNotNull { i, line ->
            if (line.any { it.code > 0x7f }) "${file.relativeTo(root)}:${i + 1}" else null
          }
        }
      if (lines.isNotEmpty())
        throw GradleException("Non-ASCII characters at:\n" + lines.joinToString("\n"))
    }
  }

tasks.check { dependsOn(checkAscii) }
