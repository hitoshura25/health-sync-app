# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in the Android SDK.

# Keep line numbers for debugging stack traces
-keepattributes SourceFile,LineNumberTable

# Hide the original source file name
-renamesourcefileattribute SourceFile

# Keep data classes and their fields
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}

# Keep Parcelables
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep custom views
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Room Database Rules
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Hilt Rules
-dontwarn com.google.errorprone.annotations.**
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Kotlinx Serialization Rules
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class io.github.hitoshura25.healthsyncapp.**$$serializer { *; }
-keepclassmembers class io.github.hitoshura25.healthsyncapp.** {
    *** Companion;
}
-keepclasseswithmembers class io.github.hitoshura25.healthsyncapp.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Avro4k Rules
-keep class com.github.avrokotlin.avro4k.** { *; }
-keepclassmembers class * {
    @com.github.avrokotlin.avro4k.AvroName *;
    @com.github.avrokotlin.avro4k.AvroNamespace *;
}

# WorkManager Rules
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.ListenableWorker {
    public <init>(...);
}
-keep class androidx.work.impl.WorkDatabase { *; }
-keep class androidx.work.impl.model.WorkSpec { *; }

# Health Connect Client Rules
-keep class androidx.health.connect.client.** { *; }
-dontwarn androidx.health.connect.client.**

# Compose Rules (already handled by Compose compiler, but included for completeness)
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Apache Avro optional compression codecs (not used, can be safely ignored)
-dontwarn com.github.luben.zstd.**
-dontwarn org.tukaani.xz.**
-dontwarn org.xerial.snappy.**
-dontwarn org.slf4j.impl.StaticLoggerBinder

# AndroidX Test / UI Automator (needed for release build testing)
-keep class androidx.test.** { *; }
-keep class androidx.tracing.** { *; }
-dontwarn androidx.test.**
-dontwarn org.junit.**
-dontwarn junit.**

# Truth library and its Guava dependencies
-keep class com.google.common.truth.** { *; }
-keep class com.google.common.collect.** { *; }
-keep class com.google.common.base.** { *; }
-dontwarn com.google.common.**

# Kotlin stdlib - required for AndroidX Test framework
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
