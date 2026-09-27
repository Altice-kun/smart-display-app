package com.smartdisplay.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 見た目としては完成しているが、まだ実際のSpotify連携（Web API / App Remote SDK）は
 * 接続していないUIシェル。再生状態はこの画面内のローカルなダミー状態。
 * Spotify連携フェーズで、ここのplaying/trackを実際のSpotifyAppRemoteの状態に差し替える。
 */
@Composable
fun MusicScreen(modifier: Modifier = Modifier) {
    var playing by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0.18f) }
    var volume by remember { mutableFloatStateOf(0.6f) }

    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(Brush.linearGradient(listOf(PanelAltColor, BgColor))),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.MusicNote, contentDescription = null, tint = TextDimColor, modifier = Modifier.size(52.dp))
        }
        Spacer(Modifier.height(20.dp))
        Text("夜明けのメロディ", color = TextColor, fontSize = 19.sp)
        Text("Aurora Sound", color = TextDimColor, fontSize = 14.sp)
        Spacer(Modifier.height(16.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.width(280.dp),
            color = AccentColor,
            trackColor = PanelAltColor
        )
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { /* 前の曲へ：Spotify連携フェーズで実装 */ }) {
                Icon(Icons.Filled.SkipPrevious, contentDescription = "前へ", tint = TextColor, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.width(12.dp))
            IconButton(
                onClick = { playing = !playing },
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(50))
                    .background(AccentColor)
            ) {
                Icon(
                    if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = "再生/一時停止",
                    tint = AccentInkColor,
                    modifier = Modifier.size(30.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            IconButton(onClick = { /* 次の曲へ：Spotify連携フェーズで実装 */ }) {
                Icon(Icons.Filled.SkipNext, contentDescription = "次へ", tint = TextColor, modifier = Modifier.size(28.dp))
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.width(280.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.VolumeUp, contentDescription = null, tint = TextDimColor, modifier = Modifier.size(18.dp))
            Slider(
                value = volume,
                onValueChange = { volume = it },
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
            )
            Text("${(volume * 100).toInt()}%", color = TextDimColor, fontSize = 12.sp)
        }
    }
}
