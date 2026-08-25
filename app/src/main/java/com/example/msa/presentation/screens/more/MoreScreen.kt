package com.msa.android.presentation.screens.more

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.msa.android.R
import com.msa.android.presentation.common.components.MSABackground
import com.msa.android.presentation.common.components.MSATopBar
import com.msa.android.presentation.theme.GoldenBorder

/**
 * More screen — list of secondary actions, exact icon parity with iOS MoreVC.
 * Each row uses the corresponding ic_more_* PNG copied from iOS MoreIcons asset catalog.
 */
@Composable
fun MoreScreen(
    onGoldSilverCalculator: () -> Unit,
    onHelp: () -> Unit,
    onFaq: () -> Unit,
    onAbout: () -> Unit,
    onUsagePolicy: () -> Unit,
    onRefundPolicy: () -> Unit,
    onPrivacyPolicy: () -> Unit,
    onIndicators: () -> Unit,
    onTechnicalAnalysis: () -> Unit,
    onPortfolio: () -> Unit,
    vm: MoreViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val ctx = LocalContext.current

    MSABackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 96.dp)
        ) {
            MSATopBar(
                title = stringResource(R.string.more_title),
                showBack = false,
                showShare = true,
                onShare = { com.msa.android.presentation.common.shareApp(ctx) }
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // iOS shows this entry first in the More tab — opens the
                // calculator hub (gold value / gold zakat / silver value / silver zakat).
               // MoreItem(stringResource(R.string.more_portfolio), R.drawable.ic_more_money_back, onClick = onPortfolio)
                MoreItem(stringResource(R.string.more_gold_silver_calculator), R.drawable.coins_01, onClick = onGoldSilverCalculator)
                MoreItem(stringResource(R.string.help),           R.drawable.ic_more_help,         onClick = onHelp)
                MoreItem(stringResource(R.string.faq),            R.drawable.ic_more_info,         onClick = onFaq)
                MoreItem(stringResource(R.string.about_us),       R.drawable.ic_more_organization, onClick = onAbout)
                MoreItem(stringResource(R.string.usage_policy),   R.drawable.ic_more_terms,        onClick = onUsagePolicy)
                MoreItem(stringResource(R.string.refund_policy),  R.drawable.ic_more_money_back,   onClick = onRefundPolicy)
                MoreItem(stringResource(R.string.privacy_policy), R.drawable.ic_more_insurance,    onClick = onPrivacyPolicy)
//                MoreItem(stringResource(R.string.indicators),         R.drawable.ic_more_info,    onClick = onIndicators)
//                MoreItem(stringResource(R.string.technical_analysis), R.drawable.ic_more_organization, onClick = onTechnicalAnalysis)

                MoreItem(stringResource(R.string.share_app), R.drawable.ic_more_shared) {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT,
                            "https://play.google.com/store/apps/details?id=${ctx.packageName}")
                    }
                    ctx.startActivity(Intent.createChooser(intent, null))
                }
                MoreItem(stringResource(R.string.rate_app), R.drawable.ic_more_rating) {
                    val pkg = ctx.packageName
                    runCatching {
                        ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg")))
                    }.onFailure {
                        ctx.startActivity(Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=$pkg")))
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

//            Text(
//                text = stringResource(R.string.version_label, state.versionName),
//                color = Color.White,
//                fontSize = 12.sp,
//                modifier = Modifier.fillMaxWidth(),
//                textAlign = TextAlign.Center
//            )
//            Spacer(Modifier.height(6.dp))
//            Text(
//                text = stringResource(R.string.developed_by),
//                color = Color(0xFFCCCCCC),
//                fontSize = 12.sp,
//                modifier = Modifier.fillMaxWidth(),
//                textAlign = TextAlign.Center
//            )
        }
    }
}

@Composable
private fun MoreItem(label: String, iconRes: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x66000000))
            .border(1.dp, GoldenBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = label,
            modifier = Modifier.size(26.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(text = label, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.weight(1f))
        // Auto-mirrored chevron: points right (▷) in LTR, left (◁) in RTL Arabic.
        // Vector drawable has android:autoMirrored="true" — Android flips it automatically.
        Image(
            painter = painterResource(R.drawable.ic_chevron),
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
    }
}
