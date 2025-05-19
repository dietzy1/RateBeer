package dk.grp30.ratebeer.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val primaryColor = Color(0xFFAB7B00) // Amber/golden beer color
private val onPrimaryColor = Color(0xFFFFFFFF)
private val primaryContainerColor = Color(0xFFF5D485) // Light beer foam color
private val onPrimaryContainerColor = Color(0xFF3D2D00)
private val secondaryColor = Color(0xFF815600) // Darker beer color
private val onSecondaryColor = Color(0xFFFFFFFF)
private val secondaryContainerColor = Color(0xFFFFDDB0) // Light amber color
private val onSecondaryContainerColor = Color(0xFF291800)
private val tertiaryColor = Color(0xFF984716) // Brown/red ale color
private val onTertiaryColor = Color(0xFFFFFFFF)
private val tertiaryContainerColor = Color(0xFFFFDBCD) // Light peach color
private val onTertiaryContainerColor = Color(0xFF351000)
private val backgroundColor = Color(0xFFFCF9F6) // Very light cream color
private val onBackgroundColor = Color(0xFF1E1B16)
private val surfaceColor = Color(0xFFFCF9F6) // Very light cream color
private val onSurfaceColor = Color(0xFF1E1B16)

private val DarkColorScheme = darkColorScheme(
    primary = primaryContainerColor,
    onPrimary = onPrimaryContainerColor,
    primaryContainer = primaryColor,
    onPrimaryContainer = onPrimaryColor,
    secondary = secondaryContainerColor,
    onSecondary = onSecondaryContainerColor,
    secondaryContainer = secondaryColor,
    onSecondaryContainer = onSecondaryColor,
    tertiary = tertiaryContainerColor,
    onTertiary = onTertiaryContainerColor,
    tertiaryContainer = tertiaryColor,
    onTertiaryContainer = onTertiaryColor,
    background = Color(0xFF1E1B16),
    onBackground = backgroundColor,
    surface = Color(0xFF1E1B16),
    onSurface = backgroundColor
)

private val LightColorScheme = lightColorScheme(
    primary = primaryColor,
    onPrimary = onPrimaryColor,
    primaryContainer = primaryContainerColor,
    onPrimaryContainer = onPrimaryContainerColor,
    secondary = secondaryColor,
    onSecondary = onSecondaryColor,
    secondaryContainer = secondaryContainerColor,
    onSecondaryContainer = onSecondaryContainerColor,
    tertiary = tertiaryColor,
    onTertiary = onTertiaryColor,
    tertiaryContainer = tertiaryContainerColor,
    onTertiaryContainer = onTertiaryContainerColor,
    background = backgroundColor,
    onBackground = onBackgroundColor,
    surface = surfaceColor,
    onSurface = onSurfaceColor
)

@Composable
fun RateBeerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
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
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
} 