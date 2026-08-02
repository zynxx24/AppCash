package com.appcash.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.appcash.ui.theme.OrangeDark
import com.appcash.ui.theme.OrangeLight
import com.appcash.ui.theme.OrangePrimary

data class ScheduleItem(
    val day: String,
    val period: String,
    val subject: String,
    val teacher: String,
    val room: String,
    val isRplFocus: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen() {
    val fullSchedule = listOf(
        // Senin
        ScheduleItem("Senin", "1–2",  "Praktik Dasar Rekayasa Perangkat Lunak",         "Pak Surya",                         "Ruang Y.1B",       true),
        ScheduleItem("Senin", "1–2",  "Praktik Dasar Kuliner",                           "Ms. Dayu N",                        "Ruang Y.1B"),
        ScheduleItem("Senin", "5–6",  "Rekayasa Perangkat Lunak",                        "Pak Rizky",                         "Ruang Y.1B",       true),
        ScheduleItem("Senin", "7–10", "Bahasa Inggris",                                  "Ms. Diah",                          "Ruang Y.1B"),
        // Selasa
        ScheduleItem("Selasa","1–10", "Rekayasa Perangkat Lunak (Sepanjang Hari)",       "Pak Rizky",                         "Meeting Room + FO",true),
        // Rabu
        ScheduleItem("Rabu",  "1–8",  "Rekayasa Perangkat Lunak",                        "Pak Rizky",                         "Meeting Room + FO",true),
        ScheduleItem("Rabu",  "9–10", "Pendidikan Agama & Budi Pekerti",                 "Bu Azmi / Bu Happy / Bu Sani",      "Meeting Room + FO"),
        // Kamis
        ScheduleItem("Kamis", "1–4",  "Matematika",                                      "Pak Restu",                         "Ruang Y.1B"),
        ScheduleItem("Kamis", "5–6",  "Rekayasa Perangkat Lunak",                        "Pak Rizky",                         "Ruang Y.1B",       true),
        ScheduleItem("Kamis", "7–10", "Kreativitas, Inovasi, dan Kewirausahaan",         "Bu Lulu",                           "Ruang Y.1B"),
        // Jumat
        ScheduleItem("Jumat", "1–2",  "Bahasa Bali",                                     "Bu Sinta",                          "Ruang Y.1B"),
        ScheduleItem("Jumat", "3–4",  "Pendidikan Pancasila",                            "Bu Happy",                          "Ruang Y.1B"),
        ScheduleItem("Jumat", "5–8",  "Bahasa Indonesia",                                "Mr. Esa",                           "Ruang Y.1B"),
        ScheduleItem("Jumat", "9–10", "Pendidikan Agama & Budi Pekerti",                 "Bu Chika",                          "Ruang Y.1B")
    )

    val days = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat")
    var currentDayIndex by remember { mutableStateOf(0) }
    val currentDay = days[currentDayIndex]
    val daySchedule = fullSchedule.filter { it.day == currentDay }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Page Header
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            Text("Dashboard", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Mata Pelajaran XII PPLG", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        }

        // Info Banner (orange gradient)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(OrangePrimary, OrangeLight)
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Informasi Tambahan", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
                    Text("Peralihan Ruangan Belajar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Selasa & Rabu → Meeting Room + FO", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
                }
                Icon(Icons.Default.Place, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(32.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Day Pager Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .weight(1f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Day title
                Text(
                    currentDay,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OrangePrimary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                LazyColumn(
                    modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(daySchedule) { item ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (item.isRplFocus)
                                        Brush.linearGradient(listOf(OrangePrimary, OrangeLight))
                                    else
                                        Brush.linearGradient(listOf(OrangeDark, OrangePrimary))
                                )
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(
                                        "${item.day} Jam ke-${item.period}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                    Text(
                                        item.room,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                }
                                Text(item.subject, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Guru Pengampu : ${item.teacher}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                    if (item.isRplFocus) {
                                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }

                // Back / Next buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { if (currentDayIndex > 0) currentDayIndex-- },
                        enabled = currentDayIndex > 0,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangePrimary),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, OrangePrimary)
                    ) { Text("Back", fontWeight = FontWeight.Bold) }

                    Button(
                        onClick = { if (currentDayIndex < days.size - 1) currentDayIndex++ },
                        enabled = currentDayIndex < days.size - 1,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                    ) { Text("Next", fontWeight = FontWeight.Bold, color = Color.White) }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
