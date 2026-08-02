# --- AppCash Application & Component Protection ---
-keep class com.appcash.AppCashApplication { <init>(); }
-keep class com.appcash.ui.MainActivity { <init>(); }
-keep class com.appcash.data.model.** { *; }
-keep class com.appcash.data.repository.** { *; }

# --- Jetpack Compose Rules ---
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.material3.** { *; }
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}

# --- GSON & Retrofit Rules ---
-keepattributes Signature, InnerClasses, EnclosingMethod, *Annotation*
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# --- Kotlin Rules ---
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlin.**
-dontwarn kotlinx.**

# --- Android Standard Rules ---
-keepclassmembers class * extends android.app.Activity {
    public void *(android.view.View);
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# --- Additional Rules for Reflection ---
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers class * {
    public <init>(...);
}

# --- Keep all classes in the appcash package ---
-keep class com.appcash.** { *; }

# --- Keep all inner classes ---
-keep class com.appcash.**$* { *; }

# --- Coroutine Rules ---
-keepclassmembers class kotlinx.coroutines.internal.DispatchedContinuation {
    kotlin.coroutines.Continuation continuation;
}

# --- AndroidX Lifecycle Rules ---
-keep class androidx.lifecycle.** { *; }
-keep class * implements androidx.lifecycle.DefaultLifecycleObserver

# --- ViewModel Rules ---
-keep class androidx.lifecycle.ViewModel { *; }
-keep class * extends androidx.lifecycle.ViewModel { *; }

# --- Keep all public methods in classes that might be accessed via reflection ---
-keepclassmembers class com.appcash.** {
    public *;
}

# --- Don't warn about missing classes that might be referenced but not present ---
-dontwarn sun.misc.**
-dontwarn com.google.android.material.**
-dontwarn androidx.compose.**

# --- Keep resource classes ---
-keepclassmembers class **.R$* {
    public static <fields>;
}