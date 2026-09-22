package com.msa.android.presentation.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.msa.android.R
import com.msa.android.presentation.common.components.GoldButton
import com.msa.android.presentation.common.components.MSABackground
import com.msa.android.presentation.common.components.MSATopBar
import com.msa.android.presentation.theme.Gold
import com.msa.android.presentation.theme.GoldenBorder
import com.msa.android.presentation.theme.MSADisplayFamily
import com.msa.android.presentation.theme.MSAFontFamily
import kotlinx.coroutines.delay

private const val CODE_LENGTH = 6

@Composable
fun VerifyOtpScreen(
    phone: String,
    onBack: () -> Unit,
    onVerified: () -> Unit,
    vm: AuthViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    var code by remember { mutableStateOf("") }
    val focus = remember { FocusRequester() }

    LaunchedEffect(state.done) { if (state.done) onVerified() }
    LaunchedEffect(Unit) { focus.requestFocus() }

    // عدّاد تنازلي لإعادة الإرسال
    LaunchedEffect(state.resendCooldown) {
        if (state.resendCooldown > 0) {
            delay(1000)
            vm.tickCooldown()
        }
    }

    // لما يكمل الكود بنتحقق تلقائياً — أسرع من إنه يدوس زرار
    LaunchedEffect(code) {
        if (code.length == CODE_LENGTH && !state.busy) vm.verify(phone, code)
    }

    MSABackground {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MSATopBar(title = stringResource(R.string.auth_verify_title), onBack = onBack)

            Spacer(Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.auth_verify_title),
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = MSADisplayFamily
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.auth_verify_hint, phone),
                color = Color(0xFFB0AAAA),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 30.dp)
            )

            Spacer(Modifier.height(30.dp))

            // خانات الكود: حقل واحد مخفي بيتحكم في ستة مربعات معروضة.
            // أبسط وأثبت من ستة حقول منفصلة بتتنطّط بينهم.
            Box(contentAlignment = Alignment.Center) {
                BasicTextField(
                    value = code,
                    onValueChange = {
                        code = it.filter(Char::isDigit).take(CODE_LENGTH)
                        vm.clearError()
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    cursorBrush = SolidColor(Color.Transparent),
                    textStyle = TextStyle(color = Color.Transparent, fontFamily = MSAFontFamily),
                    modifier = Modifier
                        .focusRequester(focus)
                        .size(width = 280.dp, height = 56.dp)
                )

                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        repeat(CODE_LENGTH) { index ->
                            val char = code.getOrNull(index)?.toString() ?: ""
                            val active = index == code.length
                            Box(
                                modifier = Modifier
                                    .size(width = 42.dp, height = 54.dp)
                                    .clip(RoundedCornerShape(11.dp))
                                    .background(Color.Black.copy(alpha = 0.35f))
                                    .border(
                                        width = if (active) 2.dp else 1.5.dp,
                                        color = when {
                                            state.error != null -> Color(0xFFE0523F)
                                            active -> Gold
                                            else -> GoldenBorder.copy(alpha = 0.45f)
                                        },
                                        shape = RoundedCornerShape(11.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = char,
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            state.error?.let {
                Spacer(Modifier.height(18.dp))
                AuthError(it, Modifier.padding(horizontal = 22.dp))
            }

            Spacer(Modifier.height(26.dp))

            GoldButton(
                text = stringResource(R.string.auth_verify_action),
                loading = state.busy,
                enabled = code.length == CODE_LENGTH,
                modifier = Modifier.padding(horizontal = 22.dp)
            ) { vm.verify(phone, code) }

            Spacer(Modifier.height(10.dp))

            TextButton(
                onClick = { vm.resend(phone) },
                enabled = state.resendCooldown == 0 && !state.busy
            ) {
                Text(
                    text = if (state.resendCooldown > 0)
                        stringResource(R.string.auth_resend_in, state.resendCooldown)
                    else stringResource(R.string.auth_resend),
                    color = if (state.resendCooldown > 0) Color(0xFF9E9898) else Gold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
