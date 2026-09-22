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
import com.msa.android.presentation.theme.MSADisplayFamily

@Composable
fun RegisterScreen(
    onBack: () -> Unit,
    onCodeSent: (String) -> Unit,
    onGoLogin: () -> Unit,
    vm: AuthViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var touched by remember { mutableStateOf(false) }

    val nameBad = name.trim().length < 3
    val emailBad = !email.matches(Regex("^[^@\\s]+@[^@\\s]+\\.[A-Za-z]{2,}$"))
    val phoneBad = !phone.matches(Regex("^01[0125]\\d{8}$"))
    val passBad = password.length < 6
    val confirmBad = confirm != password

    // التسجيل نجح والكود اتبعت — نروح لشاشة التأكيد
    LaunchedEffect(state.pendingPhone) {
        state.pendingPhone?.let { onCodeSent(it) }
    }

    MSABackground {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            MSATopBar(title = stringResource(R.string.auth_register_title), onBack = onBack)

            Column(
                modifier = Modifier.padding(horizontal = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.auth_register_title),
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = MSADisplayFamily
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.auth_register_hint),
                    color = Color(0xFFB0AAAA),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(22.dp))

                AuthField(
                    label = stringResource(R.string.auth_name),
                    value = name,
                    onChange = { name = it; vm.clearError() },
                    error = touched && nameBad
                )
                Spacer(Modifier.height(14.dp))

                AuthField(
                    label = stringResource(R.string.auth_email),
                    value = email,
                    onChange = { email = it; vm.clearError() },
                    keyboard = KeyboardType.Email,
                    ltr = true,
                    error = touched && emailBad
                )
                Spacer(Modifier.height(14.dp))

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
                Spacer(Modifier.height(14.dp))

                AuthField(
                    label = stringResource(R.string.auth_confirm_password),
                    value = confirm,
                    onChange = { confirm = it; vm.clearError() },
                    isPassword = true,
                    ltr = true,
                    error = touched && confirmBad
                )

                // رسالة تحقق محلية قبل ما نتعب السيرفر
                val localError = when {
                    !touched -> null
                    nameBad -> stringResource(R.string.auth_err_name)
                    emailBad -> stringResource(R.string.auth_err_email)
                    phoneBad -> stringResource(R.string.auth_err_phone)
                    passBad -> stringResource(R.string.auth_err_password)
                    confirmBad -> stringResource(R.string.auth_err_confirm)
                    else -> null
                }

                (state.error ?: localError)?.let {
                    Spacer(Modifier.height(16.dp))
                    AuthError(it)
                }

                Spacer(Modifier.height(24.dp))

                GoldButton(
                    text = stringResource(R.string.auth_register_action),
                    loading = state.busy
                ) {
                    touched = true
                    if (!nameBad && !emailBad && !phoneBad && !passBad && !confirmBad) {
                        vm.register(name, email, phone, password, confirm)
                    }
                }

                Spacer(Modifier.height(14.dp))

                GoldButton(
                    text = stringResource(R.string.auth_go_login),
                    filled = false,
                    big = false,
                    onClick = onGoLogin
                )

                Spacer(Modifier.height(30.dp))
            }
        }
    }
}
