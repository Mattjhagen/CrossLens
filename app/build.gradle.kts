import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

// Load release signing configuration
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
val hasKeystoreFile = keystorePropertiesFile.exists()

if (hasKeystoreFile) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

// Environment variables take precedence over keystore.properties
fun getSigningProperty(propertyName: String, envVarName: String): String? {
    return System.getenv(envVarName) ?: keystoreProperties.getProperty(propertyName)
}

val storeFilePath = getSigningProperty("storeFile", "CROSSLENS_STORE_FILE")
val storePassword = getSigningProperty("storePassword", "CROSSLENS_STORE_PASSWORD")
val keyAlias = getSigningProperty("keyAlias", "CROSSLENS_KEY_ALIAS")
val keyPassword = getSigningProperty("keyPassword", "CROSSLENS_KEY_PASSWORD")

val hasSigningConfig = storeFilePath != null && storePassword != null &&
                       keyAlias != null && keyPassword != null

android {
    namespace = "com.crosslens.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.crosslens.app"
        minSdk = 29
        targetSdk = 34
        versionCode = 12
        versionName = "0.0.12-beta"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
            arg("room.incremental", "true")
            arg("room.generateKotlin", "true")
        }
    }

    signingConfigs {
        if (hasSigningConfig) {
            create("release") {
                val storeFile = File(storeFilePath!!)
                if (!storeFile.exists()) {
                    throw GradleException(
                        """
                        |Release keystore file not found: $storeFilePath
                        |
                        |Please ensure the keystore file exists at the specified path.
                        |See docs/RELEASE_SIGNING.md for setup instructions.
                        """.trimMargin()
                    )
                }

                this.storeFile = storeFile
                this.storePassword = storePassword
                this.keyAlias = keyAlias
                this.keyPassword = keyPassword
            }
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            // Apply signing configuration if available
            if (hasSigningConfig) {
                signingConfig = signingConfigs.getByName("release")
            } else {
                // Fail with clear instructions if signing config is missing
                gradle.taskGraph.whenReady {
                    if (hasTask(":app:assembleRelease") ||
                        hasTask(":app:bundleRelease") ||
                        hasTask(":app:installRelease")) {
                        throw GradleException(
                            """
                            |
                            |❌ Release signing configuration missing.
                            |
                            |To build a release APK, you must configure release signing credentials.
                            |
                            |Option 1: Create keystore.properties file
                            |  1. Copy keystore.properties.example to keystore.properties
                            |  2. Replace placeholder values with your actual signing credentials
                            |  3. Generate a signing key if you don't have one yet
                            |
                            |Option 2: Set environment variables
                            |  export CROSSLENS_STORE_FILE=/path/to/keystore.jks
                            |  export CROSSLENS_STORE_PASSWORD=your_store_password
                            |  export CROSSLENS_KEY_ALIAS=your_key_alias
                            |  export CROSSLENS_KEY_PASSWORD=your_key_password
                            |
                            |For detailed setup instructions, see:
                            |  docs/RELEASE_SIGNING.md
                            |
                            |Debug builds continue to work without signing configuration.
                            |
                            """.trimMargin()
                        )
                    }
                }
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=kotlin.RequiresOptIn",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api"
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Browser (for Custom Tabs)
    implementation("androidx.browser:browser:1.8.0")

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Image loading
    implementation(libs.coil.compose)

    // Accompanist
    implementation(libs.accompanist.swiperefresh)

    // JSON
    implementation(libs.gson)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.arch.core.testing)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.turbine)
    testImplementation(libs.androidx.room.testing)

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.room.testing)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
