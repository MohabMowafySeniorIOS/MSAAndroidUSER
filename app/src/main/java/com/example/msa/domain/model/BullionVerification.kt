package com.msa.android.domain.model

/**
 * نتيجة التحقّق من كود سبيكة.
 *
 * النصوص **جاية من السيرفر** مش مكتوبة في التطبيق. السبب: نفس الحكم
 * بيتعرض في تطبيق أندرويد و iOS وصفحة الويب، ولو كل واحد كتب نصّه
 * كان تعديل الصياغة لازم ينزل ٣ مرات — ونسخة واحدة تتنسى فالعميل
 * يشوف كلام مختلف لنفس السبيكة حسب إيه اللي مسح بيه.
 */
data class BullionVerification(
    val status: Status,
    val title: String,
    val message: String,
    val details: BullionDetails? = null,
) {
    enum class Status {
        /** مسجّل عندنا وسليم */
        VALID,

        /** كان مسجّل واتلغى — سبب رفض مختلف عن المجهول */
        VOID,

        /** مش في سجلاتنا */
        UNKNOWN,

        /**
         * ما وصلناش للسيرفر أصلاً.
         *
         * **مش** نفس `UNKNOWN`. «السبيكة مش عندنا» حكم على السبيكة،
         * و«الشبكة وقعت» حكم على الاتصال — وخلطهم معناه إننا نقول
         * لعميل واقف في المحل إن سبيكته مضروبة عشان الواي فاي فصل.
         */
        ERROR,
    }

    val isValid: Boolean get() = status == Status.VALID

    companion object {
        fun fromApi(status: String): Status = when (status.lowercase()) {
            "valid" -> Status.VALID
            "void"  -> Status.VOID
            else    -> Status.UNKNOWN
        }
    }
}

/** بيانات السبيكة — بترجع للكود السليم بس */
data class BullionDetails(
    val code: String,
    val metal: String?,
    val karat: Int?,
    val weightGrams: Double?,
    val serial: String?,
)
