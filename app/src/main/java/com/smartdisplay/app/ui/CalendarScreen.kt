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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class CalEvent(val time: String, val label: String)
private data class CalDay(val title: String, val events: List<CalEvent>)

@Composable
fun CalendarScreen(modifier: Modifier = Modifier) {
    val days = listOf(
        CalDay("今日・9月23日(水)", listOf(
            CalEvent("10:00", "チームMTG"),
            CalEvent("13:30", "歯医者"),
            CalEvent("19:00", "買い物")
        )),
        CalDay("明日・9月24日(木)", listOf(
            CalEvent("09:00", "ゴミ出し"),
            CalEvent("15:00", "オンライン打合せ")
        )),
        CalDay("9月25日(金)", listOf(CalEvent("終日", "予定なし")))
    )

    Column(
        modifier = modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        days.forEach { day ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PanelColor)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(day.title, color = TextDimColor, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(10.dp))
                    day.events.forEach { e ->
                        Row(modifier = Modifier.padding(vertical = 5.dp)) {
                            Text(e.time, color = AccentColor, fontSize = 14.sp, modifier = Modifier.size(48.dp, 20.dp))
                            Text(e.label, color = TextColor, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}
