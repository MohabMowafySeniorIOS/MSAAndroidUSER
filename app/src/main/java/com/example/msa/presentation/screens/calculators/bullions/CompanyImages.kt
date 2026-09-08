package com.msa.android.presentation.screens.calculators.bullions

import androidx.annotation.DrawableRes
import com.msa.android.R

/**
 * صورة الشركة المصنّعة في شاشة السبائك — نفس سلوك iOS
 * (`bullionsScreenView.swift`: `imageName = company` ثم `Image(imageName)`).
 *
 * في iOS اسم الأصل هو اسم الشركة نفسه زي ما هو جاي من Firestore، وده ينفع
 * هناك لأن Asset Catalog بيقبل مسافات وحروف عربية. أندرويد مبيقبلش —
 * أسماء الموارد لازم تبقى حروف إنجليزية صغيرة وأرقام وشرطة سفلية بس.
 * عشان كده الربط هنا صريح بدل ما نحوّل الاسم بالكود:
 *
 *   • "الراعي" و"نجم الدين" مستحيل يتحولوا لاسم مورد صالح أصلاً.
 *   • `getIdentifier` بيبحث بالاسم وقت التشغيل، وده بيكسر لو الـ resource
 *     shrinking شال الصورة لأنها مش متشاف عليها في الكود.
 *
 * أي شركة جديدة تتضاف في Firestore: ضيف صورتها في `drawable-nodpi`
 * وسجّلها في الخريطة دي — لو مش موجودة بيظهر لوجو MSA زي ما iOS بيعمل.
 */
object CompanyImages {

    private val byName: Map<String, Int> = mapOf(
        "BTC"          to R.drawable.company_btc,
        "ELGALLA Gold" to R.drawable.company_elgalla_gold,
        "GFG"          to R.drawable.company_gfg,
        "Gold ERA"     to R.drawable.company_gold_era,
        "MB Gold"      to R.drawable.company_mb_gold,
        "MSA"          to R.drawable.company_msa,
        "SAM"          to R.drawable.company_sam,
        "Selema"       to R.drawable.company_selema,
        "Swiss Gold"   to R.drawable.company_swiss_gold,
        "الراعي"        to R.drawable.company_alraie,
        "نجم الدين"     to R.drawable.company_nagm_eldin,
    )

    /** نسخة بمفاتيح موحّدة — تتحمّل فروق المسافات وحالة الحروف من لوحة التحكم. */
    private val normalized: Map<String, Int> =
        byName.entries.associate { (name, res) -> normalize(name) to res }

    @DrawableRes
    val default: Int = R.drawable.company_msa

    /** بيرجع صورة الشركة، ولو مش معروفة بيرجع لوجو MSA زي iOS. */
    @DrawableRes
    fun forCompany(company: String?): Int {
        if (company.isNullOrBlank()) return default
        byName[company]?.let { return it }
        return normalized[normalize(company)] ?: default
    }

    private fun normalize(value: String): String =
        value.trim().replace(Regex("\\s+"), " ").lowercase()
}
