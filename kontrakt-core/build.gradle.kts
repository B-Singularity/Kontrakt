plugins {
    `java-library`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktlint)
    id("jacoco")
    alias(libs.plugins.pitest)
}

group = "com.bsingularity.kontrakt"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

jacoco {
    toolVersion = libs.versions.jacoco.get()
}

dependencies {
    // 1. Logging & Runtime
    compileOnly(libs.slf4j.api)
    testImplementation(libs.slf4j.api)
    implementation(libs.kotlin.logging.jvm)

    implementation(libs.classgraph)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlin.reflect)

    implementation(libs.junit.platform.engine)

    implementation(libs.mockito.core)
    implementation(libs.mockito.kotlin)

    testImplementation(kotlin("test"))
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.params)
    testImplementation(libs.kotlinx.coroutines.test)
    testRuntimeOnly(libs.slf4j.simple)
    testRuntimeOnly(libs.junit.jupiter.engine)

    testImplementation(libs.assertj.core)
    testImplementation(libs.mockk)
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.named("jacocoTestReport"))
}

tasks.named<JacocoReport>("jacocoTestReport") {
    dependsOn(tasks.test)

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    finalizedBy(tasks.named("jacocoTestCoverageVerification"))
}

tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    dependsOn(tasks.test)

    violationRules {
        rule {
            element = "CLASS"

            excludes = listOf(
                "execution.adapter.mockito.MockitoEngineAdapter",
                "execution.adapter.mockito.MockitoEngineAdapter\$*",

                "discovery.domain.service.TestDiscovererImpl",
                "discovery.domain.service.TestDiscovererImpl\$*",

                "execution.domain.service.generation.FixtureGenerator*",
                "execution.domain.service.generation.FixtureGenerator\$*",

                )

            limit {
                counter = "INSTRUCTION"
                minimum = "0.90".toBigDecimal()
            }
        }
    }
}

pitest {
    junit5PluginVersion.set("1.2.1")

    targetClasses.set(
        setOf(
            "com.bsingularity.kontrakt.core.generation.*",
            "com.bsingularity.kontrakt.core.execution.*",
        ),
    )

    threads.set(4)
    outputFormats.set(setOf("XML", "HTML"))
    timestampedReports.set(false)
}

tasks.named("pitest").configure {
    onlyIf { gradle.startParameter.projectProperties.containsKey("mutation") }
}

kotlin {
    jvmToolchain(21)
}
