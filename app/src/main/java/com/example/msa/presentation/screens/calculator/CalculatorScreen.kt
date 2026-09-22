package com.msa.android.presentation.screens.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.msa.android.R
import com.msa.android.presentation.common.components.MSABackground
import com.msa.android.presentation.common.components.MSATopBar
import com.msa.android.presentation.theme.GoldenBorder

@Composable
fun CalculatorScreen(
    onBack: () -> Unit,
    onGoldValue: () -> Unit,
    onGoldZakat: () -> Unit,
    onSilverValue: () -> Unit,
    onSilverZakat: () -> Unit
) {
    MSABackground {
        Column(Modifier.fillMaxSize()) {
            MSATopBar(title = stringResource(R.string.calculator_title), showBack = true, onBack = onBack)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .padding(bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CalculatorCard(
                        title = stringResource(R.string.calc_gold_value),
                        icon = Icons.Filled.CurrencyBitcoin,
                        modifier = Modifier.weight(1f),
                        onClick = onGoldValue
                    )
                    CalculatorCard(
                        title = stringResource(R.string.calc_gold_zakat),
                        icon = Icons.Filled.AttachMoney,
                        modifier = Modifier.weight(1f),
                        onClick = onGoldZakat
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CalculatorCard(
                        title = stringResource(R.string.calc_silver_value),
                        icon = Icons.Filled.GridView,
                        modifier = Modifier.weight(1f),
                        onClick = onSilverValue
                    )
                    CalculatorCard(
                        title = stringResource(R.string.calc_silver_zakat),
                        icon = Icons.Filled.Percent,
                        modifier = Modifier.weight(1f),
                        onClick = onSilverZakat
                    )
                }
            }
        }
    }
}

@Composable
private fun CalculatorCard(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(140.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xCC1C1C1E))
            .border(1.dp, GoldenBorder, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = GoldenBorder,
                modifier = Modifier.size(36.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
