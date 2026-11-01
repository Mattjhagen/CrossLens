import java.net.URI
import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

// Firebase is an explicit integration. A clean checkout needs no project credentials.
val firebaseEnabled = providers.gradleProperty("crosslens.firebaseEnabled").orNull == "true"
val uploadCrashlyticsMapping = providers.gradleProperty("crosslens.uploadCrashlyticsMapping").orNull == "true"
if (firebaseEnabled) {
    apply(plugin = "com.google.gms.google-services")
    apply(plugin = "com.google.firebase.crashlytics")
}

// Load release signing configuration
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
val hasKeystoreFile = keystorePropertiesFile.exists()

if (hasKeystoreFile) {
    FileInputStream(keystorePropertiesFile).use { keystoreProperties.load(it) }
}

// Priority: Gradle properties > Environment variables > keystore.properties
fun getSigningProperty(propertyName: String, envVarName: String, gradlePropName: String): String? {
    return project.findProperty(gradlePropName) as? String
        ?: System.getenv(envVarName)
        ?: keystoreProperties.getProperty(propertyName)
}

val storeFilePath = getSigningProperty("storeFile", "CROSSLENS_STORE_FILE", "crosslens.storeFile")
val storePassword = getSigningProperty("storePassword", "CROSSLENS_STORE_PASSWORD", "crosslens.storePassword")
val keyAlias = getSigningProperty("keyAlias", "CROSSLENS_KEY_ALIAS", "crosslens.keyAlias")
val keyPassword = getSigningProperty("keyPassword", "CROSSLENS_KEY_PASSWORD", "crosslens.keyPassword")

val hasSigningConfig = storeFilePath != null && storePassword != null &&
                       keyAlias != null && keyPassword != null

// Capture non-null values for use in signing config
val finalStoreFilePath = storeFilePath ?: ""
val finalStorePassword = storePassword ?: ""
val finalKeyAlias = keyAlias ?: ""
val finalKeyPassword = keyPassword ?: ""

// Owner-provided public pages; empty values leave links visibly unavailable.
fun publicUrlProperty(name: String): String {
    val value = providers.gradleProperty(name).orNull.orEmpty()
    if (value.isEmpty()) return "\"\""
    val uri = URI(value)
    require(uri.scheme == "https" && !uri.host.isNullOrBlank() && uri.userInfo == null) {
        "$name must be a public HTTPS URL"
    }
    return "\"$value\""
}

android {
    namespace = "com.crosslens.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.crosslens.app"
        minSdk = 29
        targetSdk = 36
        versionCode = 16
        versionName = "0.0.16-beta"
        buildConfigField("boolean", "FIREBASE_ENABLED", firebaseEnabled.toString())
        buildConfigField("String", "PRIVACY_POLICY_URL", publicUrlProperty("crosslens.privacyPolicyUrl"))
        buildConfigField("String", "CONTACT_URL", publicUrlProperty("crosslens.contactUrl"))

        testInstrumentationRunner = "com.crosslens.app.CrossLensTestRunner"
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
                val storeFile = rootProject.file(finalStoreFilePath)
                if (!storeFile.exists()) {
                    throw GradleException(
                        """
                        |Release keystore file not found: $finalStoreFilePath
                        |
                        |Please ensure the keystore file exists at the specified path.
                        |See docs/RELEASE_SIGNING.md for setup instructions.
                        """.trimMargin()
                    )
                }

                this.storeFile = storeFile
                this.storePassword = finalStorePassword
                this.keyAlias = finalKeyAlias
                this.keyPassword = finalKeyPassword
            }
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"

            // Disable Crashlytics for debug builds
            if (firebaseEnabled) {
                configure<com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension> {
                    mappingFileUploadEnabled = false
                }
            }
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            // Upload ProGuard mapping files to Crashlytics for symbolicated stack traces
            if (firebaseEnabled) {
                configure<com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension> {
                    mappingFileUploadEnabled = uploadCrashlyticsMapping
                }
            }

            // Without owner credentials, produce unsigned artifacts for verification only.
            if (hasSigningConfig) signingConfig = signingConfigs.getByName("release")
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

    implementation("com.android.billingclient:billing:9.1.0")

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    implementation("androidx.work:work-runtime-ktx:2.9.1")

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

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation("com.google.firebase:firebase-crashlytics-ktx")

    // Testing
    testImplementation(libs.junit)
    testImplementation("org.robolectric:robolectric:4.14.1")
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.arch.core.testing)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.turbine)
    testImplementation(libs.androidx.room.testing)
    // XML parser for JVM unit tests (Android XmlPullParser not available in pure JVM)
    testImplementation("xpp3:xpp3:1.1.4c")

    androidTestImplementation("com.google.dagger:hilt-android-testing:2.51.1")
    kspAndroidTest(libs.hilt.android.compiler)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.room.testing)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
