// build.gradle.kts (루트 폴더)

// 필요한 타입을 명시적으로 import 합니다.
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    kotlin("jvm") version "2.2.0" apply false
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    group = "com.bsingularity.kontrakt"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }

    // -----------------------------------------------------------------
    // 변경된 부분: 'kotlin' 설정을 명시적인 타입으로 구성합니다.
    // -----------------------------------------------------------------
    extensions.configure<KotlinJvmProjectExtension> {
        jvmToolchain(21)
    }
}