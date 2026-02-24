plugins {
    kotlin("jvm") version "2.3.0"
}

group = "org.example"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.asm)
    implementation(libs.asm.tree)
    implementation(libs.asm.analysis)
    implementation(libs.asm.util)
    implementation(libs.asm.commons)
    testImplementation(kotlin("test"))
    testImplementation(libs.archunit.junit5)
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()

    dependsOn(":execution:classes")

    val executionMainClasses = project(":execution")
        .layout.buildDirectory.dir("classes/kotlin/main")
    systemProperty("execution.classes.dir", executionMainClasses.get().asFile.absolutePath)
}