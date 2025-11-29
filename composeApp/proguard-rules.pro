# Keep Room database/DAO/entities and KSP generated entry points
-keep class com.fakhry.pomodojo.core.database.** { *; }
-keep class com.fakhry.pomodojo.core.framework.notifications.** { *; }
# R8 missing-class hints (safe to keep/don't-warn)
-dontwarn com.fakhry.pomodojo.core.database.**
# Koin modules are referenced reflectively
-keepclassmembers class * {
    @org.koin.core.annotation.* <methods>;
}
