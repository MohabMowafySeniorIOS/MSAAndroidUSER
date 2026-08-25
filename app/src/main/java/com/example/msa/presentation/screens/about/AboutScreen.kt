package com.msa.android.presentation.screens.about

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.msa.android.R
import com.msa.android.presentation.common.components.MSABackground
import com.msa.android.presentation.common.components.MSATopBar

@Composable
fun AboutScreen(
    onBack: () -> Unit,
    vm: AboutViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val isAr = state.language == "ar"
    val page = state.page

    MSABackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            MSATopBar(title = stringResource(R.string.about_us), onBack = onBack)

            Spacer(Modifier.height(8.dp))

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = if (isAr) page?.titleAr.orEmpty() else page?.titleEn.orEmpty(),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    text = if (isAr) page?.contentAr.orEmpty() else page?.contentEn.orEmpty(),
                    color = Color(0xFFE0E0E0),
                    fontSize = 14.sp
                )
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}
