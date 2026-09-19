package com.postiz.mobile.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Matches the actual Postiz web app's palette, pulled from
 * shacky-postiz's apps/frontend/src/app/colors.scss (--new-* and
 * --color-* custom properties), not invented.
 */

// Brand -- same purple in both themes.
val PostizPurple = Color(0xFF612BD3) // --new-btn-primary / --color-forth
val PostizPurpleBright = Color(0xFF7236F1) // --color-seventh

// Light theme (colors.scss .light)
val LightBg = Color(0xFFF0F2F4) // --new-bgColor
val LightSurface = Color(0xFFFFFFFF) // --new-bgColorInner
val LightBorder = Color(0xFFEAECEE) // --new-border
val LightText = Color(0xFF0E0E0E) // --new-textColor
val LightMuted = Color(0xFF777B7F) // --new-textItemBlur

// Dark theme (colors.scss .dark)
val DarkBg = Color(0xFF0E0E0E) // --new-bgColor
val DarkSurface = Color(0xFF1A1919) // --new-bgColorInner
val DarkBorder = Color(0xFF252525) // --new-border
val DarkText = Color(0xFFFFFFFF) // --new-textColor
val DarkMuted = Color(0xFF999999) // --new-textItemBlur

// Same in both themes (--color-custom22).
val PostizError = Color(0xFFB91C1C)
val PostizSuccess = Color(0xFF32D583) // --color-custom42
