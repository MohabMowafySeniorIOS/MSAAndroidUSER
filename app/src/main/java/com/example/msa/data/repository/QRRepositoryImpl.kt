package com.msa.android.data.repository

import android.util.Log
import com.msa.android.data.source.local.LanguagePreferences
import com.msa.android.data.source.network.MsaApi
import com.msa.android.data.source.network.dto.BullionVerifyRequest
import com.msa.android.domain.model.BullionDetails
import com.msa.android.domain.model.BullionVerification
import com.msa.android.domain.repository.QRRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * التحقّق من كود سبيكة — من **الباك اند بتاعنا**.
 *
 * ## كان بيقرا من Firestore
 *
 * النسخة القديمة كانت بتدوّر على `qr_codes/{code}` في Firestore،
 * والمجموعة دي مش موجودة في المشروع (اللي موجود اسمه `qrs`) — يعني
 * كل مسح كان بيرجّع «Invalid» مهما كانت السبيكة.
 *
 * دلوقتي الأكواد في قاعدة بيانات اللوحة: بتتولّد وبتتطبع استيكرات من
 * هناك، وبتتلغى من هناك، وكل مسح بيتسجّل. والحكم نفسه بيتحسب في
 * السيرفر مرة واحدة، فالتطبيقين وصفحة الويب بيقولوا نفس الكلام.
 *
 * ## مفيش «تعليم كمستخدم»
 *
 * النسخة القديمة كانت بتكتب `isUsed=true` مع كل مسح ناجح (ومع إنها
 * ما كانتش بتفحصها أبداً). الكود ده إثبات أصالة مش تذكرة: العميل
 * بيمسح سبيكته كل ما يحب، والمشتري التاني لازم يقدر يتأكد كمان.
 */
@Singleton
class QRRepositoryImpl @Inject constructor(
    private val api: MsaApi,
    private val languagePrefs: LanguagePreferences,
) : QRRepository {

    override suspend fun verify(code: String): BullionVerification {
        /*
         * كود فاضي مش بيروح للسيرفر.
         *
         * بنرجّعه كـ`ERROR` مش `UNKNOWN`: الكارت بيقرا العنوان
         * والرسالة من السيرفر، و`UNKNOWN` بنصوص فاضية كان بيرسم
         * دايرة حمرا فوق فراغ. `ERROR` ليه نصّه المحلي جوّه الشاشة.
         *
         * المسار ده مقفول من `ScanQRViewModel` أصلاً، بس السلوك
         * لازم يبقى مظبوط لو حد نادى الريبو من مكان تاني.
         */
        if (code.isBlank()) {
            return BullionVerification(
                status = BullionVerification.Status.ERROR,
                title = "",
                message = "",
            )
        }

        // السيرفر بيرجّع الرسالة جاهزة باللغة المطلوبة
        val lang = runCatching { languagePrefs.languageFlow.first() }.getOrDefault("ar")

        val dto = api.verifyBullionCode(BullionVerifyRequest(code = code), lang).data

        Log.d(TAG, "verify: status=${dto.status}")

        return BullionVerification(
            status = BullionVerification.fromApi(dto.status),
            title = dto.title,
            message = dto.message,
            details = dto.bullion?.let {
                BullionDetails(
                    code = it.code,
                    metal = it.metal,
                    karat = it.karat,
                    weightGrams = it.weightGrams,
                    serial = it.serial,
                )
            },
        )
    }

    private companion object {
        const val TAG = "BullionQR"
    }
}
