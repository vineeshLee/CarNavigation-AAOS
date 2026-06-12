package com.polestar.navigation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = KineticGold,
    onPrimary = OnKineticGold,
    primaryContainer = KineticGold,
    onPrimaryContainer = OnKineticGold,
    secondary = TextSecondary,
    onSecondary = Obsidian,
    background = Obsidian,
    onBackground = TextPrimary,
    surface = GraphiteCard,
    onSurface = TextPrimary,
    surfaceVariant = BlackBackground,
    onSurfaceVariant = TextSecondary,
    outline = OutlineBorder,
    error = ErrorRed,
    onError = OnErrorContainer,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer
)

@Composable
fun CarNavigationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
