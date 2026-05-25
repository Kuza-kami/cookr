package com.example.presentation.core.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.presentation.CookrThemeSelection
import com.example.presentation.ThemeMode

// 1. CITRUS FUSION (Bright, playful Orange/Yellow Neobrutalist)
private val CitrusLightColors = lightColorScheme(
    primary = Color(0xFF313131),      // Dark Charcoal
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD4E95A), // Ghibli Lime-green Container
    onPrimaryContainer = Color(0xFF313131),
    secondary = Color(0xFFD4E95A),    // Lime Green
    onSecondary = Color(0xFF313131),
    secondaryContainer = Color(0xFFF79ACC), // Coral Peach Accent
    onSecondaryContainer = Color(0xFF313131),
    tertiary = Color(0xFFB9D7FB),      // Pale Blue accent
    onTertiary = Color(0xFF313131),
    background = Color(0xFFF6FBDE),   // Beautiful Cozy Ghibli Warm Cream
    onBackground = Color(0xFF313131),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF313131),
    surfaceVariant = Color(0xFFFFF7E6), // Light warm honey
    onSurfaceVariant = Color(0xFF313131),
    outline = Color(0xFF313131)       // High contrast border
)

// 2. FOREST SAGE (Healthy organic green/nature)
private val ForestLightColors = lightColorScheme(
    primary = Color(0xFF2E7D32),      // Organic Leafy Green
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFC8E6C9),
    onPrimaryContainer = Color(0xFF09300B),
    secondary = Color(0xFF81C784),    // Sage herb
    onSecondary = Color(0xFF1B3022),
    secondaryContainer = Color(0xFFE8F5E9),
    onSecondaryContainer = Color(0xFF1B3022),
    tertiary = Color(0xFF4CAF50),
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFF1F8E9),   // Nature-infused Background
    onBackground = Color(0xFF1B3022),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1B3022),
    surfaceVariant = Color(0xFFE8F5E9),
    onSurfaceVariant = Color(0xFF1B3022),
    outline = Color(0xFF1B3022)
)

// 3. COSMIC TWILIGHT (Premium electric galaxy dark theme)
private val CosmicDarkColors = darkColorScheme(
    primary = Color(0xFF9166FF),      // Cyber Lavender Neon
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF311B92),
    onPrimaryContainer = Color(0xFFEDE7F6),
    secondary = Color(0xFFE040FB),    // Neon Pink Magenta
    onSecondary = Color(0xFF000000),
    secondaryContainer = Color(0xFF4A0072),
    onSecondaryContainer = Color(0xFFF50057),
    tertiary = Color(0xFF00E5FF),
    onTertiary = Color(0xFF000000),
    background = Color(0xFF0E0E18),   // Outer Space Deep Dark
    onBackground = Color(0xFFEDE7F6),
    surface = Color(0xFF1B1B29),      // Nebula Dark Surface cards
    onSurface = Color(0xFFEDE7F6),
    surfaceVariant = Color(0xFF2C2C3E),
    onSurfaceVariant = Color(0xFFEDE7F6),
    outline = Color(0xFFEDE7F6)       // Glowing High-contrast white borders
)

// 4. ROYAL LAPIS (Deep ocean indigo/blue)
private val LapisLightColors = lightColorScheme(
    primary = Color(0xFF1E3A8A),      // Deep Royal Blue
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF1E3A8A),
    secondary = Color(0xFF3B82F6),    // Sky blue
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFEFF6FF),
    onSecondaryContainer = Color(0xFF1E3A8A),
    tertiary = Color(0xFF60A5FA),
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFF8FAFC),   // Ice cool background
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF1E3A8A),
    outline = Color(0xFF1E3A8A)
)

// 5. CRIMSON ROSE (Rich cherry blossom red)
private val CrimsonLightColors = lightColorScheme(
    primary = Color(0xFFB91C1C),      // Crimson Red
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFEE2E2),
    onPrimaryContainer = Color(0xFFB91C1C),
    secondary = Color(0xFFF87171),    // Warm Rose
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFF1F1),
    onSecondaryContainer = Color(0xFFB91C1C),
    tertiary = Color(0xFFFCA5A5),
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFFFFDFD),   // Soft Reddish Milk background
    onBackground = Color(0xFF450A0A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF450A0A),
    surfaceVariant = Color(0xFFFFF5F5),
    onSurfaceVariant = Color(0xFFB91C1C),
    outline = Color(0xFFB91C1C)
)

// 6. SWEET LAVENDER (Delicate pastel amethyst/purple)
private val LavenderLightColors = lightColorScheme(
    primary = Color(0xFF6D28D9),      // Deep Violet Amethyst
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFF5F3FF),
    onPrimaryContainer = Color(0xFF6D28D9),
    secondary = Color(0xFFA78BFA),    // Lilac bloom
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFAF5FF),
    onSecondaryContainer = Color(0xFF6D28D9),
    tertiary = Color(0xFFC4B5FD),
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFFAF5FF),   // Soft Lavender Breeze background
    onBackground = Color(0xFF2E1065),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF2E1065),
    surfaceVariant = Color(0xFFF3E8FF),
    onSurfaceVariant = Color(0xFF6D28D9),
    outline = Color(0xFF6D28D9)
)

@Composable
fun CookrTheme(
    themeSelection: CookrThemeSelection = CookrThemeSelection.CITRUS_FUSION,
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> when (themeSelection) {
            CookrThemeSelection.CITRUS_FUSION -> CitrusLightColors
            CookrThemeSelection.FOREST_SAGE -> ForestLightColors
            CookrThemeSelection.COSMIC_TWILIGHT -> CosmicDarkColors
            CookrThemeSelection.ROYAL_LAPIS -> LapisLightColors
            CookrThemeSelection.CRIMSON_ROSE -> CrimsonLightColors
            CookrThemeSelection.SWEET_LAVENDER -> LavenderLightColors
        }
    }
    
    val view = LocalView.current
    val darkIcons = !darkTheme && (!dynamicColor || Build.VERSION.SDK_INT < Build.VERSION_CODES.S || colorScheme != CosmicDarkColors)

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                try {
                    window.statusBarColor = colorScheme.background.toArgb()
                    window.navigationBarColor = colorScheme.background.toArgb()
                    WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkIcons
                    WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = darkIcons
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
