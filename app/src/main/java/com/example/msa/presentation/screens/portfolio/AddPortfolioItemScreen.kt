package com.msa.android.presentation.screens.portfolio

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
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
import com.msa.android.domain.model.AssetType
import com.msa.android.presentation.common.components.MSABackground
import com.msa.android.presentation.common.components.MSATopBar
import com.msa.android.presentation.theme.GoldenBorder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPortfolioItemScreen(
    onBack: () -> Unit,
    vm: AddPortfolioItemViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(state.saved) { if (state.saved) onBack() }

    MSABackground {
        Column(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
            MSATopBar(
                title = stringResource(R.string.portfolio_add),
                showBack = true,
                onBack = onBack
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FieldLabel(stringResource(R.string.portfolio_asset_type))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(AssetType.values().toList()) { type ->
                        AssetChip(
                            label = type.displayAr,
                            selected = state.type == type,
                            onClick = { vm.setType(type) }
                        )
                    }
                }

                FieldLabel(
                    if (state.type.measuredByWeight)
                        stringResource(R.string.portfolio_quantity_grams)
                    else
                        stringResource(R.string.portfolio_quantity_count)
                )
                NumberField(
                    value = state.quantityText,
                    placeholder = if (state.type.measuredByWeight) "10.5" else "2",
                    onValueChange = vm::setQuantity
                )

                FieldLabel(stringResource(R.string.portfolio_total_price_egp))
                NumberField(
                    value = state.totalPriceText,
                    placeholder = "10000",
                    onValueChange = vm::setTotalPrice
                )

                if (state.quantity > 0 && state.totalPrice > 0) {
                    val unit = if (state.type.measuredByWeight) "جم" else "وحدة"
                    Text(
                        text = "≈ ${"%,.2f".format(Locale.US, state.totalPrice / state.quantity)} ج.م / $unit",
                        color = GoldenBorder, fontSize = 12.sp, fontWeight = FontWeight.Medium
                    )
                }

                FieldLabel(stringResource(R.string.portfolio_purchase_date))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xCC1C1C1E))
                        .border(1.dp, GoldenBorder.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .clickable { showDatePicker = true }
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.CalendarMonth, null,
                         tint = GoldenBorder, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
                            .format(Date(state.purchaseDate)),
                        color = Color.White, fontSize = 15.sp
                    )
                }

                FieldLabel(stringResource(R.string.portfolio_notes_optional))
                OutlinedTextField(
                    value = state.notes,
                    onValueChange = vm::setNotes,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(stringResource(R.string.portfolio_notes_placeholder),
                             color = Color(0xFF777777))
                    },
                    colors = darkOutlinedFieldColors()
                )

                if (state.error != null) {
                    Text(state.error!!, color = Color(0xFFE74C3C), fontSize = 13.sp)
                }

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = vm::save,
                    enabled = state.isValid && !state.saving,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldenBorder,
                        disabledContainerColor = Color(0xFF555555)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (state.saving) {
                        CircularProgressIndicator(color = Color.Black, strokeWidth = 2.dp,
                                                  modifier = Modifier.size(20.dp))
                    } else {
                        Text(stringResource(R.string.portfolio_save),
                             color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                Spacer(Modifier.height(40.dp))
            }
        }
    }

    if (showDatePicker) {
        val dateState = rememberDatePickerState(initialSelectedDateMillis = state.purchaseDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let { vm.setPurchaseDate(it) }
                    showDatePicker = false
                }) { Text(stringResource(R.string.portfolio_save), color = GoldenBorder) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.portfolio_cancel), color = Color(0xFFCCCCCC))
                }
            },
            colors = DatePickerDefaults.colors(containerColor = Color(0xFF1C1C1E))
        ) { DatePicker(state = dateState) }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(text, color = Color(0xFFCCCCCC), fontSize = 13.sp, fontWeight = FontWeight.Medium)
}

@Composable
private fun AssetChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg     = if (selected) GoldenBorder else Color(0xCC1C1C1E)
    val fg     = if (selected) Color.Black else Color.White
    val border = if (selected) Color.Transparent else GoldenBorder.copy(alpha = 0.4f)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(label, color = fg, fontSize = 13.sp,
             fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
    }
}

@Composable
private fun NumberField(value: String, placeholder: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = Color(0xFF777777)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        colors = darkOutlinedFieldColors()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun darkOutlinedFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = GoldenBorder,
    unfocusedBorderColor = Color(0xFF555555),
    cursorColor = GoldenBorder,
    focusedContainerColor = Color(0xCC1C1C1E),
    unfocusedContainerColor = Color(0xCC1C1C1E)
)
