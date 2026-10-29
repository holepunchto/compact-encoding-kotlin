import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsEnvSpec
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsPlugin

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.android.kotlin.multiplatform.library)
  alias(libs.plugins.dokka)
  alias(libs.plugins.ktfmt)
  id("maven-publish")
}

group = "com.github.holepunchto"

version = System.getenv("VERSION") ?: "0.0.0"

kotlin {
  explicitApi()

  @OptIn(org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation::class) abiValidation()

  compilerOptions { allWarningsAsErrors = true }

  jvm { compilerOptions { jvmTarget = JvmTarget.JVM_11 } }

  js { nodejs() }

  android {
    namespace = "to.holepunch.compactEncoding"
    compileSdk = 36
    minSdk = 29
    withHostTest {}
    compilerOptions { jvmTarget = JvmTarget.JVM_11 }
  }

  sourceSets {
    commonTest.dependencies { implementation(kotlin("test")) }
    jvmTest.dependencies { implementation(libs.kotlinx.serialization.json) }
  }
}

dokka {
  dokkaSourceSets.configureEach { reportUndocumented = true }
  dokkaPublications.html { failOnWarning = true }
}

ktfmt { googleStyle() }

plugins.withType<NodeJsPlugin> { the<NodeJsEnvSpec>().download = false }

val corpusVersion = "0.2.0"

val corpusDir = layout.buildDirectory.dir("corpus")

val packCorpus =
  tasks.register<Exec>("packCorpus") {
    val npm =
      if (System.getProperty("os.name").startsWith("Windows")) listOf("cmd", "/c", "npm")
      else listOf("npm")
    inputs.property("version", corpusVersion)
    outputs.file(corpusDir.map { it.file("compact-encoding-test-$corpusVersion.tgz") })
    workingDir(corpusDir)
    commandLine(npm + listOf("pack", "compact-encoding-test@$corpusVersion", "--loglevel=error"))
  }

val unpackCorpus =
  tasks.register<Sync>("unpackCorpus") {
    from(packCorpus.map { tarTree(it.outputs.files.singleFile) })
    into(corpusDir.map { it.dir("unpacked") })
  }

tasks.named<Test>("jvmTest") {
  useJUnitPlatform()
  inputs.files(unpackCorpus)
  val fixtures = corpusDir.map { it.dir("unpacked/package/fixtures").asFile.path }
  jvmArgumentProviders.add(
    CommandLineArgumentProvider { listOf("-Dcorpus.fixtures=${fixtures.get()}") }
  )
}

tasks.register("lint") {
  group = "verification"
  description = "Checks the formatting, the ABI, and the documentation."
  dependsOn("ktfmtCheck", "checkKotlinAbi", "dokkaGenerate")
}

tasks.register("test") {
  group = "verification"
  description = "Runs the tests on every target."
  dependsOn("jvmTest", "testAndroidHostTest", "jsNodeTest")
}

tasks.check { dependsOn("lint") }
