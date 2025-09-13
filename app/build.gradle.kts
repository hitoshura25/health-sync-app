plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.ksp) // Apply KSP plugin
    alias(libs.plugins.kotlin.serialization) // Using alias for Kotlinx Serialization plugin
}

android {
    namespace = "io.github.hitoshura25.healthsyncapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.github.hitoshura25.healthsyncapp"
        minSdk = 26
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material) 
    implementation(libs.androidx.health.connect.client)

    // ViewModel and Activity KTX
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.activity.ktx)

    // Jetpack Compose dependencies
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.compose.runtime.livedata)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx) // Kotlin Extensions
    ksp(libs.androidx.room.compiler)      // Annotation processor for KSP

    // WorkManager
    implementation(libs.androidx.workmanager.ktx)

    // Avro4k for Kotlin-first Avro serialization
    implementation(libs.avro4k.core)
    // Kotlinx Serialization
    implementation(libs.kotlinx.serialization.core) // Using alias for Kotlinx Serialization core library
    implementation(libs.kotlinx.io.core)

    // Compose tooling for previews (Debug only)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.tooling.preview)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}