import org.gradle.api.plugins.jvm.JvmTestSuite

dependencies {
    implementation(project(":kontrakt-core"))

    testImplementation(kotlin("test"))
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
        }
    }
}