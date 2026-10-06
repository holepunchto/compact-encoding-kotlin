plugins { kotlin("multiplatform") }

repositories { mavenCentral() }

kotlin {
  js()

  sourceSets { jsMain { kotlin.srcDir("../src/main/kotlin") } }
}
