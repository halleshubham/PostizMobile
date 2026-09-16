package com.postiz.mobile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Dynamic (Material You) color is intentionally not used: the editorial
// palette (ivory ground, clay accent, Newsreader/Work Sans) is the brand,
// and letting the wallpaper override it would break that on every device.
private val LightColors = lightColorScheme(
    primary = Clay,
    onPrimary = Ivory,
    primaryContainer = Clay,
    onPrimaryContainer = Ivory,
    secondary = ClayDark,
    onSecondary = Ivory,
    background = Ivory,
    onBackground = Ink,
    surface = Ivory,
    onSurface = Ink,
    surfaceVariant = Ivory,
    onSurfaceVariant = Muted,
    outline = Hairline,
    error = PostizError,
    onError = Ivory
)

private val DarkColors = darkColorScheme(
    primary = Clay,
    onPrimary = InkDarkBg,
    primaryContainer = Clay,
    onPrimaryContainer = InkDarkBg,
    secondary = ClayDark,
    onSecondary = InkDarkText,
    background = InkDarkBg,
    onBackground = InkDarkText,
    surface = InkDarkSurface,
    onSurface = InkDarkText,
    surfaceVariant = InkDarkSurface,
    onSurfaceVariant = InkDarkMuted,
    outline = InkDarkMuted,
    error = PostizError,
    onError = InkDarkBg
)

@Composable
fun PostizMobileTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = PostizTypography,
        content = content
    )
}
