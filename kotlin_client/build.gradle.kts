plugins {
    kotlin("jvm") version "1.9.22"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.java.dev.jna:jna:5.14.0")
}

kotlin {
    jvmToolchain(17)
}

application {
    // MainKt refers to the top-level main() in Main.kt (no package)
    mainClass.set("MainKt")
}

// Point the source set at the generated UniFFI Kotlin bindings instead of copying.
sourceSets {
    main {
        // Use only the freshly generated bindings (nested path) to avoid duplicate classes.
        kotlin.srcDir("../out/uniffi/arithmetic")
    }
}
