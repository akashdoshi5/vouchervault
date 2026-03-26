# Proguard rules for VoucherVault

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Keep Room entities
-keep class com.addmrp.vault.data.local.entity.** { *; }

# Keep Firebase models
-keepclassmembers class com.addmrp.vault.data.remote.** { *; }

# Keep Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Keep ML Kit
-keep class com.google.mlkit.** { *; }
