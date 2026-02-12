
plugins {
    alias(libs.plugins.android.application)
    //alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.devtools.ksp")
    id("androidx.room")
}

android {
    namespace = "com.mobile.campico"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.mobile.campico"
        minSdk = 26  // originally 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        //testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner
    }
    buildFeatures {
        //dataBinding = true
        viewBinding = true
    }
    //testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner
}

room {
    schemaDirectory("$projectDir/schemas")
}


dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.navigation.testing)
    implementation(libs.core.ktx)
    implementation(libs.androidx.compose.ui.test.junit4)

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.lifecycle)
    //implementation(libs.androidx.compose.ui.test)
    testImplementation(libs.junit)
// For local unit tests
    //testImplementation(libs.androidx.core.testing)
    testImplementation(libs.robolectric)
// Needed for createComposeRule(), but not for createAndroidComposeRule<YourActivity>():
    debugImplementation(libs.androidx.compose.ui.test.manifest)

//androidTestImplementation(libs.androidx.junit)
//androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
// Test rules and transitive dependencies:
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    /* Dependencies related to room */
// room-compiler: is for the code generation that happens
// during the build process to create the necessary database
// infrastructure based on your annotations.
// implementation(libs.androidx.room.compiler)
// room-runtime: is for the code that runs on
// your device to interact with the database.
    implementation(libs.androidx.room.runtime)
// If this project uses any Kotlin source, use Kotlin Symbol Processing (KSP)
// See Add the KSP plugin to your project
    ksp(libs.androidx.room.compiler)
// optional - Kotlin Extensions and Coroutines support for Room
    implementation(libs.androidx.room.ktx)
// optional - Test helpers
    testImplementation(libs.androidx.room.testing)

    // serialization
    implementation(libs.kotlinx.serialization.json) // Or the latest version
    implementation(libs.androidx.navigation.compose.v280alpha08) // Or a later version

    // retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    // datastore
    // Preferences DataStore (SharedPreferences like APIs)
    implementation(libs.androidx.datastore.preferences)
    // Alternatively - without an Android dependency.
    implementation(libs.androidx.datastore.preferences.core)
    // barcode
    // ML Kit Barcode Scanning
    implementation(libs.mlkit.barcode.scanning)
    implementation(libs.camera.mlkit.vision)

// CameraX dependencies for camera integration
    implementation(libs.androidx.camera.core)
    implementation(libs.camera.camera2)
    implementation(libs.camera.lifecycle)
    implementation(libs.camera.view)

// Accompanist Permissions for handling runtime permissions
    implementation(libs.accompanistPermissions)
    // layout
    implementation(libs.androidx.constraintlayout)
}
