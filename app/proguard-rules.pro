# Keep Firebase classes
-keep class com.google.firebase.** { *; }
-keep class com.msa.android.data.model.** { *; }
-keep class com.msa.android.domain.model.** { *; }

# Hilt
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper
-keep class dagger.hilt.** { *; }

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
