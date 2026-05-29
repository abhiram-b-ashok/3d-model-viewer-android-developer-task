# --- Filament -------------------------------------------------------------
# Filament uses JNI extensively. Native code dereferences the Java fields and
# methods of these classes by name, so R8/ProGuard must not rename or strip
# them or the renderer will crash at runtime.
-keep class com.google.android.filament.** { *; }
-keep class com.google.android.filament.utils.** { *; }
-keep class com.google.android.filament.gltfio.** { *; }
-keep class com.google.android.filament.android.** { *; }
-keepclasseswithmembernames class com.google.android.filament.** {
    native <methods>;
}

# Filament's NioUtils / direct ByteBuffer reflection.
-dontwarn java.nio.**
-dontwarn sun.misc.**

# Keep line numbers for crash reports from native code.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
