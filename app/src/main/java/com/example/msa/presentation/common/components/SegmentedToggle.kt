package com.msa.android.presentation.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Reusable pill segmented control — used for both Home (gold/silver) and
 * News (الأخبار / أخبار MSA) tabs. Same shape: outer dark rounded
 * container with a single gold-filled segment indicating the selection.
 */
@Composable
fun <T> SegmentedToggle(
    selected: T,
    options: List<Pair<T, String>>,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 54.dp,
    outerCorner: Dp = 27.dp,
    innerCorner: Dp = 24.dp,
    outerBg: Color = Color(0xFF1A1A1A),
    selectedBg: Color = Color(0xFFC9A05A),
    selectedFg: Color = Color.Black,
    unselectedFg: Color = Color.White,
    fontSize: Int = 16
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(outerCorner))
            .background(outerBg)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        options.forEach { (value, label) ->
            val isSelected = value == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(innerCorner))
                    .background(if (isSelected) selectedBg else Color.Transparent)
                    .clickable { onSelect(value) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (isSelected) selectedFg else unselectedFg,
                    fontSize = fontSize.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
