// ==========================================================================
// PETA FILE · AppCash (Kelompok 6) · ScheduleScreen.kt
// Isi file : Jadwal mata pelajaran XII PPLG.
// Penanda  : CARI-JADWAL, CARI-TIDAK-DIPAKAI
// Cara cari: VS Code Ctrl+Shift+F (Android Studio Ctrl+Shift+F) lalu ketik
//            "CARI-" untuk semua penanda, atau salah satu nama di baris Penanda.
// ==========================================================================
//  CATATAN: layar jadwal pelajaran ini BELUM DIPASANG di bar bawah (tidak dipanggil dari MainContent). Datanya tetap, bukan data kas.
//  Kalau ditanya "jadwal ada di mana?", jawab: ScheduleScreen.kt sudah dibuat, tinggal ditambahkan sebagai tab bila diperlukan.
// » Alamat folder (package) file ini di dalam proyek.
package com.appcash.ui.screens

// » orangeCard = dekorasi kartu oranye bergradien (Modifiers.kt).
import com.appcash.ui.theme.orangeCard
// » background = mengisi warna/gradien latar sebuah tampilan.
import androidx.compose.foundation.background
// » Layout dasar: Column (susun atas-bawah), Row (susun kiri-kanan), Box (tumpuk), Spacer (jarak kosong),
// » padding, size, fillMaxWidth, dll.
import androidx.compose.foundation.layout.*
// » LazyColumn = daftar vertikal yang hanya menggambar baris yang terlihat (hemat memori).
import androidx.compose.foundation.lazy.LazyColumn
// » LazyRow = daftar mendatar (geser kiri-kanan) yang hemat memori.
import androidx.compose.foundation.lazy.LazyRow
// » items = mengulang satu tampilan untuk setiap elemen daftar.
import androidx.compose.foundation.lazy.items
// » itemsIndexed = seperti items tetapi sekaligus membawa nomor urut.
import androidx.compose.foundation.lazy.itemsIndexed
// » RoundedCornerShape = bentuk persegi dengan sudut membulat.
import androidx.compose.foundation.shape.RoundedCornerShape
// » Icons = daftar ikon bawaan Material.
import androidx.compose.material.icons.Icons
// » Ikon bawaan Material: DateRange (versi terisi).
import androidx.compose.material.icons.filled.DateRange
// » Ikon bawaan Material: Info (versi terisi).
import androidx.compose.material.icons.filled.Info
// » Ikon bawaan Material: Place (versi terisi).
import androidx.compose.material.icons.filled.Place
// » Ikon bawaan Material: Star (versi terisi).
import androidx.compose.material.icons.filled.Star
// » Komponen Material 3 (gaya resmi Android): Text, Button, Card, TextField, Scaffold, dll.
import androidx.compose.material3.*
// » Alat state Compose: remember (mengingat nilai), mutableStateOf (nilai yang bila berubah menggambar ulang
// » layar), LaunchedEffect, dll.
import androidx.compose.runtime.*
// » Alignment = pengatur posisi isi (tengah, atas, bawah, kiri, kanan).
import androidx.compose.ui.Alignment
// » Modifier = daftar 'dekorasi' sebuah tampilan: ukuran, jarak, warna latar, klik, dll.
import androidx.compose.ui.Modifier
// » clip = memotong tampilan mengikuti sebuah bentuk (mis. sudut bulat atau lingkaran).
import androidx.compose.ui.draw.clip
// » Brush = 'cat' warna, termasuk gradien (warna berubah halus).
import androidx.compose.ui.graphics.Brush
// » Color = tipe warna Compose; dipakai untuk menulis warna seperti Color.White atau Color(0xFF...).
import androidx.compose.ui.graphics.Color
// » Offset = titik koordinat (x, y) di Canvas.
import androidx.compose.ui.geometry.Offset
// » FontWeight = ketebalan huruf (Normal, Medium, Bold, ExtraBold).
import androidx.compose.ui.text.font.FontWeight
// » dp = satuan ukuran Android yang sama di semua layar (mis. 16.dp).
import androidx.compose.ui.unit.dp
// » OrangeDark = oranye gelap.
import com.appcash.ui.theme.OrangeDark
// » OrangeLight = oranye terang.
import com.appcash.ui.theme.OrangeLight
// » OrangePrimary = warna oranye utama.
import com.appcash.ui.theme.OrangePrimary

// » ScheduleItem = cetakan data satu baris jadwal pelajaran. Layar ini BELUM dipasang di bar bawah.
data class ScheduleItem(
    // » Hari (Senin, Selasa, ...).
    val day: String,
    // » Jam pelajaran ke berapa, contoh "1–2".
    val period: String,
    // » Nama mata pelajaran.
    val subject: String,
    // » Nama guru pengampu.
    val teacher: String,
    // » Ruangan belajar.
    val room: String,
    // » Penanda true kalau pelajaran jurusan RPL/PPLG (diberi bintang emas dan warna lebih terang). Awalnya
    // » false.
    val isRplFocus: Boolean = false
)

// CARI-JADWAL CARI-TIDAK-DIPAKAI | Belum dipasang di bottom bar; tambahkan di MainContent bila ingin ditampilkan.
// » Mengizinkan fitur Material 3 yang masih eksperimental.
@OptIn(ExperimentalMaterial3Api::class)
// » Penanda fungsi penggambar tampilan.
@Composable
// » ScheduleScreen = layar jadwal pelajaran XII PPLG.
fun ScheduleScreen() {
    // » fullSchedule = daftar lengkap jadwal seminggu. Urutan ScheduleItem: hari, jam, mata pelajaran, guru,
    // » ruangan, penanda PPLG.
    val fullSchedule = listOf(
        // Senin
        // » Senin jam 1–2: Praktik Dasar RPL, Pak Surya, Ruang Y.1B (penanda PPLG = true).
        ScheduleItem("Senin", "1–2",  "Praktik Dasar Rekayasa Perangkat Lunak",         "Pak Surya",                         "Ruang Y.1B",       true),
        // » Senin jam 1–2: Praktik Dasar Kuliner.
        ScheduleItem("Senin", "1–2",  "Praktik Dasar Kuliner",                           "Ms. Dayu N",                        "Ruang Y.1B"),
        // » Senin jam 5–6: Rekayasa Perangkat Lunak, Pak Rizky.
        ScheduleItem("Senin", "5–6",  "Rekayasa Perangkat Lunak",                        "Pak Rizky",                         "Ruang Y.1B",       true),
        // » Senin jam 7–10: Bahasa Inggris.
        ScheduleItem("Senin", "7–10", "Bahasa Inggris",                                  "Ms. Diah",                          "Ruang Y.1B"),
        // Selasa
        // » Selasa jam 1–10: Rekayasa Perangkat Lunak sepanjang hari di Meeting Room + FO.
        ScheduleItem("Selasa","1–10", "Rekayasa Perangkat Lunak (Sepanjang Hari)",       "Pak Rizky",                         "Meeting Room + FO",true),
        // Rabu
        // » Rabu jam 1–8: Rekayasa Perangkat Lunak.
        ScheduleItem("Rabu",  "1–8",  "Rekayasa Perangkat Lunak",                        "Pak Rizky",                         "Meeting Room + FO",true),
        // » Rabu jam 9–10: Pendidikan Agama & Budi Pekerti.
        ScheduleItem("Rabu",  "9–10", "Pendidikan Agama & Budi Pekerti",                 "Bu Azmi / Bu Happy / Bu Sani",      "Meeting Room + FO"),
        // Kamis
        // » Kamis jam 1–4: Matematika, Pak Restu.
        ScheduleItem("Kamis", "1–4",  "Matematika",                                      "Pak Restu",                         "Ruang Y.1B"),
        // » Kamis jam 5–6: Rekayasa Perangkat Lunak.
        ScheduleItem("Kamis", "5–6",  "Rekayasa Perangkat Lunak",                        "Pak Rizky",                         "Ruang Y.1B",       true),
        // » Kamis jam 7–10: Kreativitas, Inovasi, dan Kewirausahaan.
        ScheduleItem("Kamis", "7–10", "Kreativitas, Inovasi, dan Kewirausahaan",         "Bu Lulu",                           "Ruang Y.1B"),
        // Jumat
        // » Jumat jam 1–2: Bahasa Bali, Bu Sinta.
        ScheduleItem("Jumat", "1–2",  "Bahasa Bali",                                     "Bu Sinta",                          "Ruang Y.1B"),
        // » Jumat jam 3–4: Pendidikan Pancasila.
        ScheduleItem("Jumat", "3–4",  "Pendidikan Pancasila",                            "Bu Happy",                          "Ruang Y.1B"),
        // » Jumat jam 5–8: Bahasa Indonesia.
        ScheduleItem("Jumat", "5–8",  "Bahasa Indonesia",                                "Mr. Esa",                           "Ruang Y.1B"),
        // » Jumat jam 9–10: Pendidikan Agama & Budi Pekerti.
        ScheduleItem("Jumat", "9–10", "Pendidikan Agama & Budi Pekerti",                 "Bu Chika",                          "Ruang Y.1B")
    )

    // » days = daftar lima hari sekolah yang dipakai untuk berpindah hari.
    val days = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat")
    // Mulai dari hari ini (Sabtu/Minggu -> Senin)
    // » currentDayIndex = nomor hari yang sedang ditampilkan (0 = Senin ... 4 = Jumat). "by remember" = diingat
    // » dan layar digambar ulang saat berubah.
    var currentDayIndex by remember {
        // » Nilai awal: nomor hari ini dari tanggal HP (dayOfWeek.value - 1). Kalau Sabtu/Minggu (di luar 0–4),
        // » pakai 0 = Senin.
        mutableStateOf((java.time.LocalDate.now().dayOfWeek.value - 1).takeIf { it in 0..4 } ?: 0)
    }
    // » currentDay = nama hari yang sedang dipilih.
    val currentDay = days[currentDayIndex]
    // » daySchedule = jadwal hari itu saja (filter hari yang sama dengan currentDay).
    val daySchedule = fullSchedule.filter { it.day == currentDay }

    // » Column = susunan atas-bawah seluruh layar jadwal.
    Column(
        // » Modifier:
        modifier = Modifier
            // » penuhi layar,
            .fillMaxSize()
            // » warna latar mengikuti tema.
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Page Header
        // » Bagian judul halaman.
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            // » Teks kecil "Dashboard" sebagai penunjuk asal halaman.
            Text("Dashboard", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            // » Judul besar "Mata Pelajaran XII PPLG".
            Text("Mata Pelajaran XII PPLG", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        }

        // Info Banner (orange gradient)
        // » Kotak informasi (banner) oranye.
        Box(
            // » Modifier:
            modifier = Modifier
                // » selebar layar,
                .fillMaxWidth()
                // » jarak kiri-kanan 16dp,
                .padding(horizontal = 16.dp)
                // » dekorasi kartu oranye bersudut 16dp,
                .orangeCard(16.dp)
                // » jarak dalam 16dp.
                .padding(16.dp)
        ) {
            // » Row = baris mendatar isi banner.
            Row(
                // » Selebar banner.
                modifier = Modifier.fillMaxWidth(),
                // » Teks di kiri, ikon di kanan.
                horizontalArrangement = Arrangement.SpaceBetween,
                // » Rata tengah secara vertikal.
                verticalAlignment = Alignment.CenterVertically
            ) {
                // » Kolom teks banner.
                Column {
                    // » Teks kecil "Informasi Tambahan".
                    Text("Informasi Tambahan", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
                    // » Teks tebal "Peralihan Ruangan Belajar".
                    Text("Peralihan Ruangan Belajar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    // » Teks penjelas: Selasa & Rabu belajar di Meeting Room + FO.
                    Text("Selasa & Rabu → Meeting Room + FO", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
                }
                // » Ikon tempat (pin) di kanan banner.
                Icon(Icons.Default.Place, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(32.dp))
            }
        }

        // » Spacer = jarak kosong 16dp.
        Spacer(modifier = Modifier.height(16.dp))

        // Day Pager Card
        // » Card = kartu putih besar berisi jadwal satu hari.
        Card(
            // » Modifier:
            modifier = Modifier
                // » selebar layar,
                .fillMaxWidth()
                // » jarak kiri-kanan 16dp,
                .padding(horizontal = 16.dp)
                // » weight(1f) = mengambil sisa tinggi layar.
                .weight(1f),
            // » Sudut bulat 20dp.
            shape = RoundedCornerShape(20.dp),
            // » Warna kartu putih.
            colors = CardDefaults.cardColors(containerColor = Color.White),
            // » Bayangan tipis 2dp.
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            // » Kolom isi kartu penuh.
            Column(modifier = Modifier.fillMaxSize()) {
                // Day title
                // » Teks nama hari (Senin, ...).
                Text(
                    // » Isinya currentDay.
                    currentDay,
                    // » Modifier:
                    modifier = Modifier
                        // » selebar kartu,
                        .fillMaxWidth()
                        // » jarak atas-bawah 16dp.
                        .padding(vertical = 16.dp),
                    // » Gaya sub-judul.
                    style = MaterialTheme.typography.titleMedium,
                    // » Tebal.
                    fontWeight = FontWeight.Bold,
                    // » Warna oranye.
                    color = OrangePrimary,
                    // » Rata tengah.
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                // » LazyColumn = daftar pelajaran yang bisa digulir.
                LazyColumn(
                    // » Mengisi sisa tinggi kartu.
                    modifier = Modifier.weight(1f),
                    // » Jarak kiri-kanan isi 12dp.
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    // » Jarak antar baris 8dp.
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // » Untuk setiap pelajaran hari itu (item)...
                    items(daySchedule) { item ->
                        // » ...buat satu kotak.
                        Box(
                            // » Modifier:
                            modifier = Modifier
                                // » selebar kartu,
                                .fillMaxWidth()
                                // » sudut bulat 12dp,
                                .clip(RoundedCornerShape(12.dp))
                                // » latar gradien:
                                .background(
                                    // » kalau pelajaran PPLG...
                                    if (item.isRplFocus)
                                        // » ...gradien oranye utama ke oranye terang,
                                        Brush.linearGradient(listOf(OrangePrimary, OrangeLight))
                                    // » kalau bukan...
                                    else
                                        // » ...gradien oranye gelap ke oranye utama.
                                        Brush.linearGradient(listOf(OrangeDark, OrangePrimary))
                                )
                                // » jarak dalam 12dp.
                                .padding(12.dp)
                        ) {
                            // » Kolom isi kotak, jarak antar baris 4dp.
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                // » Baris atas: jam di kiri, ruangan di kanan.
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    // » Teks jam pelajaran.
                                    Text(
                                        // » Tulisan: "<Hari> Jam ke-<jam>".
                                        "${item.day} Jam ke-${item.period}",
                                        // » Huruf kecil.
                                        style = MaterialTheme.typography.labelSmall,
                                        // » Putih agak transparan (alpha 0,85).
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                    // » Teks ruangan.
                                    Text(
                                        // » Isinya nama ruangan.
                                        item.room,
                                        // » Huruf kecil.
                                        style = MaterialTheme.typography.labelSmall,
                                        // » Putih agak transparan.
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                }
                                // » Teks nama mata pelajaran: gaya sub-judul kecil, tebal, putih.
                                Text(item.subject, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                                // » Baris bawah: guru di kiri, bintang di kanan.
                                Row(
                                    // » Selebar kotak.
                                    modifier = Modifier.fillMaxWidth(),
                                    // » Teks di kiri, ikon di kanan.
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    // » Rata tengah vertikal.
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // » Teks guru.
                                    Text(
                                        // » Tulisan "Guru Pengampu : <nama guru>".
                                        "Guru Pengampu : ${item.teacher}",
                                        // » Huruf kecil.
                                        style = MaterialTheme.typography.labelSmall,
                                        // » Putih agak transparan.
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                    // » Kalau pelajaran PPLG...
                                    if (item.isRplFocus) {
                                        // » ...tampilkan ikon bintang emas ukuran 14dp.
                                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }
                    // » Satu item penutup: jarak kosong 8dp di bawah daftar.
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }

                // Back / Next buttons
                // » Baris tombol navigasi hari.
                Row(
                    // » Modifier:
                    modifier = Modifier
                        // » selebar kartu,
                        .fillMaxWidth()
                        // » jarak 12dp.
                        .padding(12.dp),
                    // » Jarak antar tombol 8dp.
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // » OutlinedButton = tombol bergaris "Sebelumnya".
                    OutlinedButton(
                        // » Saat ditekan: kalau bukan hari pertama, mundur satu hari (currentDayIndex--).
                        onClick = { if (currentDayIndex > 0) currentDayIndex-- },
                        // » Tombol aktif hanya kalau bukan Senin.
                        enabled = currentDayIndex > 0,
                        // » Lebar tombol sama dengan tombol sebelah (weight 1).
                        modifier = Modifier.weight(1f),
                        // » Sudut bulat 12dp.
                        shape = RoundedCornerShape(12.dp),
                        // » Warna isi tombol oranye.
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangePrimary),
                        // » Garis tepi oranye kalau aktif, abu kalau nonaktif.
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, if (currentDayIndex > 0) OrangePrimary else Color(0xFFE0E0E0))
                    // » Tulisan tombol "Sebelumnya".
                    ) { Text("Sebelumnya", fontWeight = FontWeight.Bold) }

                    // » Button = tombol penuh "Berikutnya".
                    Button(
                        // » Saat ditekan: kalau bukan hari terakhir, maju satu hari (currentDayIndex++).
                        onClick = { if (currentDayIndex < days.size - 1) currentDayIndex++ },
                        // » Aktif hanya kalau bukan Jumat.
                        enabled = currentDayIndex < days.size - 1,
                        // » Lebar sama dengan tombol sebelah.
                        modifier = Modifier.weight(1f),
                        // » Sudut bulat 12dp.
                        shape = RoundedCornerShape(12.dp),
                        // » Warna latar oranye.
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                    // » Tulisan tombol "Berikutnya", putih.
                    ) { Text("Berikutnya", fontWeight = FontWeight.Bold, color = Color.White) }
                }
            }
        }

        // » Jarak kosong 16dp di bawah kartu.
        Spacer(modifier = Modifier.height(16.dp))
    }
}
