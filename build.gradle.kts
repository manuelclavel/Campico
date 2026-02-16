// Top-level build file where you can add configuration options common to all sub-projects/modules.
// build.gradle.kts (project level)

plugins {
    alias(libs.plugins.android.application) apply false
    ///alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("androidx.room") version "2.8.4" apply false
    kotlin("jvm") version "2.3.10"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.3.10" // Or your current Kotlin version
    id("com.google.devtools.ksp") version "2.3.4" apply false
    //id("org.jetbrains.kotlin.android")  apply false

}

kotlin {
    compilerOptions {
        languageVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0
        // Optional: Set jvmTarget
        //jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
    }
}



kotlin {
    jvmToolchain(21) // Use your desired version
}

