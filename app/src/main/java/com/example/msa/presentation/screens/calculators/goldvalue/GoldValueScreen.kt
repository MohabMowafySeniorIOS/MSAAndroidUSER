package com.msa.android.presentation.screens.calculators.goldvalue

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
fun GoldValueScreen(
    onBack: () -> Unit,
    vm: GoldValueViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    var showResult by remember { mutableStateOf(false) }

    MSABackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            MSATopBar(title = stringResource(R.string.calc_gold_value), onBack = onBack)

            Text(
                text = stringResource(R.string.gold_value_note, NumberFormatter.two(state.base21Sell)),
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))

            CalculatorInputRow(
                karatLabel = stringResource(R.string.karat_24),
                value = state.g24,
                onValueChange = { vm.update("24", it) }
            )
            CalculatorInputRow(
                karatLabel = stringResource(R.string.karat_22),
                value = state.g22,
                onValueChange = { vm.update("22", it) }
            )
            CalculatorInputRow(
                karatLabel = stringResource(R.string.karat_21),
                value = state.g21,
                onValueChange = { vm.update("21", it) }
            )
            CalculatorInputRow(
                karatLabel = stringResource(R.string.karat_18),
                value = state.g18,
                onValueChange = { vm.update("18", it) }
            )

            Spacer(Modifier.height(20.dp))

            Box(modifier = Modifier.padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
                GoldButton(text = stringResource(R.string.calc_gold_value)) {
                    vm.calculate()
                    showResult = true
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }

    if (showResult) {
        AlertDialog(
            onDismissRequest = { showResult = false },
            confirmButton = { TextButton(onClick = { showResult = false }) { Text("OK") } },
            title = { Text(stringResource(R.string.calc_gold_value), fontWeight = FontWeight.Bold) },
            text = { Text("${stringResource(R.string.total)}: ${NumberFormatter.two(state.total)} ${stringResource(R.string.egp)}") }
        )
    }
}
