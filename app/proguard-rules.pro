# ProGuard rules for BrainDrop
-keep public class * { public protected *; }
-keepclassmembers class * { @com.google.gson.annotations.SerializedName <fields>; }
-keep class io.objectbox.** { *; }
-keep class brain.drop.db.** { *; }
-dontwarn io.objectbox.**
