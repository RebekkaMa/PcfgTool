plugins {
    kotlin("multiplatform") version "1.6.10"
}

group = "me.rebekka"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        withJava()
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    linuxX64("native"){
        binaries{
            executable {  }
        }
    }


    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
                implementation("com.github.ajalt.clikt:clikt:3.4.0")
                implementation("com.github.h0tk3y.betterParse:better-parse:0.4.3")
                implementation("com.squareup.okio:okio:3.0.0")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.0")
                implementation("io.kotest:kotest-assertions-core:5.2.1")
                implementation("com.squareup.okio:okio-fakefilesystem:3.0.0")

            }
        }
        val jvmMain by getting
        val jvmTest by getting
        val nativeMain by getting
        val nativeTest by getting
    }
}
