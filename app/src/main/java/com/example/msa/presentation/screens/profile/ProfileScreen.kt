package com.msa.android.presentation.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.msa.android.R
import com.msa.android.presentation.common.components.GoldButton
import com.msa.android.presentation.common.components.MSABackground
import com.msa.android.presentation.common.components.MSATopBar
import com.msa.android.presentation.screens.auth.AuthError
import com.msa.android.presentation.screens.auth.AuthField
import com.msa.android.presentation.theme.Gold
import com.msa.android.presentation.theme.GoldenBorder

private val red = Color(0xFFE0523F)
private val muted = Color(0xFFB0AAAA)

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onSignedOut: () -> Unit,
    vm: ProfileViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()

    var showPasswordDialog by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(state.signedOut) { if (state.signedOut) onSignedOut() }

    MSABackground {
        Column(Modifier.fillMaxSize()) {
            MSATopBar(title = stringResource(R.string.profile_title), onBack = onBack)

            if (state.loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Gold)
                }
                return@Column
            }

            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Card {
                    AuthField(
                        label = stringResource(R.string.auth_name),
                        value = state.name,
                        onChange = vm::onNameChange
                    )
                    Spacer(Modifier.height(14.dp))
                    AuthField(
                        label = stringResource(R.string.auth_email),
                        value = state.email,
                        onChange = vm::onEmailChange,
                        keyboard = KeyboardType.Email,
                        ltr = true
                    )
                    Spacer(Modifier.height(14.dp))

                    // الموبايل مقفول: مربوط بتفعيل بكود، فتغييره لازم
                    // يعدي على نفس مسار التأكيد.
                    Text(
                        text = stringResource(R.string.auth_phone),
                        color = muted, fontSize = 13.sp, fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = state.phone,
                        color = Color.White,
                        fontSize = 15.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.25f))
                            .padding(horizontal = 12.dp, vertical = 14.dp)
                    )
                    Text(
                        text = stringResource(R.string.profile_phone_locked),
                        color = muted, fontSize = 11.sp,
                        modifier = Modifier.padding(top = 5.dp)
                    )

                    state.error?.let {
                        Spacer(Modifier.height(14.dp))
                        AuthError(it)
                    }
                    state.savedMessage?.let {
                        Spacer(Modifier.height(14.dp))
                        Text(
                            text = stringResource(R.string.profile_saved),
                            color = Color(0xFF18A957),
                            fontSize = 13.sp, fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.height(18.dp))
                    GoldButton(
                        text = stringResource(R.string.profile_save),
                        loading = state.busy,
                        onClick = vm::save
                    )
                }

                Card {
                    GoldButton(
                        text = stringResource(R.string.profile_change_password),
                        filled = false, big = false
                    ) { showPasswordDialog = true }
                }

                Card {
                    GoldButton(
                        text = stringResource(R.string.auth_logout),
                        filled = false, big = false
                    ) { showLogoutConfirm = true }

                    Spacer(Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(23.dp))
                            .border(1.5.dp, red.copy(alpha = 0.6f), RoundedCornerShape(23.dp))
                            .clickable { showDeleteConfirm = true }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.profile_delete_account),
                            color = red, fontSize = 14.sp, fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = stringResource(R.string.profile_delete_hint),
                        color = muted, fontSize = 11.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(Modifier.height(30.dp))
            }
        }
    }

    if (showPasswordDialog) {
        ChangePasswordDialog(
            busy = state.busy,
            onDismiss = { showPasswordDialog = false },
            onSubmit = { current, new, confirm, onResult ->
                vm.changePassword(current, new, confirm) { message ->
                    if (message == null) showPasswordDialog = false
                    onResult(message)
                }
            }
        )
    }

    if (showLogoutConfirm) {
        ConfirmDialog(
            title = stringResource(R.string.auth_logout),
            message = stringResource(R.string.profile_logout_confirm),
            confirmText = stringResource(R.string.auth_logout),
            destructive = false,
            onDismiss = { showLogoutConfirm = false },
            onConfirm = { showLogoutConfirm = false; vm.logout() }
        )
    }

    if (showDeleteConfirm) {
        ConfirmDialog(
            title = stringResource(R.string.profile_delete_account),
            message = stringResource(R.string.profile_delete_confirm),
            confirmText = stringResource(R.string.profile_delete_account),
            destructive = true,
            onDismiss = { showDeleteConfirm = false },
            onConfirm = { showDeleteConfirm = false; vm.deleteAccount() }
        )
    }
}

@Composable
private fun Card(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black.copy(alpha = 0.35f))
            .border(1.dp, GoldenBorder.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        content = content
    )
}

@Composable
private fun ChangePasswordDialog(
    busy: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (String, String, String, (String?) -> Unit) -> Unit
) {
    var current by remember { mutableStateOf("") }
    var new by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val shortPassword = stringResource(R.string.auth_err_password)
    val mismatch = stringResource(R.string.auth_err_confirm)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF261B1A),
        title = {
            Text(stringResource(R.string.profile_change_password),
                 color = Color.White, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                AuthField(label = stringResource(R.string.profile_current_password),
                          value = current, onChange = { current = it; error = null },
                          isPassword = true, ltr = true)
                Spacer(Modifier.height(12.dp))
                AuthField(label = stringResource(R.string.profile_new_password),
                          value = new, onChange = { new = it; error = null },
                          isPassword = true, ltr = true)
                Spacer(Modifier.height(12.dp))
                AuthField(label = stringResource(R.string.auth_confirm_password),
                          value = confirm, onChange = { confirm = it; error = null },
                          isPassword = true, ltr = true)
                error?.let {
                    Spacer(Modifier.height(12.dp))
                    AuthError(it)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !busy,
                onClick = {
                    // نتحقق محلياً الأول عشان ما نتعبش السيرفر
                    when {
                        new.length < 6 -> error = shortPassword
                        new != confirm -> error = mismatch
                        else -> onSubmit(current, new, confirm) { message -> error = message }
                    }
                }
            ) {
                Text(stringResource(R.string.wallet_save), color = Gold, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = muted)
            }
        }
    )
}

@Composable
private fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    destructive: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF261B1A),
        title = { Text(title, color = Color.White, fontWeight = FontWeight.Bold) },
        text = { Text(message, color = muted, fontSize = 14.sp) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmText, color = if (destructive) red else Gold,
                     fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = muted)
            }
        }
    )
}
