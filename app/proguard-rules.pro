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

############################################
# GENERAL
############################################

# Keep annotations (important for Gson & Firebase)
-keepattributes *Annotation*

# Keep generic type info (needed for Gson)
-keepattributes Signature

############################################
# GSON (Very Important)
############################################

# Keep model classes (change package if needed)
-keep class com.abmn.englishhub.** { *; }

# Keep Gson TypeToken
-keep class com.google.gson.reflect.TypeToken { *; }

# Keep fields with @SerializedName
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

############################################
# FIREBASE CLOUD MESSAGING
############################################

-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

############################################
# GOOGLE MOBILE ADS (ADMOB)
############################################

-keep class com.google.android.gms.ads.** { *; }
-dontwarn com.google.android.gms.ads.**

-keep public class com.google.android.gms.common.** { *; }
-dontwarn com.google.android.gms.common.**

############################################
# VOLLEY
############################################

-dontwarn com.android.volley.**

############################################
# TEXT TO SPEECH
############################################

-dontwarn android.speech.tts.**

############################################
# NAVIGATION COMPONENT
############################################

-keep class androidx.navigation.** { *; }

############################################
# REMOVE LOGS IN RELEASE (Optional but Recommended)
############################################

-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
