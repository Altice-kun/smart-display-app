package com.smartdisplay.app

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import com.smartdisplay.app.screensaver.AmbientSoundPlayer
import com.smartdisplay.app.screensaver.IdleController
import com.smartdisplay.app.screensaver.ScreensaverOverlay
import com.smartdisplay.app.tv.GoogleTvRemoteClient
import com.smartdisplay.app.ui.AppTheme
import com.smartdisplay.app.ui.CalendarScreen
import com.smartdisplay.app.ui.HomeScreen
import com.smartdisplay.app.ui.MusicScreen
import com.smartdisplay.app.ui.PhotosScreen
import com.smartdisplay.app.ui.ToolsScreen
import com.smartdisplay.app.ui.TvScreen
import com.smartdisplay.app.voice.VoiceCommandManager

enum class AppTab(val label: String, val icon: ImageVector) {
    HOME("ホーム", Icons.Filled.Home),
    MUSIC("音楽", Icons.Filled.MusicNote),
    TV("TV", Icons.Filled.Tv),
    CALENDAR("カレンダー", Icons.Filled.CalendarMonth),
    PHOTOS("写真", Icons.Filled.Photo),
    TOOLS("ツール", Icons.Filled.Build)
}

class MainActivity : ComponentActivity() {

    private val idleController = IdleController(timeoutMillis = 60_000L)
    private lateinit var ambientPlayer: AmbientSoundPlayer
    private lateinit var voiceManager: VoiceCommandManager
    private var currentTab by mutableStateOf(AppTab.HOME)

    // ペアリング未実装のため常にnull。README記載の方法でペアリングを実装したら、
    // 接続成功後にここへGoogleTvRemoteClientのインスタンスを設定する。
    private var tvClient: GoogleTvRemoteClient? = null

    private val requestAudioPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) voiceManager.start()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ambientPlayer = AmbientSoundPlayer(this)

        voiceManager = VoiceCommandManager(
            context = this,
            onCommand = { command ->
                when (command) {
                    "home" -> currentTab = AppTab.HOME
                    "music" -> currentTab = AppTab.MUSIC
                    "tv" -> currentTab = AppTab.TV
                    "calendar" -> currentTab = AppTab.CALENDAR
                    "photos" -> currentTab = AppTab.PHOTOS
                    "tools" -> currentTab = AppTab.TOOLS
                    "screensaver" -> idleController.notifyInteraction()
                    // play / pause / volume_up / volume_down は各画面のプレイヤーロジックに接続する
                }
            }
        )

        setContent {
            AppTheme {
                LaunchedEffect(Unit) { idleController.watch() }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            awaitEachGesture {
                                awaitFirstDown(requireUnconsumed = false)
                                idleController.notifyInteraction()
                            }
                        }
                ) {
                    Scaffold(
                        bottomBar = {
                            NavigationBar {
                                AppTab.entries.forEach { tab ->
                                    NavigationBarItem(
                                        selected = currentTab == tab,
                                        onClick = { currentTab = tab },
                                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                                        label = { Text(tab.label) }
                                    )
                                }
                            }
                        }
                    ) { padding ->
                        Box(modifier = Modifier.padding(padding)) {
                            when (currentTab) {
                                AppTab.HOME -> HomeScreen()
                                AppTab.MUSIC -> MusicScreen()
                                AppTab.TV -> TvScreen(tvClient = tvClient)
                                AppTab.CALENDAR -> CalendarScreen()
                                AppTab.PHOTOS -> PhotosScreen()
                                AppTab.TOOLS -> ToolsScreen()
                            }
                        }
                    }

                    if (idleController.isIdle && currentTab == AppTab.HOME) {
                        LaunchedEffect(Unit) {
                            runCatching { ambientPlayer.play(R.raw.ambient_loop) }
                        }
                        ScreensaverOverlay(onDismiss = {
                            idleController.notifyInteraction()
                            ambientPlayer.stop()
                        })
                    }
                }
            }
        }

        requestAudioPermission.launch(Manifest.permission.RECORD_AUDIO)
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceManager.stop()
        ambientPlayer.stop()
        tvClient?.disconnect()
    }
}
