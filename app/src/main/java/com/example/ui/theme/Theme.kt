package com.example.ui.theme

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
    primary = HighDensityPrimaryDark,
    primaryContainer = HighDensityPrimaryContainerDark,
    onPrimaryContainer = HighDensityOnPrimaryContainerDark,
    secondary = HighDensitySecondaryDark,
    secondaryContainer = HighDensitySecondaryContainerDark,
    tertiary = SavingsTeal,
    background = HighDensityBgDark,
    surface = HighDensitySurfaceDark,
    outline = HighDensityOutlineDark,
    onPrimary = Color(0xFF001D36),
    onSecondary = Color(0xFF001D36),
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC),
    onSurfaceVariant = Color(0xFF94A3B8)
)

private val LightColorScheme = lightColorScheme(
    primary = HighDensityPrimaryLight,
    primaryContainer = HighDensityPrimaryContainerLight,
    onPrimaryContainer = HighDensityOnPrimaryContainerLight,
    secondary = HighDensitySecondaryLight,
    secondaryContainer = HighDensitySecondaryContainerLight,
    tertiary = SavingsTeal,
    background = HighDensityBgLight,
    surface = Color.White,
    outline = HighDensityOutlineLight,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF1E293B),
    onSurface = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFF64748B)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // We set dynamicColor false by default to retain the custom Emerald look,
    // but the system-adaptive light/dark state applies as the user requested.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
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
