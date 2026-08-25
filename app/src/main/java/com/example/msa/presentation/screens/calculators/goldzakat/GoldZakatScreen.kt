package com.msa.android.presentation.screens.calculators.goldzakat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.msa.android.R
import com.msa.android.presentation.common.NumberFormatter
import com.msa.android.presentation.common.components.CalculatorInputRow
import com.msa.android.presentation.common.components.GoldButton
import com.msa.android.presentation.common.components.MSABackground
import com.msa.android.presentation.common.components.MSATopBar

@Composable
fun GoldZakatScreen(
    onBack: () -> Unit,
    vm: GoldZakatViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    var showResult by remember { mutableStateOf(false) }

    MSABackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            MSATopBar(title = stringResource(R.string.calc_gold_zakat), onBack = onBack)

            Text(
                text = stringResource(R.string.gold_zakat_note),
                color = Color.White, fontSize = 13.sp,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))

            CalculatorInputRow(stringResource(R.string.karat_24_label), state.g24) { vm.update("24", it) }
            CalculatorInputRow(stringResource(R.string.karat_22_label), state.g22) { vm.update("22", it) }
            CalculatorInputRow(stringResource(R.string.karat_21_label), state.g21) { vm.update("21", it) }
            CalculatorInputRow(stringResource(R.string.karat_18_label), state.g18) { vm.update("18", it) }

            Spacer(Modifier.height(20.dp))

            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                GoldButton(text = stringResource(R.string.calc_zakat)) {
                    vm.calculate(); showResult = true
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }

    val res = state.result
    if (showResult && res != null) {
        AlertDialog(
            onDismissRequest = { showResult = false },
            confirmButton = { TextButton(onClick = { showResult = false }) { Text("OK") } },
            title = { Text(stringResource(R.string.calc_zakat), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("${stringResource(R.string.pure_gold)}: ${NumberFormatter.two(res.totalPureGold)} ${stringResource(R.string.gram)}")
                    Spacer(Modifier.height(8.dp))
                    if (!res.isObligatory) {
                        Text(stringResource(R.string.zakat_not_obligatory))
                    } else {
                        Text("${stringResource(R.string.zakat_in_gold)}: ${NumberFormatter.two(res.zakatGrams)} ${stringResource(R.string.gram)}")
                        Text("${stringResource(R.string.zakat_in_money)}: ${NumberFormatter.two(res.zakatValue)} ${stringResource(R.string.egp)}")
                    }
                }
            }
        )
    }
}
