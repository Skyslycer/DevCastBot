import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.21"
    kotlin("plugin.serialization") version "1.5.21"
    id("com.github.johnrengelman.shadow") version "7.0.0"
    application
}

group = "de.skyslycer"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://repo.skyslycer.de/repository/maven-releases")
}

dependencies {
    // Kord
    implementation("dev.kord:kord-core:0.8.0-M4")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.5.0-RC")

    // Logging
    implementation("org.slf4j:slf4j-api:2.0.0alpha1")
    implementation("ch.qos.logback:logback-classic:1.3.0-alpha5")
    implementation("io.sentry:sentry:5.0.1")
    implementation("io.sentry:sentry-logback:5.0.1")

    // Database
    implementation("org.litote.kmongo:kmongo-coroutine:4.2.8")

    // ID Generation
    implementation("org.apache.commons:commons-lang3:3.0")

    // Language
    implementation("de.skyslycer.skylocalizer:core:1.1")
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "16"
}