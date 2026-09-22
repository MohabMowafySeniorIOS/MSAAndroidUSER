package com.msa.android.presentation.screens.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.msa.android.R
import com.msa.android.data.repository.PortfolioDraft
import com.msa.android.domain.model.ItemType
import com.msa.android.domain.model.PortfolioMetal
import com.msa.android.presentation.common.components.GoldButton
import com.msa.android.presentation.common.components.MSABackground
import com.msa.android.presentation.common.components.MSATopBar
import com.msa.android.presentation.screens.auth.AuthError
import com.msa.android.presentation.screens.auth.AuthField
import com.msa.android.presentation.theme.Gold
import com.msa.android.presentation.theme.GoldenBorder

/**
 * إضافة أو تعديل قطعة.
 *
 * سعر الجرام بيتملا تلقائياً بسعر السوق للعيار المختار، والمبلغ الإجمالي
 * بيتحسب من (الوزن × سعر الجرام) + المصنعية — والاتنين قابلين للتعديل
 * لو المستخدم دفع رقم مختلف.
 */
@Composable
fun WalletEditorScreen(
    entryId: Long?,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    walletVm: WalletViewModel = hiltViewModel(),
    vm: WalletEditorViewModel = hiltViewModel()
) {
    val wallet by walletVm.state.collectAsStateWithLifecycle()
    val busy by vm.busy.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()

    val existing = remember(entryId, wallet.items) {
        entryId?.let { id -> wallet.items.firstOrNull { it.id == id } }
    }

    var metal by remember(existing) { mutableStateOf(existing?.metal ?: PortfolioMetal.GOLD) }
    var karat by remember(existing) { mutableStateOf(existing?.karat ?: "24") }
    var type by remember(existing) { mutableStateOf(existing?.type ?: ItemType.BULLION) }
    var weight by remember(existing) { mutableStateOf(existing?.weight?.takeIf { it > 0 }?.toString() ?: "") }
    var gramPrice by remember(existing) { mutableStateOf(existing?.gramPrice?.toString() ?: "") }
    var manufacturing by remember(existing) { mutableStateOf(existing?.manufacturingPerGram?.takeIf { it > 0 }?.toString() ?: "") }
    var cashback by remember(existing) { mutableStateOf(existing?.cashbackPerGram?.takeIf { it > 0 }?.toString() ?: "") }
    var totalPaid by remember(existing) { mutableStateOf(existing?.totalPaid?.toString() ?: "") }
    var note by remember(existing) { mutableStateOf(existing?.note ?: "") }
    var totalEdited by remember(existing) { mutableStateOf(existing != null) }
    var touched by remember { mutableStateOf(false) }

    // لو المستخدم غيّر المعدن والعيار الحالي مش من عياراته، نرجّعه لأول عيار
    LaunchedEffect(metal) {
        if (karat !in metal.karats) karat = metal.karats.first()
    }

    // سعر الجرام بيتحدّث مع تغيير العيار طالما المستخدم مكتبوش بنفسه
    LaunchedEffect(metal, karat, wallet.gold21, wallet.silver999) {
        if (existing == null) {
            val market = walletVm.currentGramPrice(metal, karat)
            if (market > 0) gramPrice = money(market).replace(",", "")
        }
    }

    // الإجمالي بيتحسب لحد ما المستخدم يكتبه بإيده
    LaunchedEffect(weight, gramPrice, manufacturing) {
        if (!totalEdited) {
            val w = weight.toDoubleOrNull() ?: 0.0
            val g = gramPrice.toDoubleOrNull() ?: 0.0
            val m = manufacturing.toDoubleOrNull() ?: 0.0
            if (w > 0 && g > 0) totalPaid = money(w * g + w * m).replace(",", "")
        }
    }

    val weightBad = (weight.toDoubleOrNull() ?: 0.0) <= 0
    val priceBad = (gramPrice.toDoubleOrNull() ?: 0.0) <= 0

    MSABackground {
        Column(Modifier.fillMaxSize()) {
            MSATopBar(
                title = stringResource(
                    if (entryId == null) R.string.wallet_add else R.string.wallet_edit
                ),
                onBack = onBack
            )

            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(Modifier.height(6.dp))

                FieldLabel(stringResource(R.string.wallet_metal))
                ChipRow(
                    options = PortfolioMetal.entries.map {
                        it to stringResource(if (it == PortfolioMetal.GOLD) R.string.gold else R.string.silver)
                    },
                    selected = metal,
                    onSelect = { metal = it }
                )

                Spacer(Modifier.height(14.dp))
                FieldLabel(stringResource(R.string.wallet_karat))
                ChipRow(
                    options = metal.karats.map { it to stringResource(R.string.wallet_karat_chip, it) },
                    selected = karat,
                    onSelect = { karat = it }
                )

                Spacer(Modifier.height(14.dp))
                FieldLabel(stringResource(R.string.wallet_type))
                ChipRow(
                    options = ItemType.entries.map { it to stringResource(it.labelRes()) },
                    selected = type,
                    onSelect = { type = it }
                )

                Spacer(Modifier.height(18.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) {
                        AuthField(
                            label = stringResource(R.string.wallet_weight),
                            value = weight,
                            onChange = { weight = it.filterDecimal(); vm.clearError() },
                            keyboard = KeyboardType.Decimal,
                            ltr = true,
                            error = touched && weightBad
                        )
                    }
                    Box(Modifier.weight(1f)) {
                        AuthField(
                            label = stringResource(R.string.wallet_gram_price),
                            value = gramPrice,
                            onChange = { gramPrice = it.filterDecimal(); vm.clearError() },
                            keyboard = KeyboardType.Decimal,
                            ltr = true,
                            error = touched && priceBad
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))
                AuthField(
                    label = stringResource(R.string.wallet_manufacturing),
                    value = manufacturing,
                    onChange = { manufacturing = it.filterDecimal() },
                    keyboard = KeyboardType.Decimal,
                    ltr = true
                )

                Spacer(Modifier.height(14.dp))
                AuthField(
                    label = stringResource(R.string.wallet_total_paid),
                    value = totalPaid,
                    onChange = { totalPaid = it.filterDecimal(); totalEdited = true },
                    keyboard = KeyboardType.Decimal,
                    ltr = true
                )
                Text(
                    text = stringResource(R.string.wallet_total_hint),
                    color = Color(0xFF9E9898),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(Modifier.height(14.dp))
                AuthField(
                    label = stringResource(R.string.wallet_cashback_per_gram),
                    value = cashback,
                    onChange = { cashback = it.filterDecimal() },
                    keyboard = KeyboardType.Decimal,
                    ltr = true
                )

                Spacer(Modifier.height(14.dp))
                AuthField(
                    label = stringResource(R.string.wallet_note),
                    value = note,
                    onChange = { note = it }
                )

                error?.let {
                    Spacer(Modifier.height(16.dp))
                    AuthError(it)
                }

                Spacer(Modifier.height(24.dp))

                GoldButton(
                    text = stringResource(R.string.wallet_save),
                    loading = busy
                ) {
                    touched = true
                    if (!weightBad && !priceBad) {
                        vm.save(
                            existingId = entryId,
                            draft = PortfolioDraft(
                                metal = metal,
                                karat = karat,
                                type = type,
                                purchaseDate = null,
                                weight = weight.toDouble(),
                                gramPrice = gramPrice.toDouble(),
                                manufacturingPerGram = manufacturing.toDoubleOrNull() ?: 0.0,
                                cashbackPerGram = cashback.toDoubleOrNull() ?: 0.0,
                                totalPaid = totalPaid.toDoubleOrNull(),
                                note = note.ifBlank { null },
                                imageFile = null
                            ),
                            onSaved = onSaved
                        )
                    }
                }

                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        color = Color(0xFFC6C6C6),
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

/** صف اختيارات — المعدن والعيار والنوع */
@Composable
private fun <T> ChipRow(
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScrollIfNeeded(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { (value, label) ->
            val active = value == selected
            Text(
                text = label,
                color = if (active) Color.White else Color(0xFFB0AAAA),
                fontSize = 13.sp,
                fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (active) Gold.copy(alpha = 0.22f) else Color.Black.copy(alpha = 0.3f))
                    .border(
                        1.dp,
                        if (active) Gold else GoldenBorder.copy(alpha = 0.3f),
                        RoundedCornerShape(10.dp)
                    )
                    .clickable { onSelect(value) }
                    .padding(horizontal = 14.dp, vertical = 9.dp)
            )
        }
    }
}

@Composable
private fun Modifier.horizontalScrollIfNeeded(): Modifier =
    this.horizontalScroll(rememberScrollState())

/** بيسمح بالأرقام ونقطة عشرية واحدة بس */
private fun String.filterDecimal(): String {
    val cleaned = filter { it.isDigit() || it == '.' }
    val firstDot = cleaned.indexOf('.')
    if (firstDot < 0) return cleaned
    return cleaned.substring(0, firstDot + 1) +
           cleaned.substring(firstDot + 1).filter { it.isDigit() }
}
