import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    group = "com.bsingularity.kontrakt"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }

    extensions.configure<KotlinJvmProjectExtension> {
        jvmToolchain(21)
    }
}