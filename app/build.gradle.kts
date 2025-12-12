plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.ksp) // Apply KSP plugin
    alias(libs.plugins.kotlin.serialization) // Using alias for Kotlinx Serialization plugin
    alias(libs.plugins.hilt) // <--- ADD THIS: Apply Hilt Gradle plugin
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

    // Test against release build to validate ProGuard/R8
    testBuildType = "release"

    signingConfigs {
        create("release") {
            // Priority: environment variables (CI/CD) > gradle.properties (local dev)
            val keystorePath = System.getenv("SIGNING_KEY_STORE_PATH")
                ?: project.findProperty("SIGNING_KEY_STORE_PATH")?.toString()
            val storePass = System.getenv("SIGNING_STORE_PASSWORD")
                ?: project.findProperty("SIGNING_STORE_PASSWORD")?.toString()
            val alias = System.getenv("SIGNING_KEY_ALIAS")
                ?: project.findProperty("SIGNING_KEY_ALIAS")?.toString()
            val keyPass = System.getenv("SIGNING_KEY_PASSWORD")
                ?: project.findProperty("SIGNING_KEY_PASSWORD")?.toString()

            if (keystorePath != null && storePass != null && alias != null && keyPass != null) {
                storeFile = file(keystorePath)
                storePassword = storePass
                keyAlias = alias
                keyPassword = keyPass
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // Validate signing config only when building release variants
    tasks.matching { it.name.contains("Release") }.configureEach {
        doFirst {
            val releaseConfig = android.signingConfigs.getByName("release")
            if (releaseConfig.storeFile == null) {
                throw GradleException(
                    """
                    Release signing not configured!

                    For CI/CD: Set environment variables:
                      - SIGNING_KEY_STORE_PATH
                      - SIGNING_STORE_PASSWORD
                      - SIGNING_KEY_ALIAS
                      - SIGNING_KEY_PASSWORD

                    For local development: Add to ~/.gradle/gradle.properties:
                      SIGNING_KEY_STORE_PATH=/path/to/local-dev-release.jks
                      SIGNING_STORE_PASSWORD=your-password
                      SIGNING_KEY_ALIAS=local-dev
                      SIGNING_KEY_PASSWORD=your-password
                    """.trimIndent()
                )
            }
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
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
    hilt {
        enableAggregatingTask = false
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
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

    // Hilt Dependencies
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler) // Main Hilt KSP compiler

    // Hilt WorkManager Integration
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler) // KSP for Hilt AndroidX extensions (like @HiltWorker)

    // Compose tooling for previews (Debug only)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.tooling.preview)

    testImplementation(libs.junit)
    // Mockito for unit testing
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)

    // Robolectric dependencies
    testImplementation(libs.robolectric)
    testImplementation(libs.core.ktx) // For ApplicationProvider

    // Hilt Testing Dependencies (for Robolectric unit tests)
    testImplementation(libs.hilt.android.testing)
    kspTest(libs.hilt.compiler)          // Main Hilt KSP for tests
    kspTest(libs.androidx.hilt.compiler) // AndroidX Hilt KSP for tests

    // Added Test Dependencies
    testImplementation(libs.kotlinx.serialization.cbor)
    testImplementation(libs.avro4k.core)
    testImplementation(libs.google.truth)
    testImplementation(libs.androidx.work.testing)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // UI Automator 2.4 - Modern API with built-in waiting
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.4.0-alpha05")

    // AndroidX Test
    androidTestImplementation("androidx.test:core:1.5.0")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")

    // Truth assertions
    androidTestImplementation("com.google.truth:truth:1.1.5")

    // For instrumented tests with Hilt, you'd also need:
    // androidTestImplementation(libs.hilt.android.testing)
    // kspAndroidTest(libs.hilt.compiler)
    // kspAndroidTest(libs.androidx.hilt.compiler)
}