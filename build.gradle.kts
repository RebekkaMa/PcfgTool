plugins {
    kotlin("jvm") version "1.6.20"
    application
}

group = "me.rebekka"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    implementation("com.github.ajalt.clikt:clikt:3.4.0")
    implementation("com.github.h0tk3y.betterParse:better-parse:0.4.3")

    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.0")
    testImplementation("io.kotest:kotest-assertions-core:5.2.1")
    testImplementation("com.squareup.okio:okio-fakefilesystem:3.0.0")
}

kotlin {
    jvmToolchain {
        (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(11))
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
}

application {
    mainClass.set("PcfgToolMainKt")
    applicationDefaultJvmArgs = setOf("-Dkotlinx.coroutines.debug")
    executableDir = ""
}