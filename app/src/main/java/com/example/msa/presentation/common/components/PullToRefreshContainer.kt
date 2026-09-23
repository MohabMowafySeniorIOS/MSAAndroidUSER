package com.msa.android.presentation.common.components

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.msa.android.presentation.theme.GoldenBorder

/** Consistent pull-to-refresh behavior for data-backed Compose screens. */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PullToRefreshContainer(
    refreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val state = rememberPullRefreshState(
        refreshing = refreshing,
        onRefresh = onRefresh
    )

    androidx.compose.foundation.layout.Box(
        modifier = modifier.pullRefresh(state)
    ) {
        content()
        PullRefreshIndicator(
            refreshing = refreshing,
            state = state,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = Color(0xFF1C1414),
            contentColor = GoldenBorder,
            scale = true
        )
    }
}
