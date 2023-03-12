plugins {
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

dependencies {
    implementation(project(":core"))
}

tasks.withType<Jar> {
    manifest { attributes["Main-Class"] = "com.github.m5rian.MainKt" }
}