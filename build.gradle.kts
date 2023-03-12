import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    base
    kotlin("jvm") version "1.7.20"
    kotlin("plugin.serialization") version "1.6.21"
}

allprojects {
    group = "com.github.m5rian"

    repositories {
        mavenCentral()
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")

    dependencies {
        implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-core", "1.3.2")
        implementation("org.jetbrains.kotlinx", "kotlinx-serialization-core", "1.5.0")
        implementation("org.jetbrains.kotlinx", "kotlinx-serialization-json", "1.5.0")
        implementation("me.tongfei", "progressbar", "0.9.4")
    }
}