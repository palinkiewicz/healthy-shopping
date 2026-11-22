# Common ProGuard rules for this project

# Kotlinx Serialization
-keepattributes *Annotation*, EnclosingMethod, Signature
-keep,allowobfuscation,allowshrinking class kotlinx.serialization.json.** { *; }
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}

# Retrofit & OkHttp
-keepattributes Signature, InnerClasses, AnnotationDefault
-keepclassmembers,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.Platform$Java8
-dontwarn retrofit2.Platform$IOUtil
-keepnames class retrofit2.Response
-keep class retrofit2.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# App specific models (prevent stripping JSON data classes)
-keep class pl.dakil.healthyshopping.data.model.** { *; }

# ML Kit (via firebase-components)
# ML Kit discovers its ComponentRegistrar classes (BarcodeRegistrar, VisionCommonRegistrar,
# CommonComponentRegistrar) from manifest metadata and instantiates them reflectively through
# their no-arg constructor. firebase-components only ships `-keep class * implements ComponentRegistrar`
# without members; since AGP 9 (android.r8.strictFullModeForKeepRules=true) that no longer keeps the
# default constructor, so the scanner crashed as soon as BarcodeScanning.getClient() ran.
-keep class * implements com.google.firebase.components.ComponentRegistrar {
    <init>();
}
