package com.msa.android.presentation.screens.qr

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.android.domain.model.BullionVerification
import com.msa.android.domain.repository.QRRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * التحقّق من كود السبيكة اللي الكاميرا قرته.
 *
 * ## أول مسح بيفوز، والباقي بيتجاهل
 *
 * الكاميرا بتقرا نفس الكود ٣٠ مرة في الثانية طول ما هو قدّامها. من
 * غير القفل ده كنا هنبعت ٣٠ طلب في الثانية لنفس الكود، وكل واحد
 * بيتسجّل في سجل المسح عند المشرف — فسبيكة اتمسحت مرة واحدة تبان
 * كأنها اتمسحت ٢٠٠ مرة.
 *
 * والقفل ما بيتفتحش بمؤقّت: بيفضل مقفول لحد ما المستخدم يدوس «امسح
 * تاني». اللي خلّص مسح عايز يقرا النتيجة، مش إن الكاميرا تعيد المسح
 * من ورا ظهره.
 */
@HiltViewModel
class ScanQRViewModel @Inject constructor(
    private val repository: QRRepository,
) : ViewModel() {

    /** `null` معناه لسه ما اتمسحش حاجة */
    var result by mutableStateOf<BullionVerification?>(null)
        private set

    var loading by mutableStateOf(false)
        private set

    fun verify(code: String) {
        if (loading || result != null || code.isBlank()) return

        loading = true

        viewModelScope.launch {
            result = try {
                repository.verify(code)
            } catch (e: Exception) {
                /*
                 * فشل الشبكة **مش** «سبيكة مضروبة».
                 *
                 * لو رجّعنا هنا نفس رسالة الكود المجهول، أي انقطاع
                 * واي فاي كان هيقول لعميل واقف في المحل إن سبيكته
                 * مش من عندنا. الحالة دي ليها نصّها ورسالتها.
                 */
                BullionVerification(
                    status = BullionVerification.Status.ERROR,
                    title = "",
                    message = e.localizedMessage ?: e.javaClass.simpleName,
                )
            }
            loading = false
        }
    }

    /** رجوع للكاميرا لمسح سبيكة تانية */
    fun clearResult() {
        result = null
        loading = false
    }
}
