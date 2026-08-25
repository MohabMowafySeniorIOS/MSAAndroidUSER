package com.msa.android.presentation.screens.calculators.silvervalue

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
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
fun SilverValueScreen(
    onBack: () -> Unit,
    vm: SilverValueViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    var showResult by remember { mutableStateOf(false) }

    MSABackground {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
        ) {
            MSATopBar(title = stringResource(R.string.calc_silver_value), onBack = onBack)
            Text(
                text = stringResource(R.string.silver_value_note, NumberFormatter.two(state.base999Sell)),
                color = Color.White, fontSize = 13.sp,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            CalculatorInputRow(stringResource(R.string.karat_999), state.g999) { vm.update("999", it) }
            CalculatorInputRow(stringResource(R.string.karat_925), state.g925) { vm.update("925", it) }
            CalculatorInputRow(stringResource(R.string.karat_900), state.g900) { vm.update("900", it) }
            CalculatorInputRow(stringResource(R.string.karat_800), state.g800) { vm.update("800", it) }
            CalculatorInputRow(stringResource(R.string.karat_600), state.g600) { vm.update("600", it) }

            Spacer(Modifier.height(20.dp))
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                GoldButton(text = stringResource(R.string.calc_silver_value)) {
                    vm.calculate(); showResult = true
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }

    if (showResult) {
        AlertDialog(
            onDismissRequest = { showResult = false },
            confirmButton = { TextButton(onClick = { showResult = false }) { Text("OK") } },
            title = { Text(stringResource(R.string.calc_silver_value), fontWeight = FontWeight.Bold) },
            text = { Text("${stringResource(R.string.total)}: ${NumberFormatter.two(state.total)} ${stringResource(R.string.egp)}") }
        )
    }
}
