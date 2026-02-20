plugins {
    alias(libs.plugins.android.test)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.kipita.baselineprofile"
    compileSdk = 36

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
        }
    }

    defaultConfig {
        minSdk = 28
        targetSdk = 36
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Points to the app module to generate baseline profiles for
    targetProjectPath = ":app"
    experimentalProperties["android.experimental.self-instrumenting"] = true

    // Mirror the app's product flavors so profiles are generated per flavor
    flavorDimensions += "env"
    productFlavors {
        create("dev") { dimension = "env" }
        create("staging") { dimension = "env" }
        create("prod") { dimension = "env" }
    }
}

dependencies {
    implementation(libs.benchmark.macro.junit4)
    implementation(libs.androidx.test.ext.junit)
    implementation(libs.uiautomator)
}
