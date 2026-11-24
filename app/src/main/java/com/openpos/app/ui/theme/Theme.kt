package com.openpos.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

val Blue80 = Color(0xFF90CAF9)
val Blue40 = Color(0xFF1565C0)
val Green40 = Color(0xFF2E7D32)
val Amber40 = Color(0xFFF9A825)
val Red40 = Color(0xFFC62828)

private val LightColors = lightColorScheme(
    primary = Blue40,
    secondary = Green40,
    tertiary = Amber40,
    error = Red40
)

private val DarkColors = darkColorScheme(
    primary = Blue80,
    secondary = Green40,
    tertiary = Amber40,
    error = Red40
)

@Composable
fun OpenPosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> DarkColors
        else -> LightColors
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
