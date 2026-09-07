# AndroidX / Support Libraries
-keep class androidx.** { *; }
-keep interface androidx.** { *; }
-keep public class * extends androidx.fragment.app.Fragment
-keep public class * extends androidx.activity.ComponentActivity
-keep public class * extends androidx.appcompat.app.AppCompatActivity
-keep public class * extends androidx.multidex.MultiDexApplication
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# Kotlin
-keep class kotlin.** { *; }
-keep interface kotlin.** { *; }
-keep class kotlinx.** { *; }
-keep interface kotlinx.** { *; }
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# Compose
-keep class androidx.compose.** { *; }
-keep interface androidx.compose.** { *; }
-keep class androidx.compose.runtime.internal.** { *; }
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.material3.** { *; }
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}

# Coroutines
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-keepclassmembers class kotlinx.coroutines.internal.** {
    volatile <fields>;
}
-keepclassmembers class kotlinx.coroutines.channels.** {
    volatile <fields>;
}
-keep class kotlinx.coroutines.** { *; }

# Serialization / JSON
-keep class * implements java.io.Serializable { *; }
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# JNI / Native
-keepclassclasseswithmembers class * {
    native <methods>;
}
-keepclasseswithmembers class io.stardew.** {
    native <methods>;
}

# Application packages
-keep class io.stardew.** { *; }
-keepclassmembers class io.stardew.** {
    public <fields>;
    public <methods>;
}

# Enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Parcelable
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# View / WebView (if any)
-keepclassmembers class * extends android.webkit.WebViewClient {
    public void *(android.webkit.WebView, java.lang.String, android.graphics.Bitmap);
    public boolean *(android.webkit.WebView, java.lang.String);
}
-keepclassmembers class * extends android.webkit.WebChromeClient {
    public void *(android.webkit.WebView, int);
}

# Logging (optional – keep if needed)
-keepclassmembers class io.stardew.utils.Logger {
    public *** d(...);
    public *** i(...);
    public *** w(...);
    public *** e(...);
}

# Remove verbose logging for release (if you want)
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
}
-assumenosideeffects class io.stardew.utils.Logger {
    public static *** d(...);
    public static *** v(...);
}
