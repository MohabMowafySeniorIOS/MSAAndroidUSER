package com.msa.android.presentation.screens.help

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.msa.android.R
import com.msa.android.presentation.common.components.MSABackground
import com.msa.android.presentation.common.components.MSATopBar
import com.msa.android.presentation.theme.GoldMain
import com.msa.android.presentation.theme.GoldenBorder

// القيم دي احتياطي بس — الأصل بيجي من لوحة التحكم عبر الـ API،
// ودي بتظهر لو الشبكة فصلت أو لسه المحتوى ما وصلش.
private const val FALLBACK_EMAIL = "mowafymohab@gmail.com"
private const val FALLBACK_PHONE = "+201070000538"
private const val FALLBACK_WEBSITE = "https://msagold.com"

@Composable
fun HelpScreen(
    onBack: () -> Unit,
    vm: HelpViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by vm.state.collectAsStateWithLifecycle()

    // بيانات التواصل من لوحة التحكم، وبترجع للقيم الاحتياطية
    // لو المحتوى لسه ما وصلش أو الحقل فاضي في اللوحة.
    val email = state.contact?.email?.takeIf { it.isNotBlank() } ?: FALLBACK_EMAIL
    val phone = state.contact?.phone?.takeIf { it.isNotBlank() } ?: FALLBACK_PHONE
    val website = state.contact?.social?.get("website")?.takeIf { it.isNotBlank() }
        ?: FALLBACK_WEBSITE

    MSABackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            MSATopBar(title = stringResource(R.string.help), onBack = onBack)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 30.dp),
                verticalArrangement = Arrangement.spacedBy(28.dp)
            ) {
                Text(
                    text = stringResource(R.string.contact_intro),
                    color = Color.White,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    ContactRow(
                        icon = Icons.Filled.Language,
                        title = stringResource(R.string.contact_website_title),
                        value = website,
                        onClick = {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse(website))
                            )
                        }
                    )
                    ContactRow(
                        icon = Icons.Filled.Email,
                        title = stringResource(R.string.contact_email_title),
                        value = email,
                        onClick = {
                            context.startActivity(
                                Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email"))
                            )
                        }
                    )
                    ContactRow(
                        icon = Icons.Filled.Phone,
                        title = stringResource(R.string.contact_phone_title),
                        value = phone,
                        onClick = {
                            val number = phone.replace(" ", "")
                            context.startActivity(
                                Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
                            )
                        }
                    )
                }

             //   AboutCard()

                Spacer(Modifier.height(2.dp))
            }
        }
    }
}

@Composable
private fun ContactRow(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Black.copy(alpha = 0.30f))
            .border(1.dp, GoldMain, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(35.dp)
        )
        Spacer(Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = value,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun AboutCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.Black.copy(alpha = 0.25f))
            .border(1.dp, GoldenBorder.copy(alpha = 0.45f), RoundedCornerShape(24.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = null,
                tint = GoldMain,
                modifier = Modifier.size(26.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.contact_about_title),
                color = GoldMain,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = stringResource(R.string.contact_about_body),
            color = Color.White.copy(alpha = 0.90f),
            fontSize = 15.sp,
            lineHeight = 22.sp
        )
    }
}
