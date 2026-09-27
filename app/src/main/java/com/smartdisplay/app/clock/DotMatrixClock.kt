package com.smartdisplay.app.clock

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.time.LocalTime

/**
 * ドットマトリクス（LED風）表示の時計。
 * 3x5ドットのビットマップフォントで HH:MM:SS を描画する。
 * サイズはmodifier側で指定すること（例: Modifier.size(width = 260.dp, height = 60.dp)）。
 */

private val DOT_FONT: Map<Char, List<String>> = mapOf(
    '0' to listOf("111", "101", "101", "101", "111"),
    '1' to listOf("010", "110", "010", "010", "111"),
    '2' to listOf("111", "001", "111", "100", "111"),
    '3' to listOf("111", "001", "111", "001", "111"),
    '4' to listOf("101", "101", "111", "001", "001"),
    '5' to listOf("111", "100", "111", "001", "111"),
    '6' to listOf("111", "100", "111", "101", "111"),
    '7' to listOf("111", "001", "001", "001", "001"),
    '8' to listOf("111", "101", "111", "101", "111"),
    '9' to listOf("111", "101", "111", "001", "111"),
    ':' to listOf("0", "1", "0", "1", "0")
)

@Composable
fun DotMatrixClock(
    modifier: Modifier = Modifier,
    showSeconds: Boolean = true,
    onColor: Color = Color(0xFFF2A65A),
    offColor: Color = Color(0xFFF2A65A).copy(alpha = 0.10f),
    dotRadius: Dp = 4.dp,
    dotGap: Dp = 11.dp,
    charGap: Dp = 9.dp
) {
    var now by remember { mutableStateOf(LocalTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = LocalTime.now()
            delay(1000)
        }
    }
    val text = if (showSeconds) {
        "%02d:%02d:%02d".format(now.hour, now.minute, now.second)
    } else {
        "%02d:%02d".format(now.hour, now.minute)
    }

    Canvas(modifier = modifier) {
        val dotGapPx = dotGap.toPx()
        val dotRadiusPx = dotRadius.toPx()
        val charGapPx = charGap.toPx()
        var xCursor = dotRadiusPx

        for (ch in text) {
            val pattern = DOT_FONT[ch] ?: continue
            val width = pattern[0].length
            drawDotPattern(pattern, Offset(xCursor, dotRadiusPx), dotRadiusPx, dotGapPx, onColor, offColor)
            xCursor += (width - 1) * dotGapPx + dotGapPx + charGapPx
        }
    }
}

private fun DrawScope.drawDotPattern(
    pattern: List<String>,
    origin: Offset,
    dotRadius: Float,
    dotGap: Float,
    onColor: Color,
    offColor: Color
) {
    for (row in pattern.indices) {
        val line = pattern[row]
        for (col in line.indices) {
            val isOn = line[col] == '1'
            drawCircle(
                color = if (isOn) onColor else offColor,
                radius = dotRadius,
                center = Offset(origin.x + col * dotGap, origin.y + row * dotGap)
            )
        }
    }
}
