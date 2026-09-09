package com.example.tdm.model

enum class ThemeMode(val label: String, val iconDescription: String) {
  SYSTEM("System", "Follow Android system setting"),
  LIGHT("Light", "Crisp high-contrast clinical day mode"),
  DARK("Dark", "Eye-safe OLED dark night mode")
}
