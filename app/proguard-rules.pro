# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Gson specific rules
-keep class com.google.gson.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# Keep generic types for Gson TypeToken
-keep class com.google.gson.reflect.TypeToken {
    <fields>;
    <methods>;
}

# Keep your model classes with the fields
-keep class com.pshealthcare.customer.app.models.** { *; }

# Preserve generic type information for Gson
-keepattributes Signature

# Keep Gson TypeAdapter, TypeAdapterFactory, and JsonSerializer classes
-keep class com.google.gson.** { *; }
-keep class * extends com.google.gson.TypeAdapter
-keep class * extends com.google.gson.TypeAdapterFactory
-keep class * extends com.google.gson.JsonSerializer

# Keep classes with TypeToken usage
-keep class * extends com.google.gson.reflect.TypeToken
