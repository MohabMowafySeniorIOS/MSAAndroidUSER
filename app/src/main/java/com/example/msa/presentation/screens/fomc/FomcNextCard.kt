package com.msa.android.presentation.screens.fomc

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.msa.android.R
import com.msa.android.domain.model.FomcEvent
import com.msa.android.domain.repository.FomcRepository
import com.msa.android.presentation.common.FomcFormatter
import com.msa.android.presentation.theme.GoldenBorder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FomcNextState(
    val event: FomcEvent? = null,
    val secondsRemaining: Long? = null,
    val loaded: Boolean = false
)

/**
 * الاجتماع القادم — ViewModel صغير مخصوص لكارت الرئيسية.
 *
 * منفصل عن [FomcViewModel] بقصد: كارت الرئيسية محتاج اجتماع واحد بس،
 * ومش لازم يحمّل تاريخ الاجتماعات كله ولا يحتفظ بتبويبات. الاتنين
 * بيشاركوا نفس الريبوزيتوري، فالكاش مشترك والطلب مبيتكررش.
 */
@HiltViewModel
class FomcNextViewModel @Inject constructor(
    private val repo: FomcRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FomcNextState())
    val state: StateFlow<FomcNextState> = _state.asStateFlow()

    private var ticker: Job? = null

    init {
        viewModelScope.launch {
            val event = repo.next().getOrNull()

            _state.update {
                it.copy(event = event, secondsRemaining = event?.secondsUntil(), loaded = true)
            }

            if (event != null) startTicker()
        }
    }

    private fun startTicker() {
        ticker?.cancel()
        ticker = viewModelScope.launch {
            while (isActive) {
                delay(1_000)

                val remaining = _state.value.event?.secondsUntil()
                _state.update { it.copy(secondsRemaining = remaining) }

                // الاجتماع بدأ/القرار صدر — نجيب اللي بعده مرة واحدة
                if (remaining == null) {
                    repo.invalidate()
                    val next = repo.next().getOrNull()
                    _state.update {
                        it.copy(event = next, secondsRemaining = next?.secondsUntil())
                    }

                    if (next == null) return@launch
                }
            }
        }
    }

    override fun onCleared() {
        ticker?.cancel()
        super.onCleared()
    }
}

/**
 * كارت مضغوط للاجتماع القادم — بيتحط في الرئيسية.
 *
 * بيختفي تماماً لو مفيش اجتماع معلن أو لو التحميل فشل: كارت مكتوب فيه
 * "مفيش بيانات" في وسط الرئيسية أسوأ من غيابه.
 */
@Composable
fun FomcNextCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    vm: FomcNextViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val event = state.event ?: return
    val ctx = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x66000000))
            .border(1.dp, GoldenBorder.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.fomc_next_meeting),
                color = GoldenBorder,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            FomcStatusChip(event.statusAt())
        }

        Text(
            text = FomcFormatter.range(ctx, event.meetingStartAt, event.meetingEndAt),
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )

        FomcFormatter.countdown(ctx, state.secondsRemaining)?.let { countdown ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.fomc_time_remaining),
                    color = Color(0xFFCCCCCC),
                    fontSize = 12.sp
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = countdown,
                    color = GoldenBorder,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
