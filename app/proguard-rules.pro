# ProGuard rules for DeepSeekMonitorAndroid

# ── Retrofit / OkHttp ──
-keepattributes Signature
-keepattributes Exceptions
-keep class com.deepseek.monitor.data.remote.dto.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# ── Gson ──
-keep class com.google.gson.** { *; }
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# ── Room ──
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# ── Hilt ──
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
