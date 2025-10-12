import org.gradle.api.plugins.jvm.JvmTestSuite

plugins {
    `java-library`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktlint)
}

group = "com.bsingularity.kontrakt"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(libs.slf4j.api)
    implementation(libs.kotlin.logging.jvm)

    implementation(libs.classgraph)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlin.reflect)

    testImplementation(kotlin("test"))
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.kotlinx.coroutines.test)
    testRuntimeOnly(libs.junit.jupiter.engine)
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
        }
    }
}

kotlin {
    jvmToolchain(21)
}
