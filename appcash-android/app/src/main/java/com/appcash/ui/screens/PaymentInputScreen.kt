// ==========================================================================
// PETA FILE · AppCash (Kelompok 6) · PaymentInputScreen.kt
// Isi file : Form input pembayaran manual (versi lama).
// Penanda  : CARI-TIDAK-DIPAKAI
// Cara cari: VS Code Ctrl+Shift+F (Android Studio Ctrl+Shift+F) lalu ketik
//            "CARI-" untuk semua penanda, atau salah satu nama di baris Penanda.
// ==========================================================================
//  CATATAN: layar input pembayaran manual (pilih siswa, nominal, tanggal, catatan) ini BELUM DIPASANG di menu.
//  Pembayaran resmi dicatat lewat tab Kas (PaymentsScreen). Riwayatnya tidak ikut menghitung saldo.
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
// » items = mengulang satu tampilan untuk setiap elemen daftar.
import androidx.compose.foundation.lazy.items
// » CircleShape = bentuk lingkaran.
import androidx.compose.foundation.shape.CircleShape
// » RoundedCornerShape = bentuk persegi dengan sudut membulat.
import androidx.compose.foundation.shape.RoundedCornerShape
// » Icons = daftar ikon bawaan Material.
import androidx.compose.material.icons.Icons
// » Ikon bawaan Material: Check (versi terisi).
import androidx.compose.material.icons.filled.Check
// » Ikon bawaan Material: Edit (versi terisi).
import androidx.compose.material.icons.filled.Edit
// » Ikon bawaan Material: Person (versi terisi).
import androidx.compose.material.icons.filled.Person
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
// » AnnotatedString = teks dengan gaya berbeda-beda di tiap bagian.
import androidx.compose.ui.text.AnnotatedString
// » FontWeight = ketebalan huruf (Normal, Medium, Bold, ExtraBold).
import androidx.compose.ui.text.font.FontWeight
// » TextOverflow = cara menangani teks yang terlalu panjang (Ellipsis = dipotong dengan '...').
import androidx.compose.ui.text.style.TextOverflow
// » OffsetMapping = pemetaan posisi kursor antara teks asli dan teks yang ditampilkan.
import androidx.compose.ui.text.input.OffsetMapping
// » TransformedText = hasil perubahan tampilan teks isian.
import androidx.compose.ui.text.input.TransformedText
// » VisualTransformation = cara menampilkan teks isian (mis. diberi titik ribuan).
import androidx.compose.ui.text.input.VisualTransformation
// » dp = satuan ukuran Android yang sama di semua layar (mis. 16.dp).
import androidx.compose.ui.unit.dp
// » Member = bentuk data satu siswa.
import com.appcash.data.model.Member
// » PaymentRecord = catatan satu pembayaran.
import com.appcash.data.model.PaymentRecord
// » AppCashRepository = pusat data & rumus aplikasi (saldo, denda, pembayaran).
import com.appcash.data.repository.AppCashRepository
// » OrangeLight = oranye terang.
import com.appcash.ui.theme.OrangeLight
// » OrangePrimary = warna oranye utama.
import com.appcash.ui.theme.OrangePrimary
// » launch = menjalankan tugas di jalur belakang (coroutine) supaya layar tidak membeku.
import kotlinx.coroutines.launch

// » KDoc (komentar dokumentasi) kelas di bawah: pengubah tampilan tanggal.
/**
 * VisualTransformation for date input: user types digits only,
 * displayed as YYYY-MM-DD with auto-inserted dashes.
 */
// » DateVisualTransformation = kelas yang mengubah cara TAMPILAN teks isian tanggal: pengguna mengetik angka
// » saja (20260904), layar menampilkan 2026-09-04. Teks aslinya tetap angka.
class DateVisualTransformation : VisualTransformation {
    // Menampilkan tanggal YYYYMMDD sebagai YYYY-MM-DD saat diketik.
    // » filter = fungsi yang dipanggil Compose untuk membuat tampilan teks dari teks asli.
    override fun filter(text: AnnotatedString): TransformedText {
        // » digits = teks asli (hanya angka).
        val digits = text.text
        // » out = penampung teks hasil tampilan (StringBuilder = penyambung teks).
        val out = StringBuilder()
        // » Untuk setiap posisi (i) karakter...
        digits.indices.forEach { i ->
            // » ...ambil karakter ke-i.
            val c = digits[i]
            // » ...tambahkan ke hasil.
            out.append(c)
            // » Setelah karakter ke-4 dan ke-6 (indeks 3 dan 5), tambahkan tanda "-" (asal belum karakter terakhir).
            // » Hasil: YYYY-MM-DD.
            if ((i == 3 || i == 5) && i < digits.length - 1) out.append('-')
        }
        // » offsetMapping = pemetaan posisi kursor antara teks asli dan teks tampilan (karena tampilan lebih panjang
        // » akibat tanda "-").
        val offsetMapping = object : OffsetMapping {
            // Menggeser posisi kursor karena ada tanda strip tambahan.
            // » originalToTransformed = posisi kursor di teks asli -> posisi di teks tampilan.
            override fun originalToTransformed(offset: Int): Int {
                // » Dalam 4 karakter pertama (tahun), posisi sama.
                if (offset <= 4) return offset
                // » Pada karakter 5-6 (bulan), geser 1 karena ada satu "-".
                if (offset <= 6) return offset + 1
                // » Sisanya (tanggal), geser 2 karena ada dua "-".
                return offset + 2
            }
            // Kebalikan originalToTransformed untuk posisi kursor.
            // » transformedToOriginal = kebalikannya: posisi di teks tampilan -> posisi di teks asli.
            override fun transformedToOriginal(offset: Int): Int {
                // » Dalam tahun, posisi sama.
                if (offset <= 4) return offset
                // » Pada bagian bulan, kurangi 1.
                if (offset <= 7) return offset - 1
                // » Sisanya kurangi 2.
                return offset - 2
            }
        }
        // » Kembalikan hasil: teks tampilan + pemetaan kursor.
        return TransformedText(AnnotatedString(out.toString()), offsetMapping)
    }
}

// CARI-TIDAK-DIPAKAI | Tidak dipanggil dari navigasi; pembayaran resmi dicatat lewat PaymentsScreen.
// » Mengizinkan fitur Material 3 eksperimental (dropdown).
@OptIn(ExperimentalMaterial3Api::class)
// » Penanda fungsi penggambar tampilan.
@Composable
// » PaymentInputScreen = layar input pembayaran manual (BELUM dipasang di menu). repository = sumber data.
fun PaymentInputScreen(repository: AppCashRepository) {
    // » members = daftar siswa untuk dropdown. Awalnya kosong.
    var members by remember { mutableStateOf<List<Member>>(emptyList()) }
    // » paymentHistory = riwayat input pembayaran. Awalnya kosong.
    var paymentHistory by remember { mutableStateOf<List<PaymentRecord>>(emptyList()) }
    // » loading = true selama data dimuat.
    var loading by remember { mutableStateOf(true) }

    // Form state
    // » selectedMemberId = id siswa terpilih (0 = belum memilih). mutableIntStateOf = state khusus angka bulat.
    var selectedMemberId by remember { mutableIntStateOf(0) }
    // » expandedMemberDropdown = apakah dropdown nama terbuka.
    var expandedMemberDropdown by remember { mutableStateOf(false) }
    // » amountText = teks jumlah bayar; nilai awal "5000" (iuran per minggu).
    var amountText by remember { mutableStateOf("5000") }
    // » dateText = teks tanggal (hanya angka 8 digit); awalnya tanggal hari ini tanpa tanda "-".
    var dateText by remember { mutableStateOf(java.time.LocalDate.now().toString().replace("-", "")) }
    // » noteText = teks keterangan, awalnya kosong.
    var noteText by remember { mutableStateOf("") }
    // » showConfirmDialog = apakah dialog konfirmasi tampil.
    var showConfirmDialog by remember { mutableStateOf(false) }

    // » scope = pengelola tugas belakang.
    val scope = rememberCoroutineScope()
    // » snackbar = pengelola pesan kecil (Snackbar) di bawah layar.
    val snackbar = remember { SnackbarHostState() }

    // Load data
    // » dataVersion = nomor versi data; berubah = muat ulang.
    val dataVersion by repository.version.collectAsState()
    // » Blok dijalankan tiap dataVersion berubah:
    LaunchedEffect(dataVersion) {
        // » Ambil daftar siswa lalu:
        repository.getMembers().fold(
            // » berhasil:
            onSuccess = {
                // » simpan ke members,
                members = it
                // » kalau belum ada siswa terpilih, pilih siswa pertama.
                if (selectedMemberId == 0 && it.isNotEmpty()) selectedMemberId = it.first().id
            },
            // » gagal: abaikan.
            onFailure = {}
        )
        // » Ambil riwayat pembayaran:
        repository.getPaymentHistory().fold(
            // » berhasil: simpan ke paymentHistory,
            onSuccess = { paymentHistory = it },
            // » gagal: abaikan.
            onFailure = {}
        )
        // » Selesai memuat: loading = false.
        loading = false
    }

    // » selectedMember = data siswa terpilih (cari berdasarkan id).
    val selectedMember = members.find { it.id == selectedMemberId }
    // » amount = jumlah bayar sebagai angka (kalau kosong/tidak valid, 0).
    val amount = amountText.toIntOrNull() ?: 0

    // Confirm submit dialog
    // » Kalau dialog diminta, ada siswa terpilih, dan jumlah > 0 → tampilkan dialog konfirmasi.
    if (showConfirmDialog && selectedMember != null && amount > 0) {
        // » AlertDialog = jendela pop-up.
        AlertDialog(
            // » Tutup bila diketuk di luar.
            onDismissRequest = { showConfirmDialog = false },
            // » Sudut bulat 20dp.
            shape = RoundedCornerShape(20.dp),
            // » Ikon di bagian atas dialog:
            icon = {
                // » lingkaran oranye:
                Box(
                    // » ukuran 48dp,
                    modifier = Modifier
                        // » bentuk lingkaran,
                        .size(48.dp)
                        // » latar oranye,
                        .clip(CircleShape)
                        .background(OrangePrimary),
                    // » ikon di tengah.
                    contentAlignment = Alignment.Center
                ) {
                    // » ikon centang putih.
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
            },
            // » Judul "Konfirmasi Pembayaran".
            title = { Text("Konfirmasi Pembayaran", fontWeight = FontWeight.Bold, color = OrangePrimary) },
            // » Isi dialog:
            text = {
                // » kolom rincian, jarak antar baris 8dp.
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // » Baris Nama Anggota.
                    DetailRow("Nama Anggota", selectedMember!!.name)
                    // » Baris Jumlah (format Rp titik ribuan).
                    DetailRow("Jumlah", "Rp ${formatRupiah(amount)}")
                    // » Baris Tanggal: kalau 8 digit, ditulis YYYY-MM-DD.
                    DetailRow("Tanggal", dateText.let { d -> if (d.length == 8) "${d.substring(0,4)}-${d.substring(4,6)}-${d.substring(6,8)}" else d })
                    // » Baris Keterangan hanya jika diisi.
                    if (noteText.isNotBlank()) DetailRow("Keterangan", noteText)
                }
            },
            // » Tombol konfirmasi.
            confirmButton = {
                // » Button Konfirmasi.
                Button(
                    // » Saat ditekan:
                    onClick = {
                        // » tutup dialog,
                        showConfirmDialog = false
                        // » jalankan di jalur belakang:
                        scope.launch {
                            // » catat pembayaran lewat repository dengan:
                            repository.addPaymentRecord(
                                // » id siswa,
                                memberId = selectedMemberId,
                                // » jumlah,
                                amount = amount,
                                // » tanggal (diformat YYYY-MM-DD),
                                date = dateText.let { d -> if (d.length == 8) "${d.substring(0,4)}-${d.substring(4,6)}-${d.substring(6,8)}" else d },
                                // » keterangan (kalau kosong dipakai "Pembayaran Kas"),
                                note = noteText.ifBlank { "Pembayaran Kas" }
                            // » lalu tangani hasilnya:
                            ).fold(
                                // » berhasil:
                                onSuccess = {
                                    // » kosongkan keterangan,
                                    noteText = ""
                                    // Reload history
                                    // » muat ulang riwayat:
                                    repository.getPaymentHistory().fold(
                                        // » berhasil: simpan,
                                        onSuccess = { paymentHistory = it },
                                        // » gagal: abaikan,
                                        onFailure = {}
                                    )
                                    // » tampilkan pesan "✓ Pembayaran berhasil dicatat!".
                                    snackbar.showSnackbar("✓ Pembayaran berhasil dicatat!")
                                },
                                // » gagal:
                                onFailure = {
                                    // » tampilkan pesan "Gagal mencatat pembayaran".
                                    snackbar.showSnackbar("Gagal mencatat pembayaran")
                                }
                            )
                        }
                    },
                    // » Warna tombol oranye.
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                    // » Sudut bulat 10dp.
                    shape = RoundedCornerShape(10.dp)
                ) {
                    // » Ikon centang.
                    Icon(Icons.Default.Check, contentDescription = null)
                    // » Jarak 4dp.
                    Spacer(modifier = Modifier.width(4.dp))
                    // » Tulisan "Konfirmasi" tebal.
                    Text("Konfirmasi", fontWeight = FontWeight.Bold)
                }
            },
            // » Tombol batal.
            dismissButton = {
                // » TextButton: menutup dialog.
                TextButton(onClick = { showConfirmDialog = false }) {
                    // » Tulisan "Batal" oranye.
                    Text("Batal", color = OrangePrimary)
                }
            }
        )
    }

    // » Box = wadah penuh layar dengan latar sesuai tema. Di dalamnya daftar + snackbar.
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // » when = pilih salah satu kondisi:
        when {
            // » Kalau masih loading: tampilkan lingkaran berputar di tengah.
            loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = OrangePrimary)
            // » Kalau tidak:
            else -> {
                // » LazyColumn = daftar bisa digulir.
                LazyColumn(
                    // » Penuhi layar.
                    modifier = Modifier.fillMaxSize(),
                    // » Jarak antar item 12dp.
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Page Header
                    // » item = satu blok di daftar: header halaman.
                    item {
                        // » Kolom header, jarak dalam.
                        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                            // » Teks kecil "Dashboard".
                            Text("Dashboard", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            // » Judul besar "Input Pembayaran".
                            Text("Input Pembayaran", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                        }
                    }

                    // Hero Card
                    // » item: kartu hero oranye di atas.
                    item {
                        // » Box kartu oranye:
                        Box(
                            // » Modifier:
                            modifier = Modifier
                                // » selebar layar,
                                .fillMaxWidth()
                                // » jarak kiri-kanan 16dp,
                                .padding(horizontal = 16.dp)
                                // » dekorasi kartu oranye 20dp,
                                .orangeCard(20.dp)
                                // » jarak dalam 20dp.
                                .padding(20.dp)
                        ) {
                            // » Kolom isi, jarak antar baris 6dp.
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                // » Baris: ikon + teks judul.
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    // » Kotak ikon:
                                    Box(
                                        // » Modifier:
                                        modifier = Modifier
                                            // » ukuran 40dp,
                                            .size(40.dp)
                                            // » sudut bulat 10dp,
                                            .clip(RoundedCornerShape(10.dp))
                                            // » warna putih transparan 25%.
                                            .background(Color.White.copy(alpha = 0.25f)),
                                        // » Ikon di tengah.
                                        contentAlignment = Alignment.Center
                                    ) {
                                        // » Ikon pensil putih.
                                        Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                                    }
                                    // » Kolom teks judul.
                                    Column {
                                        // » Judul "Input Pembayaran Kas".
                                        Text("Input Pembayaran Kas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Color.White)
                                        // » Teks "Catat pembayaran kas anggota kelas".
                                        Text("Catat pembayaran kas anggota kelas", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
                                    }
                                }
                                // » Jarak 4dp.
                                Spacer(modifier = Modifier.height(4.dp))
                                // » Lencana putih transparan, sudut bulat 20dp.
                                Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.2f)) {
                                    // » Teks:
                                    Text(
                                        // » "<jumlah> Transaksi Tercatat" dari panjang riwayat.
                                        "${paymentHistory.size} Transaksi Tercatat",
                                        // » huruf kecil,
                                        style = MaterialTheme.typography.labelSmall,
                                        // » tebal,
                                        fontWeight = FontWeight.Bold,
                                        // » putih,
                                        color = Color.White,
                                        // » jarak dalam lencana.
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Form Card
                    // » item: kartu form.
                    item {
                        // » Card putih:
                        Card(
                            // » Modifier:
                            modifier = Modifier
                                // » selebar layar,
                                .fillMaxWidth()
                                // » jarak kiri-kanan 16dp.
                                .padding(horizontal = 16.dp),
                            // » Sudut bulat 20dp.
                            shape = RoundedCornerShape(20.dp),
                            // » Warna putih.
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            // » Bayangan 2dp.
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            // » Kolom isi form.
                            Column(
                                // » Jarak dalam 16dp.
                                modifier = Modifier.padding(16.dp),
                                // » Jarak antar isian 14dp.
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // » Judul form "Form Pembayaran".
                                Text("Form Pembayaran", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = OrangePrimary)

                                // Member Dropdown
                                // » Dropdown nama anggota.
                                ExposedDropdownMenuBox(
                                    // » Terbuka/tutup mengikuti expandedMemberDropdown.
                                    expanded = expandedMemberDropdown,
                                    // » Saat diketuk: balik status.
                                    onExpandedChange = { expandedMemberDropdown = !expandedMemberDropdown }
                                ) {
                                    // » Isian yang menampilkan pilihan sekarang:
                                    OutlinedTextField(
                                        // » teks: "id - nama" atau "Pilih Anggota",
                                        value = selectedMember?.let { "${it.id} - ${it.name}" } ?: "Pilih Anggota",
                                        // » tidak menerima ketikan,
                                        onValueChange = {},
                                        // » hanya baca,
                                        readOnly = true,
                                        // » label "Nama Anggota",
                                        label = { Text("Nama Anggota") },
                                        // » ikon orang di kiri,
                                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = OrangePrimary) },
                                        // » ikon panah dropdown di kanan,
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMemberDropdown) },
                                        // » selebar dan menjadi jangkar dropdown,
                                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                                        // » sudut bulat 12dp,
                                        shape = RoundedCornerShape(12.dp),
                                        // » garis tepi dan label oranye saat fokus.
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary, focusedLabelColor = OrangePrimary)
                                    )
                                    // » Daftar pilihan dropdown:
                                    ExposedDropdownMenu(
                                        // » terbuka/tutup,
                                        expanded = expandedMemberDropdown,
                                        // » tutup bila diketuk di luar.
                                        onDismissRequest = { expandedMemberDropdown = false }
                                    ) {
                                        // » Untuk tiap siswa (m)...
                                        members.forEach { m ->
                                            // » ...satu baris pilihan:
                                            DropdownMenuItem(
                                                // » teks "id - nama",
                                                text = { Text("${m.id} - ${m.name}") },
                                                // » saat dipilih: simpan id siswa dan tutup dropdown.
                                                onClick = { selectedMemberId = m.id; expandedMemberDropdown = false }
                                            )
                                        }
                                    }
                                }

                                // Amount Input
                                // » Isian jumlah pembayaran.
                                OutlinedTextField(
                                    // » Nilainya amountText.
                                    value = amountText,
                                    // » Saat diketik: hanya angka yang diterima, maksimal 9 digit.
                                    onValueChange = { amountText = it.filter { c -> c.isDigit() }.take(9) },
                                    // » Label "Jumlah Pembayaran (Rp)".
                                    label = { Text("Jumlah Pembayaran (Rp)") },
                                    // » Selebar form.
                                    modifier = Modifier.fillMaxWidth(),
                                    // » Sudut bulat 12dp.
                                    shape = RoundedCornerShape(12.dp),
                                    // » Warna tepi/label oranye saat fokus.
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary, focusedLabelColor = OrangePrimary),
                                    // » Satu baris.
                                    singleLine = true,
                                    // » Teks bantu di bawah isian:
                                    supportingText = {
                                        // » kalau jumlah > 0 tampilkan "Rp <angka berformat>".
                                        if (amount > 0) Text("Rp ${formatRupiah(amount)}", color = OrangePrimary, fontWeight = FontWeight.SemiBold)
                                    }
                                )

                                // Date Input with auto-format
                                // » Isian tanggal.
                                OutlinedTextField(
                                    // » Nilainya dateText (angka saja).
                                    value = dateText,
                                    // » Saat diketik:
                                    onValueChange = { newVal ->
                                        // » ambil angka saja,
                                        val digitsOnly = newVal.filter { c -> c.isDigit() }
                                        // » simpan bila tidak lebih dari 8 digit.
                                        if (digitsOnly.length <= 8) dateText = digitsOnly
                                    },
                                    // » Label "Tanggal (YYYYMMDD)".
                                    label = { Text("Tanggal (YYYYMMDD)") },
                                    // » Contoh isian "20260904".
                                    placeholder = { Text("Contoh: 20260904") },
                                    // » Selebar form.
                                    modifier = Modifier.fillMaxWidth(),
                                    // » Sudut bulat 12dp.
                                    shape = RoundedCornerShape(12.dp),
                                    // » Warna tepi/label oranye saat fokus.
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary, focusedLabelColor = OrangePrimary),
                                    // » Satu baris.
                                    singleLine = true,
                                    // » Tampilan tanggal diberi tanda "-" otomatis oleh DateVisualTransformation.
                                    visualTransformation = DateVisualTransformation(),
                                    // » Teks bantu di bawah:
                                    supportingText = {
                                        // » kalau sudah 8 digit...
                                        if (dateText.length == 8) {
                                            // » ...tampilkan tanggal berformat YYYY-MM-DD:
                                            Text(
                                                // » potong tahun-bulan-hari dari dateText,
                                                "${dateText.substring(0,4)}-${dateText.substring(4,6)}-${dateText.substring(6,8)}",
                                                // » warna oranye,
                                                color = OrangePrimary,
                                                // » agak tebal.
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        // » kalau belum 8 digit...
                                        } else {
                                            // » ...tampilkan "Ketik 8 digit angka".
                                            Text("Ketik 8 digit angka", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                )

                                // Note Input
                                // » Isian keterangan (opsional).
                                OutlinedTextField(
                                    // » Nilainya noteText.
                                    value = noteText,
                                    // » Saat diketik: simpan.
                                    onValueChange = { noteText = it },
                                    // » Label "Keterangan (opsional)".
                                    label = { Text("Keterangan (opsional)") },
                                    // » Selebar form.
                                    modifier = Modifier.fillMaxWidth(),
                                    // » Sudut bulat 12dp.
                                    shape = RoundedCornerShape(12.dp),
                                    // » Warna oranye saat fokus.
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary, focusedLabelColor = OrangePrimary),
                                    // » Satu baris.
                                    singleLine = true
                                )

                                // Submit Button
                                // » Tombol "Catat Pembayaran".
                                Button(
                                    // » Saat ditekan: tampilkan dialog konfirmasi.
                                    onClick = { showConfirmDialog = true },
                                    // » Selebar form, tinggi 50dp.
                                    modifier = Modifier.fillMaxWidth().height(50.dp),
                                    // » Tombol aktif bila: siswa terpilih, jumlah > 0, tanggal 8 digit...
                                    enabled = selectedMember != null && amount > 0 && dateText.length == 8 &&
                                        // » ...dan tanggal benar-benar valid (isValidDate).
                                        isValidDate("${dateText.take(4)}-${dateText.substring(4, 6)}-${dateText.takeLast(2)}"),
                                    // » Warna oranye.
                                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                                    // » Sudut bulat 14dp.
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    // » Ikon centang putih.
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                                    // » Jarak 8dp.
                                    Spacer(modifier = Modifier.width(8.dp))
                                    // » Teks "Catat Pembayaran" putih tebal.
                                    Text("Catat Pembayaran", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }

                    // Recent Payments Header
                    // » Kalau ada riwayat, tampilkan daftar riwayat.
                    if (paymentHistory.isNotEmpty()) {
                        // » item: judul daftar riwayat.
                        item {
                            // » Teks:
                            Text(
                                // » "Riwayat Pembayaran Terbaru",
                                "Riwayat Pembayaran Terbaru",
                                // » gaya sub-judul kecil,
                                style = MaterialTheme.typography.titleSmall,
                                // » tebal,
                                fontWeight = FontWeight.Bold,
                                // » oranye,
                                color = OrangePrimary,
                                // » jarak kiri-kanan 20dp.
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                        }

                        // » Tampilkan 20 riwayat terbaru (take(20)), tiap satu sebagai kartu.
                        items(paymentHistory.take(20)) { record ->
                            // » Kartu satu riwayat.
                            PaymentHistoryCard(record = record)
                        }
                    }

                    // » item penutup: jarak kosong 8dp.
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
            }
        }
        // » SnackbarHost = tempat pesan kecil muncul, di tengah bawah layar.
        SnackbarHost(hostState = snackbar, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

// Kartu satu riwayat input pembayaran (layar ini belum dipasang di menu).
// » Penanda fungsi penggambar tampilan.
@Composable
// » PaymentHistoryCard = kartu satu riwayat input pembayaran.
fun PaymentHistoryCard(record: PaymentRecord) {
    // » Box kartu oranye:
    Box(
        // » Modifier:
        modifier = Modifier
            // » selebar layar,
            .fillMaxWidth()
            // » jarak kiri-kanan 16dp,
            .padding(horizontal = 16.dp)
            // » kartu oranye 16dp,
            .orangeCard(16.dp)
            // » jarak dalam 14dp.
            .padding(14.dp)
    ) {
        // » Baris mendatar isi kartu.
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Initial Badge
            // » Lingkaran inisial:
            Box(
                // » Modifier:
                modifier = Modifier
                    // » ukuran 44dp,
                    .size(44.dp)
                    // » lingkaran,
                    .clip(CircleShape)
                    // » putih transparan 25%.
                    .background(Color.White.copy(alpha = 0.25f)),
                // » Isi di tengah.
                contentAlignment = Alignment.Center
            ) {
                // » Teks inisial:
                Text(
                    // » huruf pertama nama (huruf besar),
                    text = record.memberName.take(1).uppercase(),
                    // » gaya sub-judul,
                    style = MaterialTheme.typography.titleMedium,
                    // » sangat tebal,
                    fontWeight = FontWeight.ExtraBold,
                    // » putih.
                    color = Color.White
                )
            }
            // » Jarak mendatar 12dp.
            Spacer(modifier = Modifier.width(12.dp))
            // » Kolom nama dan keterangan, mengisi sisa lebar.
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                // » Teks nama siswa (maksimal 1 baris, kalau panjang dipotong dengan "...").
                Text(record.memberName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                // » Teks keterangan (maksimal 1 baris).
                Text(record.note, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            // » Kolom kanan (rata kanan): jumlah dan tanggal.
            Column(horizontalAlignment = Alignment.End) {
                // » Teks jumlah "Rp <angka>", sangat tebal, putih.
                Text("Rp ${formatRupiah(record.amount)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.ExtraBold, color = Color.White)
                // » Teks tanggal, huruf kecil, putih agak transparan.
                Text(record.date, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.75f))
            }
        }
    }
}
