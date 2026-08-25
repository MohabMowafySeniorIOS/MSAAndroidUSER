# 🔧 إصلاح مشكلة "الداتا مش بتيجي من Firebase"

## 🐛 المشاكل اللي كانت موجودة

عند فحص الكود لاقيت **3 مشاكل أساسية**:

### المشكلة #1 — تخبط في الـ Packages
الملفات كانت في مجلد `com/msa/gold/` لكن جواها مكتوب `package com.msa.android`.
**الحل**: نقلت كل الملفات لـ `com.msa.android/` عشان يطابق الـ package + يطابق الـ `applicationId` و الـ `google-services.json`.

### المشكلة #2 — الـ Firestore mapping كان غلط ❗ (السبب الرئيسي)

**في iOS** الكود الأصلي بيقرأ:
```swift
var type = doc.documentID                    // ← الـ document ID
let data = doc.data()
var name = data["name_ar"] as? String         // ← اسم الـ field: name_ar
let buyPrice = data["buyPrice"] as? String
```

**في Android** كان مكتوب (غلط):
```kotlin
type = doc.getString("type")     // ❌ بيدور على field "type" — مش موجود في Firestore!
name = doc.getString("name")     // ❌ المفروض يكون "name_ar"
```

**النتيجة**: `type` كانت `null` دايماً، فالـ filter `metals.firstOrNull { it.type == "gold" }` كان يرجع `null`، فمفيش data بتظهر.

**الحل**:
```kotlin
type = doc.id                                                // ← الـ document ID
name = (data["name_ar"] as? String) ?: (data["name"] as? String)
buyPrice = (data["buyPrice"] as? String) ?: (data["buyPrice"] as? Number)?.toString()
```

### المشكلة #3 — HomeViewModel كان يدور على types غلط
- كان: `pickBase(metals, "gold_21")` و `pickBase(metals, "silver_800")` ❌
- المفروض: `pickBase(metals, "gold")` و `pickBase(metals, "silver")` ✅ (نفس iOS)

---

## ✅ الحاجات اللي اتعملت

| الملف | التعديل |
|---|---|
| **MetalsRepositoryImpl** | يستخدم `doc.id` كـ type + يقرأ `name_ar` + يدعم القيم نمبر أو سترينج + **logging** |
| **HomeViewModel** | يستخدم `"gold"` و `"silver"` كـ types (نفس iOS) + logging |
| **BanksRepositoryImpl** | يلاقي central bank بـ name contains "المركزى" بدل document ID جامد + logging |
| **OtherRepositories** (FAQ) | يستخدم الـ field `qestion_ar` (typo الموجود في iOS بالظبط) |
| **FirestoreExtensions** | يطبع errors في Logcat بدل ما يبتلعها |
| **packages** | كل الملفات اتنقلت لـ `com.msa.android/` |

---

## 🔍 خطوات الـ debugging بعد التشغيل

شغّل التطبيق وافتح **Logcat** في Android Studio، وفلتر بـ tag = `MetalsRepo`. هتشوف:

### حالة ١: الـ Logs تظهر "0 docs"
```
D/MetalsRepo: metals snapshot: 0 docs
```
**معناه**: الـ collection اسمها `metals` موجودة، لكن مفيش documents جواها.
**الحل**: تأكد إن الـ Firestore بتاعتك فعلاً فيها documents.

### حالة ٢: الـ Logs تظهر "X docs" بس قيم null
```
D/MetalsRepo: metals snapshot: 2 docs
D/MetalsRepo: metal doc id=gold buy=null sell=null
```
**معناه**: الـ documents موجودة بس الـ field names مختلفة. شوف في Firebase Console:
- هل اسم الـ field `buyPrice` ولا `buy_price` ولا حاجة تانية؟
- لو مختلف، عدّل الـ field name في `MetalsRepositoryImpl.kt`

### حالة ٣: error PERMISSION_DENIED
```
E/FirestoreFlow: Query error: PERMISSION_DENIED: Missing or insufficient permissions
```
**معناه**: قواعد الـ Security Rules بتاعت Firestore بتمنع القراءة.
**الحل**: في Firebase Console → Firestore Database → Rules:
```js
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Public read for prices (gold/silver/banks/FAQ/pages/onboarding/news)
    match /metals/{doc} { allow read: if true; }
    match /banks/{doc}  { allow read: if true; }
    match /FAQ/{doc}    { allow read: if true; }
    match /pages/{doc}  { allow read: if true; }
    match /on_boarding/{doc} { allow read: if true; }
    match /bullions/{doc} { allow read: if true; }
    match /news/{doc}     { allow read: if true; }

    // Write contact messages
    match /ContactUs/{doc} { allow create: if true; }
  }
}
```

### حالة ٤: error UNAVAILABLE
```
E/FirestoreFlow: Query error: UNAVAILABLE
```
**معناه**: مفيش إنترنت أو الـ Firebase down.
**الحل**: تأكد من INTERNET permission في الـ Manifest (موجودة بالفعل).

---

## 📋 الـ Schema المتوقع للـ Firestore

عشان الكود يشتغل، Firestore لازم تكون فيها:

### Collection: `metals`
كل document = نوع معدن. الـ documentID هو الـ type:

| Document ID | Fields |
|---|---|
| `gold`     | `{ name_ar: "ذهب 21", buyPrice: "5200", salePrice: "5250", updatedAt: <Timestamp> }` |
| `silver`   | `{ name_ar: "فضة 800", buyPrice: "55", salePrice: "57", updatedAt: <Timestamp> }` |
| `ounce_price` | `{ goldPrice: 2400, silverPrice: 30 }` |

### Collection: `banks`
كل document = بنك:
```json
{
  "name": "البنك المركزى",
  "buy": "47.5",
  "sell": "47.6",
  "logo": "https://...",
  "date": "2026-05-16",
  "trend": "up"          // up | down | same
}
```

### Collection: `FAQ`
```json
{
  "qestion_ar": "إيه هو سعر الذهب؟",     // ⚠️ "qestion" مش "question" (typo من iOS)
  "qestion_en": "What is gold price?",
  "answer_ar": "...",
  "answer_en": "..."
}
```

### Collection: `pages`
Documents بـ IDs ثابتة (مطابقة لـ iOS):
- `about`, `terms`, `privacy`, `refund` (مش `about_us` ولا `usage_policy` — iOS بيستخدم الأسماء القصيرة)
- Fields:
  ```
  title_ar, title_en
  describtion_ar, describtion_en   ← typo بيستخدمه iOS (مش "description"!)
  ```
  لو الـ Firestore عندك بـ `content_ar/en` أو `description_ar/en` الكود هيقع عليها كـ fallback.

### Collection: `on_boarding`
```json
{
  "image": "https://...",
  "title_ar": "...",
  "title_en": "...",
  "description_ar": "...",
  "description_en": "...",
  "order": 0
}
```

### Collection: `ContactUs` (write-only)
يتعمل عند إرسال رسالة من شاشة Help.

### Collection: `news` (التاب الرابع)
ترتيب: `publishedAt` descending. كل document:
```json
{
  "title": "Gold prices hit record",
  "description": "...",
  "url": "https://...",
  "urlToImage": "https://...",
  "publishedAt": "2026-05-16T10:30:00Z",
  "source": { "name": "Reuters" }
}
```

### Collection: `bullions`
```json
{ "karat": "21", "company": "...", "product": "...", "price": "...", "image": "...", "type": "gold" }
```

---

## 🎯 لو لسه مش شغّال

ابعتلي **screenshot من Logcat** فيه:
- الـ Tag: `MetalsRepo` و `BanksRepo` و `FirestoreFlow`
- وعلى الأقل أول 50 سطر بعد ما التطبيق يفتح

أو ابعتلي **screenshot من Firebase Console** للـ `metals` collection عشان أشوف الـ schema بتاعتك الحقيقية.

---

## 🛠️ ملف Build إضافي

لو لسه عندك مشكلة في الـ build بسبب Hilt أو KSP:
```bash
./gradlew clean
./gradlew :app:assembleDebug --info
```
