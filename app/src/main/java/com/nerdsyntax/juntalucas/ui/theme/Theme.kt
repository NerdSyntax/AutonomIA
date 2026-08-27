package com.nerdsyntax.juntalucas.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    secondary = GreenSecondary
)

private val DarkColorScheme = darkColorScheme(
    primary = GreenSecondary,
    secondary = GreenPrimary,
    tertiary = GreenDark
)

@Composable
fun JuntaLucasTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) {
            DarkColorScheme
        } else {
            LightColorScheme
        },
        typography = JuntaLucasTypography,
        content = content
    )
}