package com.smartdisplay.app.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private val photoPalettes = listOf(
    listOf(Color(0xFF3B4A73), Color(0xFF1C2338)),
    listOf(Color(0xFF6B4226), Color(0xFF2A1A12)),
    listOf(Color(0xFF2F6B4F), Color(0xFF132A20)),
    listOf(Color(0xFF6B2F57), Color(0xFF2A1322)),
    listOf(Color(0xFF4A5568), Color(0xFF1A202C))
)

@Composable
fun PhotosScreen(modifier: Modifier = Modifier) {
    var index by remember { mutableIntStateOf(0) }
    var playing by remember { mutableStateOf(false) }
    var zoomed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (zoomed) 1.15f else 1f, label = "photo-zoom")

    LaunchedEffect(playing, index) {
        if (playing) {
            delay(2200)
            index = (index + 1) % photoPalettes.size
        }
    }

    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(280.dp)
                .clip(RoundedCornerShape(22.dp))
                .scale(scale)
                .background(Brush.linearGradient(photoPalettes[index]))
                .pointerInput(Unit) {
                    detectTapGestures(onDoubleTap = { zoomed = !zoomed })
                }
        )
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            photoPalettes.indices.forEach { i ->
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(if (i == index) AccentColor else LineColor)
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            IconButton(onClick = { index = (index - 1 + photoPalettes.size) % photoPalettes.size }) {
                Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "前へ", tint = TextColor)
            }
            IconButton(onClick = { playing = !playing }) {
                Icon(
                    if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = "スライドショー再生/停止",
                    tint = TextColor
                )
            }
            IconButton(onClick = { index = (index + 1) % photoPalettes.size }) {
                Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "次へ", tint = TextColor)
            }
        }
        Spacer(Modifier.height(8.dp))
        Text("写真をダブルタップでズーム", color = TextDimColor, fontSize = 12.sp)
    }
}
