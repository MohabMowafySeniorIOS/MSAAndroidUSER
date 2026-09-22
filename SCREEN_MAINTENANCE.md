# صيانة الشاشات — `appVersion/appVersion`

قفل شاشة واحدة من لوحة Firebase من غير ما تنزّل نسخة جديدة.
المستند: مجموعة `appVersion` → مستند `appVersion`.

---

## المفاتيح

| الشاشة | أندرويد | iOS |
|---|---|---|
| الرئيسية | `homeMaintain` | `ioshomeMaintain` |
| أسعار الدولار في البنوك | `dollarMaintain` | `iosdollarMaintain` |
| **اجتماعات الفيدرالي** | `FedralliMaintain` | `FedralliMaintain` |
| **الأخبار** | `NewsMaintain` | `NewsMaintain` |
| **السبائك** | `BullionsMaintain` | `BullionsMaintain` |
| التطبيق كله | `needMaintain` | `iosneedMaintain` |

### ⚠️ تلات مفاتيح مشتركة بين المنصتين

الرئيسية والدولار لكل منصة مفتاحها، فتقدر تقفل الشاشة على أندرويد
وتسيبها شغّالة على iOS. **الفيدرالي والأخبار والسبائك مش كده**: مفيش
`ios…` ليهم في اللوحة، فالمفتاح الواحد بيقفل الشاشة على المنصتين مع
بعض.

لو حبيت تتحكم في كل منصة لوحدها: ضيف `iosNewsMaintain` (أو غيره) في
Firestore وقولّي أعدّل `flagKey` في `ScreenMaintenanceService.swift`.

### ⚠️ الاسم مكتوب «Fedralli» بغلطة مقصودة

الحقل في اللوحة اسمه `FedralliMaintain` بحرف F كبير وبالإملاء ده.
الكود بيطابقه بالحرف. **متغيّرش الاسم في اللوحة**: كل نسخة منزّلة على
موبايلات الناس بتقرا الاسم القديم، فتغييره معناه إن الشاشة ترجع
تشتغل عندهم من غير ما حد ياخد باله.

---

## نص الرسالة

اختياري. من غيره بتظهر الرسالة المترجمة المدفونة في التطبيق
(`maintenance_screen_title` / `maintenance_screen_message`).

للتحكم فيها من اللوحة، ضيف الحقول دي على نفس المستند:

```
FedralliMaintainTitle_ar     FedralliMaintainTitle_en
FedralliMaintainMessage_ar   FedralliMaintainMessage_en
NewsMaintainTitle_ar         NewsMaintainTitle_en
NewsMaintainMessage_ar       NewsMaintainMessage_en
BullionsMaintainTitle_ar     BullionsMaintainTitle_en
BullionsMaintainMessage_ar   BullionsMaintainMessage_en
```

ترتيب البحث: لغة المستخدم ← اللغة التانية ← الاسم من غير لاحقة ←
النص المدفون في التطبيق.

---

## شكلها للمستخدم

### التبويبات (الرئيسية · الدولار · الأخبار · السبائك)

الكارت بيغطي محتوى التبويب، **وشريط التبويبات السفلي بيفضل ظاهر
وشغّال** — فالمستخدم يروح لتبويب تاني عادي.

### الفيدرالي

**الهيدر بزرار الرجوع بيفضل مكانه**، والكارت بيحل محل جسم الشاشة بس.

مختلف عن التبويبات عن قصد: شاشة الفيدرالي بيتم الدخول لها من الرئيسية
أو «المزيد»، فلو غطّينا الهيدر كمان المستخدم كان هيتحبس في صفحة
مالهاش خروج. التبويبات مالهاش المشكلة دي لأن شريطها برّه الغطا.

### التبويب المقفول ما بيضربش السيرفر

لما العلم يبقى مرفوع، الشاشة **ما بتحمّلش بياناتها أصلاً** — لا نداء
API ولا مراقب Firestore ولا لودر بيلف. لو قفلت التبويب عشان مصدره
بايظ، ما ينفعش التطبيق يفضل يضرب المصدر البايظ. ولما تقفل العلم،
التحميل بيحصل ساعتها من غير ما المستخدم يعمل أي حاجة.

التغيير بيوصل **فوراً**: مراقب حيّ على المستند، يعني قلب المفتاح
بيغيّر الشاشة للمستخدم اللي فاتحها دلوقتي من غير ما يقفل التطبيق
ويفتحه.

---

## الملفات

**أندرويد**

```
data/source/firebase/FirestoreConstants.kt          أسماء الحقول
domain/model/AppVersionInfo.kt                      ScreenMaintenance
data/repository/OtherRepositories.kt                القراءة من Firestore
presentation/screens/appstatus/
  ScreenMaintenanceViewModel.kt                     MaintainedScreen
  ScreenMaintenanceGate.kt                          الجيت + الكارت
presentation/screens/fomc/FomcScreen.kt             ربط الفيدرالي
presentation/screens/main/MainScreen.kt             ربط التبويبات الأربعة
```

**iOS**

```
MVP/Helper/ScreenMaintenanceService.swift           MaintainedScreen
MVP/CustomViews/ScreenMaintenanceView.swift         الغطا (UIKit) + المراقب
                                                    والكارت (SwiftUI)
MVP/Scenes/HomeFiles/HomeVC.swift                   الرئيسية
MVP/Scenes/.../DollarPricesOnBankVC.swift           الدولار
MVP/Scenes/MetalsScenes/AllNews/AllNewsVC.swift     الأخبار
MVP/Scenes/MetalsScenes/bullionsScreenView.swift    السبائك
MVP/Scenes/MetalsScenes/FomcView.swift              الفيدرالي
```

### UIKit ولا SwiftUI؟

الشاشات المبنية بـUIKit (الرئيسية · الدولار · الأخبار) بتستخدم
`bindScreenMaintenance` — غطا بيتحط على `view` بتاع الكنترولر.

الشاشات المبنية بـSwiftUI (السبائك · الفيدرالي) بتستخدم
`ScreenMaintenanceObserver` + `ScreenMaintenanceCardView` جوّه العرض
نفسه. **مينفعش** نحط غطا UIKit على `UIHostingController`: الـ`view`
بتاعه شجرة SwiftUI بتعيد بناء نفسها، فأي عنصر بتضيفه بعد كده بيتحط
فوق الغطا والكارت بيختفي ورا المحتوى.

مفيش ملف Swift جديد — الحاجات الجديدة اتحطت في ملفات مسجّلة أصلاً في
المشروع، فمفيش تعديل على `project.pbxproj`.

---

## حاجات تعرفها

### كارت «الاجتماع القادم» في الرئيسية مش مقفول

المفتاح بيقفل **شاشة** الفيدرالي. كارت الاجتماع القادم اللي في
الرئيسية بيفضل يظهر ببياناته وعدّاده، والضغط عليه بيودّي على الشاشة
المقفولة (بزرار رجوع، فمفيش حبس).

لو ده مش المطلوب — يعني لو بتقفل الشاشة لأن بيانات الفيدرالي نفسها
غلط — الكارت لازم يتخفي هو كمان. تعديل بسيط في:

- أندرويد: `presentation/screens/home/HomeScreen.kt` (نداء `FomcNextCard`)
- iOS: `MVP/Scenes/HomeFiles/HomeVC+FomcCard.swift`

### أول إطار بعد فتح الشاشة

قيمة المفتاح بتوصل من Firestore بعد لحظة من فتح الشاشة، فأول إطار
بيتبني على «مفيش صيانة» وبعدها الكارت بيظهر. ده نفس سلوك الرئيسية
والدولار من قبل التعديل ده — مش شيء جديد.

### إشعارات الفيدرالي

مواضيع `fomc_ar` / `fomc_en` بتفضل شغّالة والمستخدم بيفضل يستقبل
إشعارات الفيدرالي والشاشة مقفولة. لو عايز توقفها كمان، ده بيتعمل من
السيرفر مش من التطبيق.

---

## إصلاحات دخلت مع الشغل ده

مراقب Firestore بيقفل برمية لو حصل خطأ (صلاحيات · شبكة · المستند
اتمسح). الرمية دي كانت بتطلع من `viewModelScope` من غير أي معالج —
**يعني التطبيق كان بيقفل**. دلوقتي `ScreenMaintenanceViewModel`
و`AppStatusViewModel` الاتنين بيعيدوا الاشتراك كل ٥ ثواني وبيسيبوا
آخر حالة معروفة مكانها.
