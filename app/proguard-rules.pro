# =============================================================================
# Kipita ProGuard / R8 Rules
# =============================================================================

# ---------------------------------------------------------------------------
# Kotlin
# ---------------------------------------------------------------------------
-keepattributes *Annotation*, InnerClasses, EnclosingMethod, Signature, Exceptions
-keep class kotlin.Metadata { *; }
-keepclassmembers class **$WhenMappings { *; }
-keepclassmembers class kotlin.Lazy { *; }
-dontwarn kotlin.**

# ---------------------------------------------------------------------------
# Kipita data models — Room entities, domain models, API DTOs
# Must be kept so Room and Moshi can access them by name at runtime.
# ---------------------------------------------------------------------------
-keep class com.kipita.data.local.** { *; }
-keep class com.kipita.domain.model.** { *; }
-keep class com.kipita.data.api.** { *; }
-keep class com.kipita.BuildConfig { *; }

# ---------------------------------------------------------------------------
# Room
# ---------------------------------------------------------------------------
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase { *; }
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**

# ---------------------------------------------------------------------------
# Hilt / Dagger
# ---------------------------------------------------------------------------
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-dontwarn dagger.hilt.**

# ---------------------------------------------------------------------------
# Moshi (JSON serialisation for Retrofit DTOs)
# ---------------------------------------------------------------------------
-keep class com.squareup.moshi.** { *; }
-keepclassmembers class ** {
    @com.squareup.moshi.FromJson *;
    @com.squareup.moshi.ToJson *;
}
-keep @com.squareup.moshi.JsonClass class * { *; }
-dontwarn com.squareup.moshi.**

# ---------------------------------------------------------------------------
# Retrofit + OkHttp
# ---------------------------------------------------------------------------
-keep class retrofit2.** { *; }
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# ---------------------------------------------------------------------------
# SQLCipher
# ---------------------------------------------------------------------------
-keep class net.sqlcipher.** { *; }
-keep class net.sqlcipher.database.** { *; }
-dontwarn net.sqlcipher.**

# ---------------------------------------------------------------------------
# Firebase / Google Play Services
# ---------------------------------------------------------------------------
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# ---------------------------------------------------------------------------
# Google Generative AI (Gemini) SDK
# ---------------------------------------------------------------------------
-keep class com.google.ai.client.generativeai.** { *; }
-keep class com.google.protobuf.** { *; }
-dontwarn com.google.ai.client.**
-dontwarn com.google.protobuf.**

# ---------------------------------------------------------------------------
# Coil (image loading)
# ---------------------------------------------------------------------------
-keep class coil.** { *; }
-dontwarn coil.**

# ---------------------------------------------------------------------------
# WorkManager
# ---------------------------------------------------------------------------
-keep class androidx.work.** { *; }
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.CoroutineWorker
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-dontwarn androidx.work.**

# ---------------------------------------------------------------------------
# Kotlin Coroutines
# ---------------------------------------------------------------------------
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# ---------------------------------------------------------------------------
# Jetpack Compose
# ---------------------------------------------------------------------------
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# ---------------------------------------------------------------------------
# Crashlytics / Firebase Performance
# ---------------------------------------------------------------------------
-keepattributes SourceFile, LineNumberTable
-keep public class * extends java.lang.Exception
-keep class com.google.firebase.crashlytics.** { *; }

# ---------------------------------------------------------------------------
# DataStore
# ---------------------------------------------------------------------------
-keep class androidx.datastore.** { *; }
-dontwarn androidx.datastore.**

# ---------------------------------------------------------------------------
# General Android / Lifecycle
# ---------------------------------------------------------------------------
-keep class androidx.lifecycle.** { *; }
-keep class androidx.navigation.** { *; }
-dontwarn androidx.lifecycle.**

# ---------------------------------------------------------------------------
# Suppress noisy warnings from transitive dependencies
# ---------------------------------------------------------------------------
-dontwarn javax.annotation.**
-dontwarn sun.misc.Unsafe
-dontwarn java.lang.instrument.**
