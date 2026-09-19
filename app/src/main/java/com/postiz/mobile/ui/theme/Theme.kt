package com.postiz.mobile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// No dynamic (Material You) color: matching the real Postiz web app's fixed
// purple brand, not the device wallpaper.
private val LightColors = lightColorScheme(
    primary = PostizPurple,
    onPrimary = Color.White,
    primaryContainer = PostizPurple,
    onPrimaryContainer = Color.White,
    secondary = PostizPurpleBright,
    onSecondary = Color.White,
    background = LightBg,
    onBackground = LightText,
    surface = LightSurface,
    onSurface = LightText,
    surfaceVariant = LightSurface,
    onSurfaceVariant = LightMuted,
    outline = LightBorder,
    error = PostizError,
    onError = Color.White
)

private val DarkColors = darkColorScheme(
    primary = PostizPurple,
    onPrimary = Color.White,
    primaryContainer = PostizPurple,
    onPrimaryContainer = Color.White,
    secondary = PostizPurpleBright,
    onSecondary = Color.White,
    background = DarkBg,
    onBackground = DarkText,
    surface = DarkSurface,
    onSurface = DarkText,
    surfaceVariant = DarkSurface,
    onSurfaceVariant = DarkMuted,
    outline = DarkBorder,
    error = PostizError,
    onError = Color.White
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
