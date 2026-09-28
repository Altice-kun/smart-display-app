package com.smartdisplay.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartdisplay.app.clock.DotMatrixClock
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(modifier = Modifier.weight(1.15f)) {
                DotMatrixClock(modifier = Modifier.fillMaxWidth().height(64.dp))
                Spacer(Modifier.height(6.dp))
                DateLabel()
            }
            WeatherCard(modifier = Modifier.weight(1f))
        }

        NewsCard()

        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            AgendaCard(modifier = Modifier.weight(1.15f))
            QuickLaunchColumn(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun DateLabel() {
    var today by remember { mutableStateOf(LocalDate.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            today = LocalDate.now()
            delay(30_000)
        }
    }
    val formatter = remember { DateTimeFormatter.ofPattern("M月d日(E)", Locale.JAPANESE) }
    Text(text = today.format(formatter), color = TextDimColor, fontSize = 15.sp)
}

@Composable
private fun WeatherCard(modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = PanelColor)) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("天気・静岡", color = TextDimColor, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Icon(Icons.Filled.WbSunny, contentDescription = null, tint = AccentColor, modifier = Modifier.size(32.dp))
                Spacer(Modifier.width(14.dp))
                Column {
                    Text("24°", color = TextColor, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                    Text("晴れ時々くもり・最高27°/最低18°", color = TextDimColor, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun NewsCard() {
    val headlines = listOf(
        "大型連休、行楽地に人出戻る" to "NHK",
        "新型EV充電規格が国内統一へ" to "日経",
        "週末は広く晴れ、行楽日和に" to "気象協会"
    )
    Card(colors = CardDefaults.cardColors(containerColor = PanelColor)) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("ニュース", color = TextDimColor, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            headlines.forEach { (title, source) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(title, color = TextColor, fontSize = 13.sp, modifier = Modifier.weight(1f))
                    Text(source, color = TextDimColor, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun AgendaCard(modifier: Modifier = Modifier) {
    val items = listOf("10:00" to "チームMTG", "13:30" to "歯医者", "19:00" to "買い物")
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = PanelColor)) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("今日の予定", color = TextDimColor, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(10.dp))
            items.forEach { (time, label) ->
                Row(modifier = Modifier.padding(vertical = 5.dp)) {
                    Text(time, color = AccentColor, fontSize = 14.sp, modifier = Modifier.size(48.dp, 20.dp))
                    Text(label, color = TextColor, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun QuickLaunchColumn(modifier: Modifier = Modifier) {
    val items = listOf(
        "カメラ" to Icons.Filled.CameraAlt,
        "フォト" to Icons.Filled.Photo,
        "タイマー" to Icons.Filled.Timer,
        "設定" to Icons.Filled.Settings
    )
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items.forEach { (label, icon) ->
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = PanelAltColor)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 14.dp).fillMaxWidth(),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                ) {
                    Icon(icon, contentDescription = label, tint = TextColor, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.height(6.dp))
                    Text(label, color = TextDimColor, fontSize = 11.sp)
                }
            }
        }
    }
}
