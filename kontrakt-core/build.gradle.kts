import org.gradle.api.plugins.jvm.JvmTestSuite

plugins {
    `java-library`
}

dependencies {
    testImplementation(kotlin("test"))

    testRuntimeOnly(libs.junit.jupiter.engine)
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
        }
    }
}