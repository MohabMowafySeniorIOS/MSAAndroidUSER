package com.msa.android.presentation.screens.branches

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.msa.android.R
import com.msa.android.domain.model.Branch

/**
 * الأفعال اللي المستخدم بيعملها على فرع: الاتجاهات، الاتصال، الواتساب.
 *
 * كلها بتفتح تطبيقات خارجية، وكلها معمولة بنفس الاحتياط: لو التطبيق
 * المستهدف مش موجود بنقع على بديل بدل ما التطبيق يعمل crash بـ
 * `ActivityNotFoundException` — وده بيحصل فعلاً على أجهزة من غير
 * خرايط جوجل أو من غير واتساب.
 */
object BranchActions {

    /**
     * فتح الاتجاهات.
     *
     * بنجرّب رابط الاتجاهات اللي السيرفر بناه الأول (بيفتح تطبيق
     * خرايط جوجل لو متثبّت، والمتصفح لو لأ)، وبعدين `geo:` كبديل
     * لأي تطبيق خرايط تاني على الجهاز.
     */
    fun openDirections(context: Context, branch: Branch) {
        val url = branch.directionsUrl
        val lat = branch.latitude
        val lng = branch.longitude

        val candidates = buildList {
            url?.let { add(Uri.parse(it)) }

            if (lat != null && lng != null) {
                val label = Uri.encode(branch.name(arabic = true))
                add(Uri.parse("geo:$lat,$lng?q=$lat,$lng($label)"))
                add(Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lng"))
            }
        }

        if (!launchFirstAvailable(context, candidates)) {
            toast(context, R.string.branch_no_maps_app)
        }
    }

    fun call(context: Context, phone: String) {
        // ACTION_DIAL مش ACTION_CALL: بيفتح الاتصال بالرقم جاهز
        // ومستنّي ضغطة المستخدم، فمش محتاجين إذن CALL_PHONE أصلاً.
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${phone.trim()}"))

        if (!launch(context, intent)) {
            toast(context, R.string.branch_no_dialer)
        }
    }

    /**
     * فتح محادثة واتساب.
     *
     * `wa.me` بيشتغل مع واتساب العادي وواتساب بزنس، وبيقع على المتصفح
     * لو مفيش أي واحد فيهم — أبسط وأمتن من استهداف حزمة بعينها.
     */
    fun whatsapp(context: Context, number: String) {
        // wa.me بيرفض أي رموز غير الأرقام (+ و مسافات و شرطات)
        val digits = number.filter { it.isDigit() }

        if (digits.isEmpty()) {
            toast(context, R.string.branch_no_whatsapp)

            return
        }

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$digits"))

        if (!launch(context, intent)) {
            toast(context, R.string.branch_no_whatsapp)
        }
    }

    private fun launchFirstAvailable(context: Context, uris: List<Uri>): Boolean =
        uris.any { launch(context, Intent(Intent.ACTION_VIEW, it)) }

    private fun launch(context: Context, intent: Intent): Boolean = try {
        // الشاشة ممكن تتفتح من سياق مش Activity، فبنضيف العلم ده
        // عشان ما يحصلش استثناء على بعض الأجهزة
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        true
    } catch (e: Exception) {
        Log.d("BranchActions", "intent failed: ${intent.data}", e)
        false
    }

    private fun toast(context: Context, messageRes: Int) {
        Toast.makeText(context, messageRes, Toast.LENGTH_SHORT).show()
    }
}
