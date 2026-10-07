package com.mughalarts.gownordermanager.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Navy,
    onPrimary = Color.White,
    secondary = Gold,
    onSecondary = Navy,
    tertiary = SuccessGreen,
    onTertiary = Color.White,
    background = AppBackground,
    onBackground = TextPrimary,
    surface = Color.White,
    onSurface = TextPrimary
)

@Composable
fun GownOrderManagerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content
    )
}
