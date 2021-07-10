import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    application
    kotlin("jvm") version "1.4.10"
}

group = "com.cobox.iot.vulture"
version = "1.1.0-alpha"

application.mainClassName = "com.cobox.vulture.Vulture"

repositories {
    mavenCentral()
}

val vertxVersion = project.property("vertxVersion").toString()
val sqliteVersion = project.property("sqliteVersion").toString()

dependencies {
    implementation("io.vertx:vertx-core:$vertxVersion") // Vert.x baseline
    implementation("io.vertx:vertx-lang-kotlin:$vertxVersion") // kotlin for Vert.x
    implementation("io.vertx:vertx-web:$vertxVersion") // Web client for Vert.x
    implementation("io.vertx:vertx-hazelcast:$vertxVersion") // Hazelcast cluster manager for Vert.x
    implementation("io.vertx:vertx-config:$vertxVersion") // Config retriever for Vert.x
    implementation("io.vertx:vertx-jdbc-client:$vertxVersion") // JDBC clent for Vert.x
    implementation("org.xerial:sqlite-jdbc:$sqliteVersion") // Sqlite for JDBC

    implementation(project("nas-server"))
    implementation(project("user-server"))
    implementation(project("ddns-server"))
    implementation(project("system_monitor"))
    implementation(project("database"))
    implementation(project("sdk"))
    implementation(project("utilities"))

    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}