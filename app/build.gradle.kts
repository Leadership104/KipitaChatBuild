plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.firebase.crashlytics.plugin)
}

val googleServicesCandidates = listOf(
    "src/dev/google-services.json",
    "src/staging/google-services.json",
    "src/prod/google-services.json",
    "google-services.json"
)

if (googleServicesCandidates.any { file(it).exists() }) {
    apply(plugin = "com.google.gms.google-services")
}

// Read local.properties for API keys and manifest placeholders
fun localProp(key: String, default: String = "placeholder_$key"): String {
    val f = rootProject.file("local.properties")
    if (!f.exists()) return default
    return f.readLines()
        .firstOrNull { it.startsWith("$key=") }
        ?.substringAfter("=")
        ?.trim()
        ?: default
}

android {
    namespace = "com.kipita"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.kipita"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
        buildConfigField("String", "FIREBASE_PROJECT_ID", "\"kipita-a1694\"")
        val mapsKey = localProp("MAPS_API_KEY", "").ifBlank { localProp("GOOGLE_PLACES_API_KEY", "") }
        manifestPlaceholders["MAPS_API_KEY"] = mapsKey
        buildConfigField("String", "GOOGLE_PLACES_API_KEY", "\"${localProp("GOOGLE_PLACES_API_KEY")}\"")
        buildConfigField("String", "GEMINI_API_KEY", "\"${localProp("GEMINI_API_KEY")}\"")
        buildConfigField("String", "OPENAI_API_KEY", "\"${localProp("OPENAI_API_KEY")}\"")
        buildConfigField("String", "CLAUDE_API_KEY", "\"${localProp("CLAUDE_API_KEY")}\"")
        buildConfigField("String", "DWAAT_BASE_URL", "\"${localProp("DWAAT_BASE_URL", "https://api.dwaat.com/")}\"")
        buildConfigField("String", "DWAAT_FALLBACK_BASE_URL", "\"${localProp("DWAAT_FALLBACK_BASE_URL", "https://dwaat.com/")}\"")
    }

    signingConfigs {
        create("localDebug") {
            storeFile = rootProject.file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
        // Release signing — set these four keys in local.properties before running prodRelease build
        val ksPath = localProp("RELEASE_STORE_FILE", "")
        val ksPass = localProp("RELEASE_STORE_PASSWORD", "")
        val kAlias = localProp("RELEASE_KEY_ALIAS", "")
        val kPass  = localProp("RELEASE_KEY_PASSWORD", "")
        if (ksPath.isNotBlank() && ksPass.isNotBlank() && kAlias.isNotBlank() && kPass.isNotBlank()) {
            create("release") {
                storeFile = rootProject.file(ksPath)
                storePassword = ksPass
                keyAlias = kAlias
                keyPassword = kPass
            }
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("localDebug")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            val releaseSigningConfig = runCatching { signingConfigs.getByName("release") }.getOrNull()
            if (releaseSigningConfig != null) signingConfig = releaseSigningConfig
        }
    }

    flavorDimensions += "env"
    productFlavors {
        create("dev") {
            dimension = "env"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            buildConfigField("String", "BASE_URL", "\"https://api.dev.kipita.app/\"")
            buildConfigField("String", "FIREBASE_APP_PACKAGE", "\"com.mytum.dev\"")
        }
        create("staging") {
            dimension = "env"
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-staging"
            buildConfigField("String", "BASE_URL", "\"https://api.staging.kipita.app/\"")
            buildConfigField("String", "FIREBASE_APP_PACKAGE", "\"com.mytum.staging\"")
        }
        create("prod") {
            dimension = "env"
            buildConfigField("String", "BASE_URL", "\"https://api.kipita.app/\"")
            buildConfigField("String", "FIREBASE_APP_PACKAGE", "\"com.mytum\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    lint {
        abortOnError = false
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kapt {
    correctErrorTypes = true
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui-text-google-fonts")
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.moshi)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(libs.work.runtime.ktx)
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    kapt(libs.androidx.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.androidx.hilt.work)
    implementation(libs.androidx.datastore.preferences)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)

    implementation(libs.hilt.navigation.compose)
    implementation(libs.androidx.datastore.preferences)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.truth)
    testImplementation(libs.kotlinx.coroutines.test)

    implementation(libs.coil.compose)
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    implementation(libs.firebase.auth)
    implementation(libs.google.maps.compose)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
