# Add project specific ProGuard rules here.
-keepattributes *Annotation*
-keepclassmembers class kotlinx.serialization.json.** { *; }
-keep,includedescriptorclasses class com.postiz.mobile.**$$serializer { *; }
-keepclassmembers class com.postiz.mobile.** { *** Companion; }
-keepclasseswithmembers class com.postiz.mobile.** { kotlinx.serialization.KSerializer serializer(...); }
