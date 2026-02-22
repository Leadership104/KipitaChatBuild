pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }

    // Allows module-level `id("org.jetbrains.kotlin.plugin.compose")` usage
    // without requiring an inline version declaration.
    plugins {
        id("org.jetbrains.kotlin.plugin.compose") version "2.2.10"
    }
}
// foojay-resolver removed: use JAVA_HOME / org.gradle.java.home instead

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Kipita"
include(":app")
include(":baselineprofile")
