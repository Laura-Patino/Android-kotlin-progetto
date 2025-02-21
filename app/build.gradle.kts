plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    // Ktor
    id("kotlinx-serialization")

    // Room DB
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.example.progetto"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.progetto"
        minSdk = 34
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.play.services.location)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Core
    implementation(libs.ktor.ktor.client.core)
    implementation(libs.ktor.client.android)
    // Logging
    implementation(libs.ktor.client.logging)
    // JSON Serialization
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.io.ktor.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)

    // DataStore for preferences
    implementation(libs.androidx.datastore.preferences)

    // Room DB
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    //MapBox+
    implementation("com.mapbox.maps:android:11.9.0")
    implementation("com.mapbox.extension:maps-compose:11.9.0")

    //Location
    implementation(libs.kotlinx.coroutines.play.services)
}