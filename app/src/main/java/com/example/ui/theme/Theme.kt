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

private val DarkColorScheme =
  darkColorScheme(
      primary = AccLight,
      secondary = GreenFinanceLight,
      tertiary = GreenFinanceDark,
      background = DarkBackground,
      surface = DarkSurface,
      onPrimary = DarkText,
      onSecondary = DarkText,
      onTertiary = LightText,
      onBackground = LightText,
      onSurface = LightText,
      error = ErrorColor
  )

private val LightColorScheme =
  lightColorScheme(
      primary = AccDark,
      secondary = GreenFinanceDark,
      tertiary = GreenFinanceLight,
      background = Color(0xFFF5F5F5),
      surface = Color.White,
      onPrimary = Color.White,
      onSecondary = Color.White,
      onTertiary = DarkText,
      onBackground = DarkText,
      onSurface = DarkText,
      error = Color(0xFFB00020)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, shapes = AppShapes, content = content)
}
