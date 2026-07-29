import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
}

// Reads a secret from local.properties first (gitignored, used for local/dev builds),
// falling back to a real environment variable (used for CI builds). Never hardcoded.
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}
fun secret(key: String): String =
    (localProperties.getProperty(key) ?: System.getenv(key) ?: "").also {
        if (it.isEmpty()) logger.warn("Warning: $key is not set in local.properties or the environment.")
    }

android {
    namespace = "com.huraira.murshid"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.huraira.murshid"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "R2_ACCESS_KEY_ID", "\"${secret("R2_ACCESS_KEY_ID")}\"")
        buildConfigField("String", "R2_SECRET_ACCESS_KEY", "\"${secret("R2_SECRET_ACCESS_KEY")}\"")
        buildConfigField("String", "R2_ENDPOINT", "\"${secret("R2_ENDPOINT")}\"")
        buildConfigField("String", "R2_BUCKET_NAME", "\"${secret("R2_BUCKET_NAME")}\"")
        buildConfigField("String", "R2_PUBLIC_BASE_URL", "\"${secret("R2_PUBLIC_BASE_URL")}\"")
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
        buildConfig = true
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
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.messaging.ktx)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.okhttp)
    implementation(libs.kotlinx.coroutines.play.services)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.material.icons.extended)

    // DataStore for preferences
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.youtube.player)
}