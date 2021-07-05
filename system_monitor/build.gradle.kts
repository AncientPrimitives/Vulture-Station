plugins {
    java
    kotlin("multiplatform")
}

group = "com.cobox.iot.vulture"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
}

kotlin {
    targets {
        linuxArm64()
        linuxArm32Hfp()
        linuxX64()
        mingwX64()
        macosX64()
    }
}