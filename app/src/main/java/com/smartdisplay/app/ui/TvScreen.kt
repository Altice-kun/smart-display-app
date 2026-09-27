package com.smartdisplay.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartdisplay.app.tv.GoogleTvRemoteClient
import com.smartdisplay.app.tv.proto.RemoteKeyCode

@Composable
fun TvScreen(tvClient: GoogleTvRemoteClient?, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (tvClient == null) {
            Text(
                "Google TVとペアリングされていません",
                color = TextDimColor,
                fontSize = 13.sp
            )
            Text(
                "README記載のペアリング処理を実装すると、この画面から実際に操作できます",
                color = TextDimColor,
                fontSize = 12.sp
            )
            Spacer(Modifier.height(20.dp))
        } else {
            Text("Google TV 接続中・プロジェクター", color = TextDimColor, fontSize = 13.sp)
            Spacer(Modifier.height(20.dp))
        }

        DPad(onKey = { tvClient?.sendKey(it) })

        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            RemoteButton("戻る", Icons.Filled.ArrowBack) { tvClient?.sendKey(RemoteKeyCode.KEYCODE_BACK) }
            RemoteButton("ホーム", Icons.Filled.Home) { tvClient?.sendKey(RemoteKeyCode.KEYCODE_HOME) }
            RemoteButton(null, Icons.Filled.VolumeOff) { tvClient?.sendKey(RemoteKeyCode.KEYCODE_VOLUME_MUTE) }
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            RemoteButton(null, Icons.Filled.VolumeDown) { tvClient?.sendKey(RemoteKeyCode.KEYCODE_VOLUME_DOWN) }
            RemoteButton(
                null,
                Icons.Filled.PowerSettingsNew,
                danger = true
            ) { tvClient?.sendKey(RemoteKeyCode.KEYCODE_POWER) }
            RemoteButton(null, Icons.Filled.VolumeUp) { tvClient?.sendKey(RemoteKeyCode.KEYCODE_VOLUME_UP) }
        }
    }
}

@Composable
private fun DPad(onKey: (RemoteKeyCode) -> Unit) {
    val cell = 58.dp
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        DPadButton(Icons.Filled.KeyboardArrowUp, cell) { onKey(RemoteKeyCode.KEYCODE_DPAD_UP) }
        Row {
            DPadButton(Icons.Filled.KeyboardArrowLeft, cell) { onKey(RemoteKeyCode.KEYCODE_DPAD_LEFT) }
            Spacer(Modifier.size(cell))
            DPadButton(Icons.Filled.KeyboardArrowRight, cell) { onKey(RemoteKeyCode.KEYCODE_DPAD_RIGHT) }
        }
        DPadButton(Icons.Filled.KeyboardArrowDown, cell) { onKey(RemoteKeyCode.KEYCODE_DPAD_DOWN) }
    }
}

@Composable
private fun DPadButton(icon: androidx.compose.ui.graphics.vector.ImageVector, size: androidx.compose.ui.unit.Dp, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(size)
    ) {
        Icon(icon, contentDescription = null, tint = TextColor)
    }
}

@Composable
private fun RemoteButton(
    label: String?,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    danger: Boolean = false,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (danger) androidx.compose.ui.graphics.Color(0xFF3A1E21) else PanelAltColor,
            contentColor = if (danger) androidx.compose.ui.graphics.Color(0xFFFF9A9A) else TextColor
        )
    ) {
        Icon(icon, contentDescription = label, modifier = Modifier.size(18.dp))
        if (label != null) {
            Spacer(Modifier.size(6.dp))
            Text(label, fontSize = 14.sp)
        }
    }
}
