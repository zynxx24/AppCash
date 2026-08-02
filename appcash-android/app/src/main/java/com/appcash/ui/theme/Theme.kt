package com.appcash.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val AppCashColorScheme = lightColorScheme(
    primary            = OrangePrimary,
    onPrimary          = OrangeOnPrimary,
    primaryContainer   = OrangeContainer,
    onPrimaryContainer = OrangeDark,
    secondary          = OrangeLight,
    onSecondary        = OrangeOnPrimary,
    secondaryContainer = OrangeContainer,
    onSecondaryContainer = OrangeDark,
    tertiary           = BlueAccent,
    onTertiary         = WhitePure,
    tertiaryContainer  = OrangeContainer,
    onTertiaryContainer = OrangeDark,
    background         = GreyBg,
    onBackground       = TextDark,
    surface            = GreySurface,
    onSurface          = TextDark,
    surfaceVariant     = GreyCard,
    onSurfaceVariant   = TextMedium,
    error              = ErrorRed,
    onError            = WhitePure,
    outline            = GreyBorder
)

private fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

@Composable
fun AppCashTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            try {
                val activity = view.context.findActivity() ?: return@SideEffect
                val window = activity.window
                @Suppress("DEPRECATION")
                window.statusBarColor = WhitePure.toArgb()
                @Suppress("DEPRECATION")
                window.navigationBarColor = WhitePure.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = true
            } catch (_: Exception) {
                // Silently ignore on devices where this fails
            }
        }
    }
    MaterialTheme(
        colorScheme = AppCashColorScheme,
        typography  = Typography,
        content     = content
    )
}
