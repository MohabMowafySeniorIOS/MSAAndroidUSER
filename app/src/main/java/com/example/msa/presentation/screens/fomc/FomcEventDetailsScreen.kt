package com.msa.android.presentation.screens.fomc

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.android.R
import com.msa.android.domain.model.FomcEvent
import com.msa.android.domain.model.FomcStatus
import com.msa.android.domain.repository.FomcRepository
import com.msa.android.presentation.common.FomcFormatter
import com.msa.android.presentation.common.components.GoldButton
import com.msa.android.presentation.common.components.MSABackground
import com.msa.android.presentation.common.components.MSATopBar
import com.msa.android.presentation.theme.GoldenBorder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FomcDetailsState(
    val loading: Boolean = true,
    val event: FomcEvent? = null,
    val failed: Boolean = false
)

/**
 * تفاصيل اجتماع واحد.
 *
 * بيقرا من نفس الريبوزيتوري، واللي غالباً بيلاقي الاجتماع في الكاش
 * اللي شاشة القايمة ملّته — فالانتقال من القايمة للتفاصيل بيبقى فوري
 * من غير طلب شبكة جديد.
 */
@HiltViewModel
class FomcDetailsViewModel @Inject constructor(
    private val repo: FomcRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FomcDetailsState())
    val state: StateFlow<FomcDetailsState> = _state.asStateFlow()

    fun load(id: Long) {
        // لو الاجتماع محمّل خلاص مانعيدش التحميل مع كل إعادة رسم
        if (_state.value.event?.id == id) return

        viewModelScope.launch {
            _state.value = FomcDetailsState(loading = true)

            repo.event(id)
                .onSuccess { event ->
                    _state.value = FomcDetailsState(
                        loading = false,
                        event = event,
                        failed = event == null
                    )
                }
                .onFailure {
                    _state.value = FomcDetailsState(loading = false, failed = true)
                }
        }
    }
}

@Composable
fun FomcEventDetailsScreen(
    eventId: Long,
    onBack: () -> Unit,
    vm: FomcDetailsViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()
    val ctx = LocalContext.current

    androidx.compose.runtime.LaunchedEffect(eventId) { vm.load(eventId) }

    MSABackground {
        Column(Modifier.fillMaxSize()) {

            MSATopBar(
                title = stringResource(R.string.fomc_details_title),
                showBack = true,
                onBack = onBack
            )

            when {
                state.loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = GoldenBorder) }

                state.event == null -> FomcErrorState(onRetry = { vm.load(eventId) })

                else -> {
                    val event = state.event!!
                    val status = event.statusAt()

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 120.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // ── رأس: التاريخ والحالة ──────────────────────
                        Section {
                            Text(
                                text = FomcFormatter.range(
                                    ctx, event.meetingStartAt, event.meetingEndAt
                                ),
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            FomcStatusChip(status)

                            if (status != FomcStatus.COMPLETED) {
                                Spacer(Modifier.height(14.dp))
                                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    FomcCountdown(event.secondsUntil())
                                }
                            }
                        }

                        // ── المواعيد ──────────────────────────────────
                        Section {
                            SectionTitle(stringResource(R.string.fomc_section_schedule))

                            FomcDetailRow(
                                stringResource(R.string.fomc_meeting_start),
                                FomcFormatter.date(ctx, event.meetingStartAt)
                            )
                            FomcDetailRow(
                                stringResource(R.string.fomc_meeting_end),
                                FomcFormatter.date(ctx, event.meetingEndAt)
                            )
                            FomcDetailRow(
                                stringResource(R.string.fomc_decision_at),
                                FomcFormatter.dateTime(ctx, event.decisionAt)
                            )

                            if (event.hasPressConference) {
                                FomcDetailRow(
                                    stringResource(R.string.fomc_press_conference),
                                    FomcFormatter.dateTime(ctx, event.pressConferenceAt)
                                )
                            }

                            // المحضر بيصدر بعد ٣ أسابيع — ما بيظهرش قبل كده
                            event.minutesAt?.let {
                                FomcDetailRow(
                                    stringResource(R.string.fomc_minutes_at),
                                    FomcFormatter.dateTime(ctx, it)
                                )
                            }

                            event.officialTimezone?.let { tz ->
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = stringResource(R.string.fomc_local_time_note, tz),
                                    color = Color(0xFF9E9E9E),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // ── الفائدة ───────────────────────────────────
                        // بتظهر بس لو القرار صدر فعلاً
                        if (event.federalFundsRate != null) {
                            Section {
                                SectionTitle(stringResource(R.string.fomc_section_rate))

                                FomcRateBadge(event)
                                Spacer(Modifier.height(8.dp))

                                FomcFormatter
                                    .rateRange(ctx, event.rateLowerBound, event.rateUpperBound)
                                    ?.let {
                                        FomcDetailRow(
                                            stringResource(R.string.fomc_target_range), it
                                        )
                                    }

                                event.previousRate?.let {
                                    FomcDetailRow(
                                        stringResource(R.string.fomc_previous_rate),
                                        FomcFormatter.rate(ctx, it)
                                    )
                                }

                                FomcFormatter.rateChange(ctx, event.rateChange)?.let {
                                    FomcDetailRow(stringResource(R.string.fomc_rate_change), it)
                                }
                            }
                        }

                        if (event.hasProjections) {
                            Section {
                                Text(
                                    text = stringResource(R.string.fomc_projections_note),
                                    color = Color(0xFFCCCCCC),
                                    fontSize = 13.sp
                                )
                            }
                        }

                        // ── الروابط الرسمية ───────────────────────────
                        val links = listOfNotNull(
                            event.statementUrl?.let { R.string.fomc_open_statement to it },
                            event.minutesUrl?.let { R.string.fomc_open_minutes to it },
                            event.sourceUrl?.let { R.string.fomc_open_source to it }
                        )

                        if (links.isNotEmpty()) {
                            Section {
                                SectionTitle(stringResource(R.string.fomc_section_links))

                                links.forEach { (labelRes, url) ->
                                    Spacer(Modifier.height(8.dp))
                                    GoldButton(
                                        text = stringResource(labelRes),
                                        filled = false,
                                        big = false
                                    ) {
                                        // الروابط الرسمية بتتفتح في المتصفح —
                                        // صفحات الفيدرالي فيها PDF وجداول مش
                                        // مظبوطة لويب فيو جوّه التطبيق.
                                        runCatching {
                                            ctx.startActivity(
                                                Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Text(
                            text = stringResource(R.string.fomc_source_note),
                            color = Color(0xFF9E9E9E),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Section(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x66000000))
            .border(1.dp, GoldenBorder.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) { content() }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = GoldenBorder,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold
    )
    Spacer(Modifier.height(4.dp))
}
