package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val FitTrackDarkColorScheme = darkColorScheme(
    primary = OrangePrimary,
    onPrimary = Color.Black,
    primaryContainer = OrangePrimary.copy(alpha = 0.2f),
    onPrimaryContainer = OrangePrimary,
    secondary = OrangeAccent,
    onSecondary = Color.Black,
    background = DarkBg,
    onBackground = LightText,
    surface = CardBg,
    onSurface = LightText,
    surfaceVariant = SurfaceDark,
    onSurfaceVariant = MutedText,
    error = AccentError,
    onError = Color.Black
)

@Composable
fun FitTrackTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = DeepBlack.toArgb()
            window.navigationBarColor = DeepBlack.toArgb()
            
            val controller = WindowCompat.getInsetsController(window, view)
            // We want status bar icons to be white in dark theme
            controller.isAppearanceLightStatusBars = false
            controller.isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = FitTrackDarkColorScheme,
        typography = Typography,
        content = content
    )
}
