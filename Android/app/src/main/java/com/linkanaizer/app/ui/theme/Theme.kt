package com.linkanaizer.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LinkanazerColorScheme = lightColorScheme(
    primary = PrimaryLight,
    primaryContainer = PrimaryDark,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = CategoryCardBg,
    outline = BorderLight,
    onPrimary = TextOnPrimary,
    background = SurfaceWhite,
    onBackground = TextPrimary,
)

@Composable
fun LinkanazerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LinkanazerColorScheme,
        typography = LinkanazerTypography,
        content = content,
    )
}
