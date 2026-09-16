package com.postiz.mobile.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.postiz.mobile.R

/** Newsreader (variable, opsz+wght) — the editorial display/serif voice. */
val Newsreader = FontFamily(
    Font(R.font.newsreader, weight = FontWeight.Normal),
    Font(
        R.font.newsreader,
        weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(FontVariation.weight(500))
    ),
    Font(
        R.font.newsreader,
        weight = FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(600))
    ),
    Font(R.font.newsreader_italic, weight = FontWeight.Normal, style = FontStyle.Italic),
    Font(
        R.font.newsreader_italic,
        weight = FontWeight.SemiBold,
        style = FontStyle.Italic,
        variationSettings = FontVariation.Settings(FontVariation.weight(600))
    )
)

/** Work Sans (variable, wght) — the sans body/label voice. */
val WorkSans = FontFamily(
    Font(R.font.work_sans, weight = FontWeight.Normal),
    Font(
        R.font.work_sans,
        weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(FontVariation.weight(500))
    ),
    Font(
        R.font.work_sans,
        weight = FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(600))
    )
)

val PostizTypography = Typography(
    titleLarge = TextStyle(
        fontFamily = Newsreader,
        fontStyle = FontStyle.Italic,
        fontWeight = FontWeight.SemiBold,
        fontSize = 30.sp,
        lineHeight = 35.sp
    ),
    titleMedium = TextStyle(
        fontFamily = Newsreader,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = Newsreader,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = WorkSans,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    labelLarge = TextStyle(
        fontFamily = WorkSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        letterSpacing = 1.3.sp
    )
)
