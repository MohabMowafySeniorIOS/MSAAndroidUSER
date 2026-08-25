# MSA — Android (Jetpack Compose)

تطبيق MSA لأسعار الذهب والفضة والدولار، نسخة Android كاملة مطابقة للـ iOS.

## التقنيات
- **Kotlin** + **Jetpack Compose** + **Material 3**
- **MVVM** + **Clean Architecture** (ui / data / domain / di)
- **Hilt** dependency injection
- **Firebase Firestore** (realtime listeners via Kotlin Flow)
- **Firebase Cloud Messaging** (push notifications)
- **DataStore Preferences** (language & onboarding flags)
- **Compose Navigation**
- **Coil** (image loading)
- **WebKit** (in-app NetDania chart)
- **RTL Arabic** support + **dark theme** by default
- **IBM Plex Sans Arabic** font family (matching iOS)

## بنية المشروع
```
app/src/main/java/com.msa.android/
├── MSAApplication.kt          # Hilt + FCM topics + notification channel
├── MainActivity.kt            # Locale-aware activity host
│
├── di/                        # Hilt modules
│   ├── FirebaseModule.kt
│   ├── RepositoryModule.kt
│   └── LocalPreferencesModule.kt
│
├── domain/
│   ├── model/                 # Pure data classes (Metal, BankRate, FaqItem, ...)
│   ├── repository/            # Repository interfaces
│   └── usecase/               # PriceCalculator, ZakatCalculator, GoldValueCalculator, SilverValueCalculator
│
├── data/
│   ├── source/
│   │   ├── firebase/          # FirestoreExtensions (Flow), FirestoreConstants
│   │   └── local/             # LanguagePreferences (DataStore)
│   └── repository/            # Firestore implementations
│
└── presentation/
    ├── theme/                 # Colors, typography, MSATheme
    ├── common/                # NumberFormatter, RelativeTimeFormatter
    ├── common/components/     # Reusable components (MSABackground, MSATopBar, MSABottomBar, ...)
    ├── navigation/            # MSANavGraph
    ├── service/               # FCM service
    └── screens/
        ├── splash/
        ├── language/
        ├── onboarding/
        ├── main/              # Tab host
        ├── home/              # الشاشة العالمية (gold/silver)
        ├── banks/             # أسعار العملات
        ├── calculator/        # The grid
        ├── calculators/
        │   ├── goldvalue/
        │   ├── goldzakat/
        │   ├── silvervalue/
        │   ├── silverzakat/
        │   └── bullions/
        ├── more/
        ├── help/
        ├── faq/
        ├── about/
        ├── policy/            # shared for privacy/terms/refund
        └── webview/           # NetDania chart
```

## التشغيل

### 1. الـ Firebase
- استبدل `app/google-services.json` بـ ملف ال Firebase بتاعك (نفس الـ project اللي بيستخدمه iOS).
- تأكد إن `applicationId` في `app/build.gradle.kts` (حالياً `com.msa.android`) متطابق مع اللي في الـ Firebase project.

### 2. الـ Build
- افتح المشروع بـ **Android Studio Hedgehog (2023.1.1)** أو أحدث.
- Sync Gradle (هينزل كل الـ dependencies تلقائيًا).
- Build → Make Project.

### 3. التشغيل
- Run على أي device بـ `minSdk 24` أو أحدث.

## بنية Firestore المتوقعة (مطابقة لـ iOS)
- `metals/` (وثائق فيها `type`, `name`, `buyPrice`, `salePrice`, `updatedAt`)
  - مع وثيقة `ounce_price` فيها `goldPrice`, `silverPrice`
- `banks/` (وثائق فيها `name`, `buy`, `sell`, `logo`, `date`, `trend`)
  - مع وثيقة `central_bank` لسعر الدولار
- `FAQ/` (وثائق فيها `question_ar`, `question_en`, `answer_ar`, `answer_en`)
- `pages/` (وثائق `about_us`, `privacy_policy`, `usage_policy`, `refund_policy`, `appVersion`)
- `on_boarding/` (وثائق فيها `image`, `title_ar/en`, `description_ar/en`, `order`)
- `ContactUs/` (وثائق مكتوبة من فورم المساعدة)
- `bullions/` (وثائق فيها `karat`, `company`, `product`, `price`, `image`, `type`)

## FCM Topics المشترك فيها
- `gold_ar`, `silver_ar`, `gold_en`, `silver_en`, `dollar_prices`, `news_ar`

## معادلات الحساب (نفس الـ iOS)
- **Gold base = 21K**: `karat24 = base × 24/21`, `karat18 = base × 18/21`, `pound = base × 8`, `kilo = base × 1000`
- **Silver base = 800**: `karat999 = base × 999/800`, `karat925 = base × 925/800`, `kilo = base × 1000`
- **Gold Zakat**: نصاب 85g (24K), يعادل كل العيارات لـ 24K, زكاة 2.5%
- **Silver Zakat**: نصاب 595g (999), يعادل كل العيارات لـ 999, زكاة 2.5%
- **دولار الصاغة** = `karat24Buy ÷ (ouncePrice / 31.1035)`

## ملاحظات
- الخطوط (IBM Plex Sans Arabic) موجودة فعلياً في `res/font/`.
- كل الـ drawables vector XMLs (تقدر تستبدلها بصور حقيقية لو حابب).
- مفيش XML layouts — كله Compose.
- مفيش deprecated APIs.
- المشروع كله بيدعم Dark Theme و RTL Arabic by default.

## التطوير
طورت بـ Mohab Mowafy. ❤️
