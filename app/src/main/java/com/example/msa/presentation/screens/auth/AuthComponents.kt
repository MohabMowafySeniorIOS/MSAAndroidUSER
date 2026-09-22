package com.msa.android.presentation.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.msa.android.R
import com.msa.android.presentation.theme.Gold
import com.msa.android.presentation.theme.MSAFontFamily
import com.msa.android.presentation.theme.GoldenBorder

/**
 * حقل إدخال بنفس ديزاين التطبيق — خلفية سودا شفافة وحدّ ذهبي،
 * والحدّ بيبقى أحمر لو فيه خطأ.
 */
@Composable
fun AuthField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    keyboard: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    ltr: Boolean = false,
    error: Boolean = false,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }

    Column(modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = if (error) Color(0xFFE0523F) else Color(0xFFC6C6C6),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        // أرقام الموبايل وكلمات السر بتتكتب من الشمال لليمين
        // حتى والواجهة عربي، وإلا المؤشر بيقفز.
        CompositionLocalProvider(
            LocalLayoutDirection provides if (ltr) LayoutDirection.Ltr else LocalLayoutDirection.current
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onChange,
                singleLine = true,
                // TextStyle الصريحة بتتخطّى LocalTextStyle، فلازم نحدد
                // الخط هنا كمان وإلا الحقل يطلع بخط النظام
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = Color.White, fontSize = 15.sp, fontFamily = MSAFontFamily
                ),
                visualTransformation = if (isPassword && !visible)
                    PasswordVisualTransformation() else VisualTransformation.None,
                keyboardOptions = KeyboardOptions(keyboardType = keyboard),
                trailingIcon = if (isPassword) {
                    {
                        IconButton(onClick = { visible = !visible }) {
                            Icon(
                                painter = painterResource(
                                    if (visible) R.drawable.ic_eye_off else R.drawable.ic_eye
                                ),
                                contentDescription = null,
                                tint = Color(0xFFB0AAAA)
                            )
                        }
                    }
                } else null,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Black.copy(alpha = 0.35f),
                    unfocusedContainerColor = Color.Black.copy(alpha = 0.35f),
                    focusedBorderColor = if (error) Color(0xFFE0523F) else Gold,
                    unfocusedBorderColor = if (error) Color(0xFFE0523F) else GoldenBorder.copy(alpha = 0.5f),
                    cursorColor = Gold
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/** رسالة خطأ فوق الزرار */
@Composable
fun AuthError(message: String, modifier: Modifier = Modifier) {
    Text(
        text = message,
        color = Color(0xFFE0523F),
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0x22E0523F), RoundedCornerShape(10.dp))
            .border(1.dp, Color(0x55E0523F), RoundedCornerShape(10.dp))
            .padding(12.dp)
    )
}
