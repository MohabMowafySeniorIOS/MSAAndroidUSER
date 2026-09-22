# سلايدر البانر — النسخة الأندرويد

ترجمة كاملة لسلايدر الرئيسية بتاع الـ iOS (`BannerSliderView` / `BannerCell` /
`BannerService` / `HomeVC+Banner`) لـ Jetpack Compose.

## الملفات

| الملف | مقابله في iOS |
|---|---|
| `BannerModels.kt` | `BannerModel.swift` |
| `BannerApi.kt` + `BannerRepository.kt` | `BannerService.swift` |
| `BannerModule.kt` | (مفيش — ده Hilt wiring) |
| `BannerViewModel.kt` | (مفيش — الـ iOS بينده الـ service من الـ VC) |
| `BannerSlider.kt` | `BannerSliderView.swift` + `BannerCell.swift` |
| `BannerPopup.kt` | (جديد — الـ popup بتاع نص الشاشة) |
| `BannerColors.kt` | ألوان البراند من `Colors.ai` |
| `BannerFullScreen.kt` | `openImageViewer` / `openVideoPlayer` في `HomeVC+Banner.swift` |

---

## خطوات الدمج (٤ خطوات)

### ١. حط الفولدر في مكانه

```
app/src/main/java/com/msa/android/presentation/screens/home/
├── HomeScreen.kt        ← النسخة المعدّلة الموجودة معاك
├── HomeViewModel.kt     ← زي ما هي، متغيّرتش
└── banner/              ← الفولدر ده كله جديد
```

لو الباكدج بتاعك مختلف، غيّر سطر `package` في أول كل ملف.

### ٢. الـ Gradle

```kotlin
dependencies {
    // Pager موجود في foundation نفسه (لازم 1.6.0 أو أحدث)
    implementation("androidx.compose.foundation:foundation:1.6.0")

    // الصور
    implementation("io.coil-kt:coil-compose:2.6.0")

    // الفيديو
    implementation("androidx.media3:media3-exoplayer:1.3.1")
    implementation("androidx.media3:media3-ui:1.3.1")

    // LifecycleResumeEffect + collectAsStateWithLifecycle
    // ⚠️ لازم 2.8.0 أو أحدث — الأوفرلود بتاعة LifecycleResumeEffect
    //    اللي بتاخد key اتضافت في 2.8.0
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

    // موجودين عندك أصلاً على الأغلب
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
}
```

وفي `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

> **لو عندك Coil 3** بدّل `coil.compose.AsyncImage` بـ `coil3.compose.AsyncImage`
> في `BannerSlider.kt` و `BannerFullScreen.kt`.

### ٣. اربط الـ API بالـ Retrofit بتاعك ⚠️ (أهم خطوة)

الإندبوينت الحقيقي: **`https://api.msagold.com/api/v1/banners`**

`BannerModule.kt` بيعمل Retrofit خاص بيه على الـ base URL ده عشان الكود
يشتغل لوحده. **إنت عندك Retrofit متظبط أصلاً**، فالمفروض تمسح الموديول
ده كله وتضيف السطرين دول في الـ `NetworkModule` بتاعك:

```kotlin
@Provides
@Singleton
fun provideBannerApi(retrofit: Retrofit): BannerApi =
    retrofit.create(BannerApi::class.java)
```

كده البانر هيستخدم نفس الـ baseUrl والـ interceptors والتوكن بتوع باقي
التطبيق. وبعدين ظبّط المسار في `BannerApi.kt` حسب الـ baseUrl عندك:

| الـ baseUrl بتاعك | المسار في `@GET` |
|---|---|
| `https://api.msagold.com/api/v1/` | `"banners"` ← الحالي |
| `https://api.msagold.com/api/` | `"v1/banners"` |
| `https://api.msagold.com/` | `"api/v1/banners"` |

### ٤. خلاص

`HomeScreen.kt` فيها السطر ده كأول حاجة جوه الـ `Box`:

```kotlin
HomeBannerPopupHost(
    onOpenLink = { url -> /* افتح الويب فيو بتاعك */ }
)
```

مش محتاج أي حاجة تانية — الـ ViewModel بيتحقن لوحده بـ `hiltViewModel()`،
والـ popup بيتفتح في نافذة منفصلة فمالوش أي تأثير على ترتيب المحتوى تحته.

**فاضل حاجة واحدة:** املا الـ `TODO` بتاع `onOpenLink` بطريقة فتح
الويب فيو عندك — من غيرها زرار "اعرف أكثر" هيقفل الـ popup وبس.

---

## الـ response الحقيقي

الموديل متظبط على اللي راجع فعلاً من الـ API:

```json
{
  "meta": { "current_page": 1, "per_page": 15, "total": 1 },
  "data": [{
    "id": 4,
    "name": "MSA ",
    "media_type": "image",
    "media_url":  "https://api.msagold.com/storage/14/01M18....jpeg",
    "thumb_url":  "https://api.msagold.com/storage/14/conversions/...-thumb.jpg",
    "order": 0,
    "starts_at": null, "ends_at": null,
    "link":   { "type": "none",   "url": null },
    "status": { "value": "active", "label": { "ar": "نشط", "en": "Active" } }
  }],
  "links": { "next": null, "prev": null }
}
```

اللي اتظبط عشانه:

| الحالة | التصرّف |
|---|---|
| `link.type = "none"` | `linkUrl = null` → زرار "اعرف أكثر" مبيظهرش خالص |
| `name = "MSA "` | بيتعمله trim قبل العرض |
| `starts_at` / `ends_at` | بتتفكّ لـ epoch ms (ISO-8601 بالميكروثانية UTC) |
| `status.value` | البانر مش `active` مبيتعرضش |
| `per_page = 15` | بنطلب `per_page=50` عشان كل البانرات تيجي في ريكوست واحد |
| بانر واحد بس | النقط مبتظهرش، ومفيش تقليب — كارت ثابت |

الفلترة المحلية (`activeNow()`) بتشتغل على اللي راجع من الشبكة. أي حقل
ناقص مبنفلترش بيه، عشان بانر بحقول ناقصة ما يختفيش بالغلط.

## شكل الـ popup

```
                    ✕     ← زرار إغلاق فوق الكارت
 ┌──────────────────────┐
 │      ميديا 16:9      │  ← سلايدر: صورة أو فيديو + عنوان + نقط ذهبية
 └──────────────────────┘
 [      اعرف أكثر      ]  ← زرار بتدرج الكريمي→الذهبي (يظهر لو فيه لينك بس)
```

- **عرض الشاشة كامل ناقص ١٦ يمين وشمال**، والارتفاع نسبة **16:9** من
  العرض. غيّرها من `aspectRatio` في `BannerPopup.kt` — القيمة هي
  العرض ÷ الارتفاع، يعني `1f` لمربع و `3f/4f` لطولي.
- بيدخل بأنيميشن scale + fade (spring) بدل ما ينطّ فجأة.
- الضغط على الخلفية أو زرار الرجوع بيقفل.
- الضغط على الميديا: لو فيه لينك بيفتحه، لو مفيش بيفتحها بملء الشاشة.

## الظهور

**مرة واحدة كل تشغيلة للتطبيق.** مفيش سياسات ولا إعدادات — الـ ViewModel
بينده الـ API أول ما الرئيسية تتكوّن، وأول ما البانرات توصل الـ popup
بيظهر.

اللي بيمنع التكرار هو فلاج `shownThisLaunch` في `BannerRepository` —
في الذاكرة على مستوى الـ process، **مش** في SharedPreferences:

| | |
|---|---|
| فتح التطبيق | يظهر ✅ |
| راح لتاب تاني ورجع للرئيسية | مبيظهرش ✅ |
| التطبيق راح للخلفية ورجع | مبيظهرش ✅ |
| قفل التطبيق خالص وفتحه | يظهر تاني ✅ |

عايزه يظهر كل مرة الرئيسية تتفتح مش كل فتحة تطبيق؟ خلّي
`shouldShowPopup` ترجّع `items.isNotEmpty()` وبس.

## مفيش كاش

كل مرة بنسأل الـ API من جديد. يعني لو الداشبورد وقّفت بانر أو نشرت واحد
جديد، المستخدم بيشوف الجديد من أول فتحة — مش بعد ما الكاش يقدم.

الثمن: **مفيش بانر بيظهر وهو أوفلاين**. مقبول لأن البانر إعلان مش محتوى
أساسي، ولو الريكوست فشل الرئيسية بتشتغل عادي من غيره.

## السلوك (مطابق للـ iOS)

| | |
|---|---|
| الصورة | تفضل ٥ ثواني وبعدين يقلب |
| الفيديو | يلوب لحد ما يخلص، والقلبة بتحصل بعد مدته الحقيقية (بحد أقصى ٢٠ ثانية) |
| صوت البانر | مكتوم دايماً، ومش بياخد الفوكس الصوتي فالأغنية اللي المستخدم سامعها متوقفش |
| الغلاف | `thumb_url` بيفضل ظاهر تحت الفيديو لحد أول فريم — مفيش مستطيل أسود |
| شارة الفيديو | فوق على الشمال، بتبان من أول لحظة وبعدين المدة تظهر جنبها |
| الخلفية / الخروج من الشاشة | كل حاجة بتقف (`LifecycleResumeEffect`) |
| المستخدم بيقلب بإيده | التايمر بيقف مؤقتاً وبيرجع لما يسيبه |
| الضغط على صورة | فل سكرين بزووم من 1x لـ 6x + دبل تاب |
| الضغط على فيديو | فل سكرين **بالصوت** وبأزرار تحكم كاملة |
| مفيش بانرات | الـ popup مبيظهرش خالص |
| الشبكة وقعت | مفيش popup (مفيش كاش نرجع له) |

## ملاحظات

- **الاتجاه:** السلايدر متثبّت على LTR جوّه (`LocalLayoutDirection`) — زي
  `semanticContentAttribute = .forceLeftToRight` في الـ iOS — عشان ترتيب
  البانرات اللي جاي من الداشبورد ما يتقلبش في الواجهة العربية. العنوان
  جوه البانر بيبقى على الشمال بسبب كده، بالظبط زي الـ iOS. لو عايزه على
  اليمين، شيل الـ `CompositionLocalProvider` من `BannerSlider`.

- **عايزه شريط جوه المحتوى بدل الـ popup؟** `HomeBannerSection` لسه
  موجودة في `BannerSlider.kt` — نادِها من جوه الـ `Column` اللي فيه
  `verticalScroll` وهتشتغل زي ما هي (وتقدر تستخدم الاتنين مع بعض).

- **الألوان:** `BannerColors.kt` فيه ألوان البراند محلياً عشان الفولدر
  يشتغل لوحده. لما تنقلها لـ `Theme.kt` امسح الملف وبدّل الأسماء.

- **الفلترة:** بنفلتر محلياً بـ `status` و `starts_at`/`ends_at`. لو
  عايز تعرض كل اللي راجع من غير فلترة، شيل `.activeNow()` من
  `BannerRepository.fetch()`.

- **أكتر من ١٥ بانر:** بنطلب `per_page=50`. لو عدّيت العدد ده فعلاً
  هتحتاج تلف على `links.next` — الـ DTO بتاعها موجود جاهز في
  `BannerModels.kt` (`BannerPageLinksDto`).

- **لو مش قادر ترفّع lifecycle لـ 2.8:** بدّل الجزء ده في `BannerSlider.kt`

  ```kotlin
  var isResumed by remember { mutableStateOf(false) }
  LifecycleResumeEffect(Unit) {
      isResumed = true
      onPauseOrDispose { isResumed = false }
  }
  ```

  بده (شغال على أي إصدار):

  ```kotlin
  val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
  var isResumed by remember { mutableStateOf(false) }
  DisposableEffect(lifecycleOwner) {
      val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
          when (event) {
              androidx.lifecycle.Lifecycle.Event.ON_RESUME -> isResumed = true
              androidx.lifecycle.Lifecycle.Event.ON_PAUSE  -> isResumed = false
              else -> Unit
          }
      }
      lifecycleOwner.lifecycle.addObserver(observer)
      onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }
  ```

- **أولوية اللينك:** في `HomeBannerSection` فيه `linkTakesPriority = false`
  — نفس المتغيّر الموجود في `HomeVC+Banner.swift`. خلّيه `true` لو قررت
  إن البانر المفروض يودّي على صفحة بدل ما يفتح الميديا، وابعت
  `onOpenLink` من `HomeScreen`.
