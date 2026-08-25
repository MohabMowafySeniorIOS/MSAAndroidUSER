package com.msa.android.presentation.screens.policy

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.msa.android.domain.model.PageType
import com.msa.android.presentation.common.components.MSABackground
import com.msa.android.presentation.common.components.MSATopBar

@Composable
fun PolicyScreen(
    type: PageType,
    onBack: () -> Unit,
    vm: PolicyViewModel = hiltViewModel()
) {
    LaunchedEffect(type) { vm.load(type) }
    val state by vm.state.collectAsStateWithLifecycle()
    val isAr = state.language == "ar"
    val page = state.page

    val titleRes = when (type) {
        PageType.PRIVACY -> R.string.privacy_policy
        PageType.TERMS   -> R.string.usage_policy
        PageType.REFUND  -> R.string.refund_policy
        PageType.ABOUT_US -> R.string.about_us
    }

    MSABackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            MSATopBar(title = stringResource(titleRes), onBack = onBack)

            Spacer(Modifier.height(8.dp))

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = if (isAr) page?.titleAr.orEmpty() else page?.titleEn.orEmpty(),
                    color = Color.White,
                    fontSize = 18.sp,
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
