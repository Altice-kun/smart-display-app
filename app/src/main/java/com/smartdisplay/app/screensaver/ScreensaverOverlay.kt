package com.smartdisplay.app.screensaver

import android.content.Context
import android.media.MediaPlayer
import androidx.annotation.RawRes
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.delay

/** ホームタブが一定時間放置されたかどうかを検知するタイマー。 */
class IdleController(private val timeoutMillis: Long = 60_000L) {
    var isIdle by mutableStateOf(false)
        private set
    private var lastInteraction = System.currentTimeMillis()

    /** タップなど、何らかの操作があるたびに呼び出す。 */
    fun notifyInteraction() {
        lastInteraction = System.currentTimeMillis()
        isIdle = false
    }

    /** Activity起動時に一度だけ coroutine scope 上で呼び出す監視ループ。 */
    suspend fun watch() {
        while (true) {
            delay(1000)
            if (!isIdle && System.currentTimeMillis() - lastInteraction >= timeoutMillis) {
                isIdle = true
            }
        }
    }
}

/** res/raw に置いた環境音をループ再生するだけの簡易プレイヤー。 */
class AmbientSoundPlayer(private val context: Context) {
    private var player: MediaPlayer? = null

    fun play(@RawRes resId: Int, volume: Float = 0.5f) {
        stop()
        player = MediaPlayer.create(context, resId)?.apply {
            isLooping = true
            setVolume(volume, volume)
            start()
        }
    }

    fun stop() {
        player?.release()
        player = null
    }
}

/**
 * スクリーンセーバー代わりのフルスクリーン演出。
 * タブバーやコンテンツを隠した状態で、この上に重ねて表示する想定。
 * タップすると onDismiss が呼ばれる。
 */
@Composable
fun ScreensaverOverlay(onDismiss: () -> Unit) {
    val infinite = rememberInfiniteTransition(label = "screensaver-bg")
    val shift by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Reverse),
        label = "shift"
    )
    val palette = listOf(
        Color(0xFF1C2338), Color(0xFF3B2A55), Color(0xFF2F6B4F), Color(0xFF6B2F57)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) { detectTapGestures { onDismiss() } }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val pos = shift * (palette.size - 1)
            val i = pos.toInt().coerceIn(0, palette.size - 2)
            val blended = lerp(palette[i], palette[i + 1], pos - i)
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(blended, Color.Black),
                    center = Offset(size.width * shift, size.height * (1 - shift)),
                    radius = size.maxDimension
                )
            )
        }
    }
}
