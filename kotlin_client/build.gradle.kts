plugins {
    kotlin("jvm") version "1.9.22"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.java.dev.jna:jna:5.14.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    // Add Swing dispatcher for Main support in JVM
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.7.3")
}

kotlin {
    jvmToolchain(17)
}

application {
    // MainKt refers to the top-level main() in Main.kt (no package)
    mainClass.set("MainKt")
}

// Add a custom task to run Coroutine.kt
tasks.register<JavaExec>("runCoroutine") {
    group = "application"
    description = "Run the Coroutine demo"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("CoroutineKt")
}

// Point the source set at the generated UniFFI Kotlin bindings instead of copying.
sourceSets {
    main {
        // Use only the freshly generated bindings (nested path) to avoid duplicate classes.
        kotlin.srcDir("../out/uniffi/arithmetic")
    }
}
