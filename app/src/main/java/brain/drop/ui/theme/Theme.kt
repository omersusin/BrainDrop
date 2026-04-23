package brain.drop.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AccentCyan,
    onPrimary = DeepBackground,
    primaryContainer = SurfaceElevated,
    onPrimaryContainer = AccentCyan,
    secondary = AccentLavender,
    onSecondary = DeepBackground,
    secondaryContainer = SurfaceElevated,
    onSecondaryContainer = AccentLavender,
    tertiary = AccentMint,
    onTertiary = DeepBackground,
    tertiaryContainer = SurfaceElevated,
    onTertiaryContainer = AccentMint,
    background = DeepBackground,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextSecondary,
    error = Error,
    onError = DeepBackground,
    outline = SurfaceOverlay,
    outlineVariant = SurfaceOverlay.copy(alpha = 0.5f),
    scrim = Color.Black.copy(alpha = 0.6f)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0284C7),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0C4A6E),
    secondary = Color(0xFF7C3AED),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEDE9FE),
    onSecondaryContainer = Color(0xFF4C1D95),
    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF18181B),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF18181B),
    surfaceVariant = Color(0xFFF4F4F5),
    onSurfaceVariant = Color(0xFF71717A),
    error = Color(0xFFDC2626),
    onError = Color.White
)

val LocalFocusMode = staticCompositionLocalOf { false }

@Composable
fun BrainDropTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    focusMode: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val modifiedColors = if (focusMode && darkTheme) {
        colorScheme.copy(
            primary = TextSecondary,
            secondary = TextMuted,
            tertiary = TextMuted,
            surface = DeepBackground,
            surfaceVariant = DeepBackground
        )
    } else colorScheme

    CompositionLocalProvider(LocalFocusMode provides focusMode) {
        MaterialTheme(
            colorScheme = modifiedColors,
            typography = BrainDropTypography,
            content = content
        )
    }
}
