package com.msa.android.presentation.screens.calculators.bullions

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.msa.android.R
import com.msa.android.presentation.common.components.MSABackground
import com.msa.android.presentation.common.components.MSATopBar
import kotlin.collections.get
import kotlin.math.round
import kotlin.times

/**
 * 1:1 port of iOS bullionsScreenView.
 *
 *   VStack {
 *     header
 *     ScrollView {
 *       productCard   (HStack [chooseViews | image])
 *       if selectedGrame != nil { priceGrid }
 *     }
 *   }
 */
@Composable
fun BullionScreen(
    onBack: () -> Unit,
    vm: BullionViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // iOS imageName: starts "MSA", becomes company name on company select.
    // Try to find a drawable matching the company name; fall back to ic_msa_logo.
    val productImageRes = remember(state.imageName) {
        if (state.imageName.isBlank() || state.imageName == "MSA") {
            R.drawable.ic_msa_logo
        } else {
            val resName = state.imageName
                .lowercase()
                .replace(" ", "_")
                .replace("-", "_")
            val id = context.resources.getIdentifier(resName, "drawable", context.packageName)
            if (id != 0) id else R.drawable.ic_msa_logo
        }
    }

    MSABackground {
        Column(Modifier.fillMaxSize()) {
            MSATopBar(
                title = stringResource(R.string.bullions),
                onBack = onBack,
                showShare = true,
                onShare = { com.msa.android.presentation.common.shareApp(context) }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                ProductCard(
                    state = state,
                    productImageRes = productImageRes,
                    onPickMetal = vm::showMetalPicker,
                    onPickCompany = vm::showCompanyPicker,
                    onPickGram = vm::showGramPicker
                )

                if (state.selectedGram != null) {
                    PriceGrid(state, vm.handleGramPrice())
                }
            }
        }
    }

    // Pickers (white rounded bottom-sheet-style modal — matches iOS Menu)
    if (state.metalPickerVisible) {
        PickerDialog(
            title = "العيار",
            items = state.metals.map { it.name },
            onSelect = { idx -> vm.selectMetal(state.metals[idx]) },
            onDismiss = vm::dismissPickers
        )
    }
    if (state.companyPickerVisible) {
        PickerDialog(
            title = "الشركة المُصنعة",
            items = state.companies,
            onSelect = { idx -> vm.selectCompany(state.companies[idx]) },
            onDismiss = vm::dismissPickers
        )
    }
    if (state.gramPickerVisible) {
        PickerDialog(
            title = "المنتج",
            items = state.grams.map { it.name },
            onSelect = { idx -> vm.selectGram(state.grams[idx]) },
            onDismiss = vm::dismissPickers
        )
    }
}

/* ───────────── Product card (HStack chooseViews | image) ───────────── */

@Composable
private fun ProductCard(
    state: BullionState,
    productImageRes: Int,
    onPickMetal: () -> Unit,
    onPickCompany: () -> Unit,
    onPickGram: () -> Unit
) {
    // iOS:
    //   HStack {
    //     chooseViews        ← VStack of 3 dropdowns
    //     Image(80x80)
    //   }
    //   .padding()
    //   .background(.black.opacity(0.4))
    //   .cornerRadius(12)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.4f))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            DropdownLabel(
                title = "عيار",
                value = state.selectedMetal?.name ?: "",
                onClick = onPickMetal
            )
            DropdownLabel(
                title = "الشركة المُصنعة",
                value = state.selectedCompany,
                onClick = onPickCompany
            )
            DropdownLabel(
                title = "المنتج",
                value = state.selectedGram?.name ?: "",
                onClick = onPickGram
            )
        }

        Spacer(Modifier.width(12.dp))

        // iOS: Image(imageName).frame(width: 80, height: 80)
        Image(
            painter = painterResource(productImageRes),
            contentDescription = null,
            modifier = Modifier.size(80.dp)
        )
    }
}

/** Mirrors iOS label(title:value:) — black 40% bg + corner 12 */
@Composable
private fun DropdownLabel(title: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.4f))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (value == "") {
            Text(
                text = title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }else {
            Text(
                text = value,
                color = Color.White,
                fontSize = 14.sp,
                textAlign = TextAlign.End
            )
        }

        Spacer(Modifier.weight(1f))

        Image(
            painter = painterResource(id = R.drawable.ic_arrow_down),
            contentDescription = "logo",
            modifier = Modifier.size(24.dp),
            contentScale = ContentScale.Crop
        )
    }
}

/* ─────────────────────── Price grid (5 cards) ─────────────────────── */

@Composable
private fun PriceGrid(state: BullionState, prices: Pair<Double, Double>) {
    val gram = state.selectedGram ?: return
    val gramSalePrice = prices.first
    val gramBuyPrice  = prices.second
    val total    = (gram.manufacturing + gramSalePrice) * gram.count
    val resale   = (gram.cashBack      + gramBuyPrice)  * gram.count

    Column(verticalArrangement = Arrangement.spacedBy(15.dp)) {
        Text(
            text = "ملاحظة هامة: تكاليف التصنيع المُذكورة هي الأقل وفقًا لآخر تحديث " +
                    "من الشركة المنتجة. يتم تحديث سعر الجرام للذهب وفقًا لآخر سعر في " +
                    "سوق الذهب وليس مأخوذًا من الشركة المنتجة.",
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )

        PriceCard("سعر الجرام",   "${formatNum(gramSalePrice)} جنيه")
        PriceCard("التصنيع",      "${formatNum(gram.manufacturing)} جنيه")
        PriceCard("الإجمالي",     "${formatNum(round(total))} جنيه")
        PriceCard("الكاش باك",    "${formatNum(gram.cashBack)} جنيه")
        PriceCard("إعادة البيع",  "${formatNum(round(resale))} جنيه")
    }
}

private fun formatNum(v: Double): String =
    if (v == v.toLong().toDouble()) v.toLong().toString()
    else "%.2f".format(v)

/** Mirrors iOS glowCard: black 0.4 bg + yellow 0.3 stroke + yellow shadow + corner 20 */
@Composable
private fun PriceCard(title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color(0x4DFFD700),
                spotColor = Color(0x4DFFD700)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Black.copy(alpha = 0.4f))
            .border(1.dp, Color(0x4DFFD700), RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = value,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/* ─────────────────────── Picker dialog ─────────────────────── */

@Composable
private fun PickerDialog(
    title: String,
    items: List<String>,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = title,
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                textAlign = TextAlign.Center
            )
            DividerLine()
            if (items.isEmpty()) {
                Text(
                    text = "لا توجد خيارات",
                    color = Color.Gray,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    textAlign = TextAlign.Center
                )
            } else {
                items.forEachIndexed { idx, item ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(idx) }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item,
                            color = Color.Black,
                            fontSize = 16.sp
                        )
                    }
                    if (idx != items.lastIndex) DividerLine()
                }
            }
        }
    }
}

@Composable
private fun DividerLine() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(0.6.dp)
            .background(Color(0x33000000))
    )
}

