plugins {
    java
    kotlin("jvm")
}

group = "com.cobox.iot.vulture"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    testImplementation("junit", "junit", "4.12")
}
