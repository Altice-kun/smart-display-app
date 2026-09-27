package com.smartdisplay.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// このディスプレイは常に同じダーク配色で運用する想定（OSのライト/ダーク設定には追従しない）。
val BgColor = Color(0xFF0B0F1A)
val PanelColor = Color(0xFF131A2B)
val PanelAltColor = Color(0xFF1C263C)
val LineColor = Color(0xFF26314A)
val TextColor = Color(0xFFEEF1F8)
val TextDimColor = Color(0xFF8C96AD)
val AccentColor = Color(0xFFF2A65A)
val AccentInkColor = Color(0xFF241708)

private val SmartDisplayColorScheme = darkColorScheme(
    background = BgColor,
    surface = PanelColor,
    surfaceVariant = PanelAltColor,
    onBackground = TextColor,
    onSurface = TextColor,
    primary = AccentColor,
    onPrimary = AccentInkColor,
    outline = LineColor
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    // isSystemInDarkTheme() は使わず、常にこの固定配色を適用する。
    MaterialTheme(
        colorScheme = SmartDisplayColorScheme,
        content = content
    )
}
