# Proguard rules for PrayerReminder

-keepattributes *Annotation*
-keepclassmembers class * {
    @org.jetbrains.annotations.* <fields>;
    @org.jetbrains.annotations.* <methods>;
}

# kotlinx.serialization
-keepattributes *Annotation*,InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keepclassmembers class **$$serializer {
    public static final **$$serializer INSTANCE;
}
-keepclasseswithmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}

# Domain & Data models (keep for serialization and reflection)
-keep class com.syedmsidsm.prayerreminder.domain.model.** { *; }
-keep class com.syedmsidsm.prayerreminder.reminders.ScheduledReminderEvent { *; }
-keep class com.syedmsidsm.prayerreminder.reminders.ReminderKind { *; }

# ML Kit Text Recognition
-keep class com.google.mlkit.** { *; }

# Glance & AppWidgets
-keep class androidx.glance.** { *; }
-keep class com.syedmsidsm.prayerreminder.widget.** { *; }
