package com.example

import android.os.Bundle
import androidx.compose.runtime.SideEffect
import androidx.core.view.WindowCompat
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tdm.model.ThemeMode
import com.example.tdm.ui.TdmApp
import com.example.tdm.viewmodel.TdmViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val viewModel: TdmViewModel = viewModel()
      val themeMode by viewModel.themeMode.collectAsState()
      val systemDark = isSystemInDarkTheme()
      val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
      }

      SideEffect {
        WindowCompat.getInsetsController(window, window.decorView).apply {
          isAppearanceLightStatusBars = !isDark
          isAppearanceLightNavigationBars = !isDark
        }
      }
      MyApplicationTheme(darkTheme = isDark) {
        TdmApp(viewModel = viewModel)
      }
    }
  }
}


