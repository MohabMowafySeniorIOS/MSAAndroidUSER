package com.msa.android.presentation.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.msa.android.presentation.theme.Gold

@Composable
fun ApiLoadingOverlay() {
    Box(
        modifier = Modifier
            .background(Color(0x66000000))
            .clickable(enabled = true, onClick = {}),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Gold, strokeWidth = 3.dp, modifier = Modifier.size(48.dp))
    }
}
