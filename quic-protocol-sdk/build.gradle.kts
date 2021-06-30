plugins {
    java
    kotlin("multiplatform")
}

group = "com.cobox.iot.vulture"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val mingwPath = File(System.getenv("MINGW64_DIR") ?: "C:/msys64/mingw64")

kotlin {
    // Determine host preset.
    val hostOs = System.getProperty("os.name")

    // Create target for the host platform.
    val hostTarget = when {
        hostOs == "Mac OS X" -> macosX64("quic-protocol-sdk")
        hostOs == "Linux" -> linuxX64("quic-protocol-sdk")
        hostOs.startsWith("Windows") -> mingwX64("quic-protocol-sdk")
        else -> throw GradleException("Host OS '$hostOs' is not supported in Kotlin/Native $project.")
    }

    hostTarget.apply {


        compilations["main"].enableEndorsedLibs = true
    }

    // Enable experimental stdlib API used by the sample.
    sourceSets {
        commonMain {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib")
            }
        }

        linuxX64("native").apply {
            binaries {
                sharedLib {
//                entryPoint = "nero.quic-protocol-sdk.main"
                    baseName = "msquic"
                    when (preset) {
                        presets["macosX64"] -> linkerOpts(
                            "-L${rootProject.projectDir}/quic-protocol-sdk/msquic/bin",
                            "-Lmsquic"
                        )
                        presets["linuxX64"] -> linkerOpts(
                            "-L${rootProject.projectDir}/quic-protocol-sdk/msquic/bin",
                            "-Lmsquic"
                        )
                        presets["mingwX64"] -> linkerOpts("-L${mingwPath.resolve("${rootProject.projectDir}/quic-protocol-sdk/msquic/bin")}")
                    }
                }


                compilations["main"].cinterops {
                    val msquic by creating {
                        when (preset) {
                            presets["macosX64"] -> includeDirs.headerFilterOnly(
                                "/${rootProject.projectDir}/quic-protocol-sdk/msquic/",
                                "/usr/local/include"
                            )
                            presets["linuxX64"] -> includeDirs.headerFilterOnly(
                                "/${rootProject.projectDir}/quic-protocol-sdk/msquic/",
                                "/usr/include"
                            )
                            presets["mingwX64"] -> includeDirs(mingwPath.resolve("${rootProject.projectDir}/quic-protocol-sdk/msquic/"))
                        }
                    }
                }
//            val sdl by creating {
//                when (preset) {
//                    presets["macosX64"] -> includeDirs("/opt/local/include/SDL2", "/usr/local/include/SDL2")
//                    presets["linuxX64"] -> includeDirs("/usr/include", "/usr/include/x86_64-linux-gnu", "/usr/include/SDL2")
//                    presets["mingwX64"] -> includeDirs(mingwPath.resolve("include/SDL2"))
//                }
//            }
            }
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib")
            }
        }

    }
    dependencies {
        implementation("org.jetbrains.kotlin:kotlin-stdlib")
    }
}