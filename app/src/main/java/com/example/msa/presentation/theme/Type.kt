package com.msa.android.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.msa.android.R

/**
 * IBM Plex Sans Arabic font family — same family as the iOS app.
 * Files are in res/font/:
 *   ibm_plex_sans_arabic_regular.ttf
 *   ibm_plex_sans_arabic_medium.ttf
 *   ibm_plex_sans_arabic_bold.ttf
 *
 * Note: there's no SemiBold TTF in the provided pack, so SemiBold is mapped to Medium.
 */
val IBMPlexArabic: FontFamily = FontFamily(
    Font(R.font.ibm_plex_sans_arabic_regular, FontWeight.Normal),
    Font(R.font.ibm_plex_sans_arabic_medium,  FontWeight.Medium),
    Font(R.font.ibm_plex_sans_arabic_medium,  FontWeight.SemiBold), // fallback to Medium
    Font(R.font.ibm_plex_sans_arabic_bold,    FontWeight.Bold),
)

val MSAFontFamily: FontFamily = IBMPlexArabic

val MSATypography = Typography(
    displayLarge   = TextStyle(fontFamily = MSAFontFamily, fontWeight = FontWeight.Bold,     fontSize = 32.sp, lineHeight = 40.sp),
    headlineLarge  = TextStyle(fontFamily = MSAFontFamily, fontWeight = FontWeight.Bold,     fontSize = 26.sp, lineHeight = 34.sp),
    headlineMedium = TextStyle(fontFamily = MSAFontFamily, fontWeight = FontWeight.Bold,     fontSize = 22.sp, lineHeight = 28.sp),
    titleLarge     = TextStyle(fontFamily = MSAFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 26.sp),
    titleMedium    = TextStyle(fontFamily = MSAFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 24.sp),
    titleSmall     = TextStyle(fontFamily = MSAFontFamily, fontWeight = FontWeight.Medium,   fontSize = 16.sp, lineHeight = 22.sp),
    bodyLarge      = TextStyle(fontFamily = MSAFontFamily, fontWeight = FontWeight.Normal,   fontSize = 16.sp, lineHeight = 22.sp),
    bodyMedium     = TextStyle(fontFamily = MSAFontFamily, fontWeight = FontWeight.Normal,   fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall      = TextStyle(fontFamily = MSAFontFamily, fontWeight = FontWeight.Normal,   fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge     = TextStyle(fontFamily = MSAFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium    = TextStyle(fontFamily = MSAFontFamily, fontWeight = FontWeight.Medium,   fontSize = 12.sp, lineHeight = 16.sp),
)
