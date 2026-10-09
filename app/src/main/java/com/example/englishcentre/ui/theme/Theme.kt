package com.example.englishcentre.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = NightInk, onPrimary = Color(0xFF0B1F66),
    primaryContainer = NightInkTint, onPrimaryContainer = BallpointTint,
    secondary = Color(0xFFC9D3EA), secondaryContainer = NightHighlighter, onSecondaryContainer = Highlighter,
    tertiary = NightTick, tertiaryContainer = Color(0xFF0F4A33), onTertiaryContainer = Color(0xFFD3F2E3),
    error = NightRedPen, errorContainer = Color(0xFF5C1A1A), onErrorContainer = Color(0xFFFDE2E2),
    background = Night, onBackground = Chalk, surface = Night, onSurface = Chalk,
    surfaceVariant = Color(0xFF2A3550), onSurfaceVariant = ChalkSoft,
    surfaceContainerLowest = Color(0xFF0B111F), surfaceContainerLow = Color(0xFF121A2C),
    surfaceContainer = Color(0xFF151E33), surfaceContainerHigh = Color(0xFF19233A), surfaceContainerHighest = NightCard,
    outline = Color(0xFF3D4A66), outlineVariant = Color(0xFF2A3550)
)

private val LightColorScheme = lightColorScheme(
    primary = Ballpoint, onPrimary = Color.White,
    primaryContainer = BallpointTint, onPrimaryContainer = Color(0xFF0B1F66),
    secondary = Ink, secondaryContainer = Highlighter, onSecondaryContainer = Color(0xFF4A3B00),
    tertiary = Tick, tertiaryContainer = Color(0xFFD3F2E3), onTertiaryContainer = Color(0xFF0F4A33),
    error = RedPen, errorContainer = Color(0xFFFDE2E2), onErrorContainer = Color(0xFF5C1A1A),
    background = Paper, onBackground = Ink, surface = Paper, onSurface = Ink,
    surfaceVariant = Color(0xFFE3E8F1), onSurfaceVariant = InkSoft,
    surfaceContainerLowest = PaperCard, surfaceContainerLow = Color(0xFFF9FAFD),
    surfaceContainer = Color(0xFFE9EDF5), surfaceContainerHigh = Color(0xFFF1F4F9), surfaceContainerHighest = PaperCard,
    outline = Color(0xFFC5CDDB), outlineVariant = Color(0xFFDDE3EC)
)

@Composable
fun EnglishCentreTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+; off so the centre's own palette is used
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
