package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
  primary = Color(0xFFA3CEAF),
  onPrimary = Color(0xFF123820),
  primaryContainer = Color(0xFF2D4D37),
  onPrimaryContainer = Color(0xFFDCEBDD),
  secondary = Color(0xFFB7CEB5),
  onSecondary = Color(0xFF233B25),
  secondaryContainer = Color(0xFF354A36),
  onSecondaryContainer = Color(0xFFDFEBDD),
  tertiary = Color(0xFFE4C97D),
  onTertiary = Color(0xFF3D2F05),
  tertiaryContainer = Color(0xFF514319),
  onTertiaryContainer = Color(0xFFF5E8BF),
  background = Color(0xFF151C18),
  onBackground = Color(0xFFE4ECE5),
  surface = Color(0xFF222E26),
  onSurface = Color(0xFFE4ECE5),
  surfaceVariant = Color(0xFF303E33),
  onSurfaceVariant = Color(0xFFBDCCBF),
  outline = Color(0xFF91A494),
  outlineVariant = Color(0xFF4C5F50),
  error = Color(0xFFFFB3B7),
  onError = Color(0xFF640F1B),
  errorContainer = Color(0xFF7D2731),
  onErrorContainer = Color(0xFFFFDADB)
)

private val LightColorScheme = lightColorScheme(
  primary = Color(0xFF3F6B53),
  onPrimary = Color(0xFFFFFFFF),
  primaryContainer = Color(0xFFDCEBDD),
  onPrimaryContainer = Color(0xFF173522),
  secondary = Color(0xFF506653),
  onSecondary = Color(0xFFFFFFFF),
  secondaryContainer = Color(0xFFE1EBDD),
  onSecondaryContainer = Color(0xFF253B29),
  tertiary = Color(0xFF705C28),
  onTertiary = Color(0xFFFFFFFF),
  tertiaryContainer = Color(0xFFF5E8BF),
  onTertiaryContainer = Color(0xFF49390C),
  background = Color(0xFFF5F7F4),
  onBackground = Color(0xFF223128),
  surface = Color(0xFFFFFFFF),
  onSurface = Color(0xFF223128),
  surfaceVariant = Color(0xFFE8EEE6),
  onSurfaceVariant = Color(0xFF4C5E50),
  outline = Color(0xFF6E7E70),
  outlineVariant = Color(0xFFBBC8BA),
  error = Color(0xFFA32932),
  onError = Color(0xFFFFFFFF),
  errorContainer = Color(0xFFFFDADB),
  onErrorContainer = Color(0xFF62141C)
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Keep medical branding consistent
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
