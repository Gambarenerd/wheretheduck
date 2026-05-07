# WhereTheDuck ProGuard Rules

# Firebase
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Hilt / Dagger
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Google Play Billing
-keep class com.android.vending.billing.** { *; }

# Coil
-keep class coil.** { *; }

# Data models (Firestore serialization)
-keep class com.whereduck.app.data.model.** { *; }

# Kotlin
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
