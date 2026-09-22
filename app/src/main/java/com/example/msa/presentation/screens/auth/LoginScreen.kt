package com.msa.android.presentation.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
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
import com.msa.android.presentation.theme.MSADisplayFamily

@Composable
fun LoginScreen(
    onBack: () -> Unit,
    onLoggedIn: () -> Unit,
    onGoRegister: () -> Unit,
    onNeedVerify: (String) -> Unit,
    vm: AuthViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var touched by remember { mutableStateOf(false) }

    val phoneBad = !phone.matches(Regex("^01[0125]\\d{8}$"))
    val passBad = password.length < 6

    // الدخول نجح
    LaunchedEffect(state.done) { if (state.done) onLoggedIn() }

    // السيرفر رجّع 409 يعني الرقم لسه مش مفعّل وبعت كود جديد
    LaunchedEffect(state.pendingPhone) {
        state.pendingPhone?.let { onNeedVerify(it) }
    }

    MSABackground {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            MSATopBar(title = stringResource(R.string.auth_login_title), onBack = onBack)

            Column(
                modifier = Modifier.padding(horizontal = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(20.dp))

                Text(
                    text = stringResource(R.string.auth_login_title),
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = MSADisplayFamily
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.auth_login_hint),
                    color = Color(0xFFB0AAAA),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(26.dp))

                AuthField(
                    label = stringResource(R.string.auth_phone),
                    value = phone,
                    onChange = { phone = it.filter(Char::isDigit).take(11); vm.clearError() },
                    keyboard = KeyboardType.Phone,
                    ltr = true,
                    error = touched && phoneBad
                )

                Spacer(Modifier.height(14.dp))

                AuthField(
                    label = stringResource(R.string.auth_password),
                    value = password,
                    onChange = { password = it; vm.clearError() },
                    isPassword = true,
                    ltr = true,
                    error = touched && passBad
                )

                state.error?.let {
                    Spacer(Modifier.height(16.dp))
                    AuthError(it)
                }

                Spacer(Modifier.height(24.dp))

                GoldButton(
                    text = stringResource(R.string.auth_login_action),
                    loading = state.busy
                ) {
                    touched = true
                    if (!phoneBad && !passBad) vm.login(phone, password)
                }

                Spacer(Modifier.height(14.dp))

                GoldButton(
                    text = stringResource(R.string.auth_go_register),
                    filled = false,
                    big = false,
                    onClick = onGoRegister
                )

                Spacer(Modifier.height(30.dp))
            }
        }
    }
}
