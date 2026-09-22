import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.kotlin.plugin.parcelize")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.msa.android"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.msa.android"
        minSdk = 26
        // Google Play: من 31 أغسطس 2026 لازم استهداف Android 16 (API 36)
        // للتحديثات الجديدة — والتمديد بينتهي 1 نوفمبر 2026.
        targetSdk = 36
        versionCode = 13
        versionName = "1.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }

        androidResources {
            localeFilters += listOf("en", "ar")
        }

        // مفتاح الخرايط من local.properties — مش بيتحط في git ولا في
        // الكود المصدري. لو مش موجود بنحط قيمة فاضية: التطبيق بيبني
        // عادي والخريطة بس هي اللي مش هتظهر.
        manifestPlaceholders["MAPS_API_KEY"] =
            Properties().apply {
                rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
            }.getProperty("MAPS_API_KEY", "")
    }

    signingConfigs {
        create("release") {
            storeFile = file("../msa-release-key.jks")
            storePassword = "p@ssword3344"
            keyAlias = "msa"
            keyPassword = "p@ssword3344"
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")

            isMinifyEnabled = false

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

   implementation("androidx.core:core-ktx:1.13.1")

implementation("androidx.appcompat:appcompat:1.7.0")

implementation("com.google.android.material:material:1.12.0")

implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")

implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.4")

implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.4")

implementation("androidx.activity:activity-compose:1.9.1")

implementation("androidx.core:core-splashscreen:1.0.1")

implementation(platform("androidx.compose:compose-bom:2024.08.00"))

implementation("androidx.compose.ui:ui")

implementation("androidx.compose.ui:ui-graphics")

implementation("androidx.compose.ui:ui-tooling-preview")

implementation("androidx.compose.material3:material3")

implementation("androidx.compose.material:material-icons-extended")

implementation("androidx.navigation:navigation-compose:2.7.7")

implementation("com.google.dagger:hilt-android:2.51.1")

ksp("com.google.dagger:hilt-android-compiler:2.51.1")

implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

implementation(platform("com.google.firebase:firebase-bom:33.1.2"))

implementation("com.google.firebase:firebase-analytics-ktx")

implementation("com.google.firebase:firebase-firestore-ktx")

// Media3 ExoPlayer — for video playback in BannerFullScreen
val media3Version = "1.4.1"
implementation("androidx.media3:media3-exoplayer:$media3Version")
implementation("androidx.media3:media3-ui:$media3Version")
implementation("androidx.media3:media3-common:$media3Version")

// QR scanner — CameraX for the live preview, ML Kit's BUNDLED barcode model
// for decoding. We pick the bundled variant (not play-services-mlkit-barcode-
// scanning) so:
//   • Scanning works the very first time without waiting on a model download.
//   • The scanner works on devices without Google Play Services (emulators,
//     dev devices, MIUI/EMUI without GMS).
//
// The trade-off is the 16 KB alignment warning on Android 15 — the app still
// runs fine, just in page-size-compat mode. CameraX 1.4.2+ IS 16 KB aligned;
// only `libbarhopper_v3.so` (from the bundled barcode model) is not yet.
implementation("androidx.camera:camera-core:1.4.2")
implementation("androidx.camera:camera-camera2:1.4.2")
implementation("androidx.camera:camera-lifecycle:1.4.2")
implementation("androidx.camera:camera-view:1.4.2")
implementation("com.google.mlkit:barcode-scanning:17.3.0")
implementation("androidx.concurrent:concurrent-futures-ktx:1.2.0")

implementation("com.google.firebase:firebase-messaging-ktx")

implementation("com.google.firebase:firebase-config-ktx")

implementation("androidx.datastore:datastore-preferences:1.1.4")

implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")

implementation("com.squareup.okhttp3:okhttp:4.12.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

// Retrofit + Moshi — needed for the MSA news REST API. The endpoint
// `https://msagold.com/api/v1/news` returns deeply nested JSON (title/desc
// localized into `ar`/`en` keys) so we need a real JSON deserializer instead
// of poking at JSONObject by hand.
implementation("com.squareup.retrofit2:retrofit:2.11.0")
implementation("com.squareup.retrofit2:converter-moshi:2.11.0")
implementation("com.squareup.retrofit2:converter-gson:2.11.0")
implementation("com.squareup.moshi:moshi-kotlin:1.15.1")

implementation("io.coil-kt:coil-compose:2.7.0")

// ── الفروع: الخريطة داخل التطبيق ────────────────────────────────────────
// maps-compose هي غلاف Compose الرسمي حوالين Google Maps SDK.
//
// محتاجة مفتاح API في `local.properties` باسم MAPS_API_KEY — المفتاح
// مش في الكود ولا في git (local.properties متجاهَل)، وبيتحقن في
// AndroidManifest وقت البناء عن طريق manifestPlaceholders تحت.
//
// من غير مفتاح، شاشة تفاصيل الفرع بتعرض مكان الخريطة فاضي وباقي
// الشاشة (الاتجاهات، الاتصال، الواتساب) بتفضل شغالة عادي.
implementation("com.google.maps.android:maps-compose:4.4.1")
implementation("com.google.android.gms:play-services-maps:19.0.0")

// Google AdMob — banner ads on the Home screen.
// Default IDs are Google's official test IDs (safe to ship for development).
// Replace with real IDs from admob.google.com before publishing.
implementation("com.google.android.gms:play-services-ads:23.5.0")

implementation("com.google.code.gson:gson:2.11.0")
implementation("org.json:json:20240303")

// ── QR scanning ──────────────────────────────────────────────────────────
testImplementation("junit:junit:4.13.2")
}
