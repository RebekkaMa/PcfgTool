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
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
    implementation("com.github.ajalt.clikt:clikt:3.4.2")
    implementation("com.github.h0tk3y.betterParse:better-parse:0.4.4")

    testImplementation(kotlin("test"))
    testImplementation("io.kotest:kotest-runner-junit5:5.3.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.1")
    testImplementation("io.kotest:kotest-assertions-core:5.3.0")
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
    executableDir = ""
    applicationDefaultJvmArgs = setOf("-Xms4g", "-XX:MaxTenuringThreshold=15", "-Xmx14g")
}