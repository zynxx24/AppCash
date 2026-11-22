// ==========================================================================
// PETA FILE · AppCash (Kelompok 6) · PaymentsScreen.kt
// Isi file : Halaman Kas: admin mencentang pembayaran per minggu, siswa melihat status & membayar.
// Penanda  : CARI-PEMBAYARAN, CARI-SALDO, CARI-DENDA, CARI-DISPENSASI, CARI-QRIS, CARI-WHATSAPP, CARI-ANGGOTA
// Cara cari: VS Code Ctrl+Shift+F (Android Studio Ctrl+Shift+F) lalu ketik
//            "CARI-" untuk semua penanda, atau salah satu nama di baris Penanda.
// ==========================================================================
//  BACA INI DULU : TAB "KAS" (paling kiri)
//  Isinya BERBEDA untuk admin dan siswa. PaymentsScreen memilih salah satu:
//    ADMIN  -> AdminPaymentsScreen  : kartu saldo, tombol "Tambah Pembayaran", daftar 37 minggu (M01..M37)
//               - menekan satu minggu  -> AdminWeekDetailDialog : centang/lepas pembayaran SEMUA siswa untuk minggu itu
//               - tombol Tambah        -> AdminPaymentDialog    : pilih siswa, centang minggu yang dibayar, total = kas + denda
//    SISWA  -> UserPaymentsScreen   : kartu status pribadi, tombol "Bayar Kas (N minggu)", riwayat tiap minggu (Lunas/Dispensasi/Belum Bayar)
//               - tombol Bayar        -> UserBniPaymentDialog   : transfer BNI atau scan QRIS, lalu kirim bukti ke WA admin, lalu Konfirmasi
//  Alur bayar siswa: 1) pilih minggu  2) bayar via BNI/QRIS  3) tombol "1. Kirim Bukti ke WA Admin"  4) tombol "2. Konfirmasi" (baru aktif setelah langkah 3).
//  Semua perubahan disimpan lewat repository.savePayments, lalu saldo dan denda otomatis ikut berubah.
//  Komponen bersama: PaymentDateCard (kartu satu minggu), StudentAvatar (foto siswa).
//  MiniBarChart di akhir file adalah grafik batang kecil yang TIDAK dipanggil dari mana pun.
// » Alamat folder (package) file ini di dalam proyek.
package com.appcash.ui.screens

// » orangeCard = dekorasi kartu oranye bergradien (Modifiers.kt).
import com.appcash.ui.theme.orangeCard
// » ClipData = isi yang disalin ke clipboard.
import android.content.ClipData
// » ClipboardManager = pengelola clipboard (salin teks).
import android.content.ClipboardManager
// » Context = pegangan ke lingkungan aplikasi (dibutuhkan untuk membuka penyimpanan, WhatsApp, dll).
import android.content.Context
// » Intent = 'niat' membuka aplikasi lain, mis. membuka WhatsApp atau membagikan teks.
import android.content.Intent
// » Uri = alamat/tautan, mis. tautan WhatsApp wa.me.
import android.net.Uri
// » Toast = pesan kecil singkat yang muncul di bawah layar.
import android.widget.Toast
// » Canvas = kanvas kosong untuk menggambar sendiri (garis, lingkaran, grafik).
import androidx.compose.foundation.Canvas
// » Image = menampilkan gambar.
import androidx.compose.foundation.Image
// » background = mengisi warna/gradien latar sebuah tampilan.
import androidx.compose.foundation.background
// » clickable = membuat tampilan bisa diketuk.
import androidx.compose.foundation.clickable
// » Layout dasar: Column (susun atas-bawah), Row (susun kiri-kanan), Box (tumpuk), Spacer (jarak kosong),
// » padding, size, fillMaxWidth, dll.
import androidx.compose.foundation.layout.*
// » LazyColumn = daftar vertikal yang hanya menggambar baris yang terlihat (hemat memori).
import androidx.compose.foundation.lazy.LazyColumn
// » items = mengulang satu tampilan untuk setiap elemen daftar.
import androidx.compose.foundation.lazy.items
// » itemsIndexed = seperti items tetapi sekaligus membawa nomor urut.
import androidx.compose.foundation.lazy.itemsIndexed
// » rememberScrollState = menyimpan posisi gulir.
import androidx.compose.foundation.rememberScrollState
// » CircleShape = bentuk lingkaran.
import androidx.compose.foundation.shape.CircleShape
// » RoundedCornerShape = bentuk persegi dengan sudut membulat.
import androidx.compose.foundation.shape.RoundedCornerShape
// » verticalScroll = membuat isi bisa digulir naik-turun.
import androidx.compose.foundation.verticalScroll
// » Icons = daftar ikon bawaan Material.
import androidx.compose.material.icons.Icons
// » Ikon bawaan Material: Add (versi terisi).
import androidx.compose.material.icons.filled.Add
// » Ikon bawaan Material: Check (versi terisi).
import androidx.compose.material.icons.filled.Check
// » Ikon bawaan Material: Person (versi terisi).
import androidx.compose.material.icons.filled.Person
// » Ikon bawaan Material: Send (versi terisi).
import androidx.compose.material.icons.filled.Send
// » Ikon bawaan Material: Share (versi terisi).
import androidx.compose.material.icons.filled.Share
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
// » asImageBitmap = mengubah bitmap Android menjadi gambar Compose.
import androidx.compose.ui.graphics.asImageBitmap
// » ContentScale = cara gambar menyesuaikan kotak (Crop = penuhi kotak dan potong kelebihan).
import androidx.compose.ui.layout.ContentScale
// » LocalContext = mengambil Context aplikasi di dalam tampilan (untuk membuka WhatsApp, Toast, dll).
import androidx.compose.ui.platform.LocalContext
// » painterResource = mengambil gambar dari folder res/drawable.
import androidx.compose.ui.res.painterResource
// » FontWeight = ketebalan huruf (Normal, Medium, Bold, ExtraBold).
import androidx.compose.ui.text.font.FontWeight
// » TextAlign = perataan teks (kiri, tengah, kanan).
import androidx.compose.ui.text.style.TextAlign
// » TextOverflow = cara menangani teks yang terlalu panjang (Ellipsis = dipotong dengan '...').
import androidx.compose.ui.text.style.TextOverflow
// » dp = satuan ukuran Android yang sama di semua layar (mis. 16.dp).
import androidx.compose.ui.unit.dp
// » R = daftar semua resource (gambar, teks) yang dibuat otomatis Android.
import com.appcash.R
// » Semua bentuk data (Member, Expense, Donation, dst) dari Models.kt.
import com.appcash.data.model.*
// » AppCashRepository = pusat data & rumus aplikasi (saldo, denda, pembayaran).
import com.appcash.data.repository.AppCashRepository
// » GreenPositive = hijau untuk lunas dan pemasukan.
import com.appcash.ui.theme.GreenPositive
// » OrangeDark = oranye gelap.
import com.appcash.ui.theme.OrangeDark
// » OrangeLight = oranye terang.
import com.appcash.ui.theme.OrangeLight
// » OrangePrimary = warna oranye utama.
import com.appcash.ui.theme.OrangePrimary
// » RedNegative = merah untuk denda dan pengeluaran.
import com.appcash.ui.theme.RedNegative
// » TextMedium = abu tua untuk teks keterangan.
import com.appcash.ui.theme.TextMedium
// » launch = menjalankan tugas di jalur belakang (coroutine) supaya layar tidak membeku.
import kotlinx.coroutines.launch

// » Judul bagian: PAYMENTS SCREEN dipisah menurut peran (admin / siswa).
// ============================================================
// PAYMENTS SCREEN – Branched by role
// ============================================================

// » Penanda fungsi penggambar tampilan.
// Pintu masuk tab Kas: isAdmin true -> AdminPaymentsScreen, selain itu -> UserPaymentsScreen.
// Menu Kas: admin melihat layar kelola pembayaran, siswa melihat status kas miliknya.
@Composable
// » PaymentsScreen = tab KAS (tab pertama). Hanya "pintu masuk": memilih tampilan sesuai peran. repository =
// » sumber data; isAdmin = apakah admin.
fun PaymentsScreen(repository: AppCashRepository, isAdmin: Boolean) {
    // » Kalau admin...
    if (isAdmin) {
        // » ...tampilkan AdminPaymentsScreen (kelola pembayaran semua siswa).
        AdminPaymentsScreen(repository)
    // » Kalau bukan admin...
    } else {
        // » ...tampilkan UserPaymentsScreen (status kas pribadi siswa).
        UserPaymentsScreen(repository)
    }
}

// ============================================================
// ADMIN PAYMENTS SCREEN
// ============================================================

// Tampilan admin. State: payments (tabel centang), members, dashboard, showAdminPayment (dialog tambah), selectedWeekDate (dialog detail minggu).
// Susunan: judul "Kelola Pembayaran Kas" -> kartu saldo (AdminHeroCard) -> tombol Tambah Pembayaran -> daftar minggu.
// CARI-PEMBAYARAN | Tampilan admin: kartu saldo, tombol Tambah Pembayaran, daftar minggu kas.
@Composable
// » Penanda fungsi penggambar tampilan.
fun AdminPaymentsScreen(repository: AppCashRepository) {
    // » AdminPaymentsScreen = tampilan admin tab Kas: kartu saldo, tombol Tambah Pembayaran, daftar 37 minggu.
    var payments by remember { mutableStateOf<PaymentsWrapper?>(null) }
    // » payments = tabel centang pembayaran seluruh siswa (null = belum dimuat).
    var members by remember { mutableStateOf<List<Member>>(emptyList()) }
    // » members = daftar siswa.
    var dashboard by remember { mutableStateOf<Dashboard?>(null) }
    // » dashboard = angka saldo untuk kartu oranye.
    var error by remember { mutableStateOf<String?>(null) }
    // » error = pesan error; null = tidak ada.
    var loading by remember { mutableStateOf(true) }
    // » loading = true selama data dimuat.
    var showAdminPayment by remember { mutableStateOf(false) }
    // » showAdminPayment = apakah dialog Tambah Pembayaran tampil.
    var selectedWeekDate by remember { mutableStateOf<String?>(null) }
    // » selectedWeekDate = label minggu yang sedang dibuka detailnya (null = tidak ada).
    var selectedWeekIndex by remember { mutableIntStateOf(0) }
    // » selectedWeekIndex = nomor minggu (1-37) yang sedang dibuka.
    val scope = rememberCoroutineScope()
    // » scope = pengelola tugas belakang.
    val snackbar = remember { SnackbarHostState() }
// » snackbar = pengelola pesan kecil di bawah layar.

    // Memuat ulang tabel pembayaran dari repository setelah ada perubahan.
    // » reload() = fungsi kecil yang memuat ulang tabel dan saldo setelah ada perubahan.
    fun reload() {
        // » Jalankan di jalur belakang:
        scope.launch {
            // » ambil tabel pembayaran lalu:
            repository.getPayments().fold(
                // » berhasil: simpan ke payments,
                onSuccess = { payments = it },
                // » gagal: abaikan.
                onFailure = {}
            )
            // » ambil angka dashboard lalu:
            repository.getDashboard().fold(
                // » berhasil: simpan ke dashboard,
                onSuccess = { dashboard = it },
                // » gagal: abaikan.
                onFailure = {}
            )
        }
    }

    // » dataVersion = nomor versi data; berubah = muat ulang.
    val dataVersion by repository.version.collectAsState()
    // » Blok dijalankan tiap dataVersion berubah:
    LaunchedEffect(dataVersion) {
        // » hapus error lama.
        error = null
        // » ambil tabel pembayaran lalu:
        repository.getPayments().fold(
            // » berhasil: simpan,
            onSuccess = { payments = it },
            // » gagal: simpan pesan error.
            onFailure = { error = it.message ?: "Gagal memuat pembayaran" }
        )
        // » ambil daftar siswa lalu:
        repository.getMembers().fold(
            // » berhasil: simpan,
            onSuccess = { members = it },
            // » gagal: abaikan.
            onFailure = {}
        )
        // » ambil angka dashboard lalu:
        repository.getDashboard().fold(
            // » berhasil: simpan,
            onSuccess = { dashboard = it },
            // » gagal: abaikan.
            onFailure = {}
        )
        // » Selesai: loading = false.
        loading = false
    }

    // Dialog Tambah Pembayaran (tampil bila showAdminPayment true). onConfirm -> repository.savePayments lalu muat ulang.
    // Admin Payment Dialog
    // » Kalau dialog Tambah Pembayaran diminta dan tabel sudah ada, tampilkan.
    if (showAdminPayment && payments != null) {
        // » AdminPaymentDialog:
        AdminPaymentDialog(
            // » tabel pembayaran sekarang,
            wrapper = payments!!,
            // » daftar siswa,
            members = members,
            // » repository (untuk hitung denda),
            repository = repository,
            // » onDismiss = tutup dialog,
            onDismiss = { showAdminPayment = false },
            // » onConfirm = dijalankan saat Konfirmasi, membawa tabel baru (newWrapper).
            onConfirm = { newWrapper ->
                // » Tutup dialog.
                showAdminPayment = false
                // » Jalankan di jalur belakang:
                scope.launch {
                    // » simpan tabel baru lewat repository.savePayments lalu:
                    repository.savePayments(newWrapper).fold(
                        // » berhasil:
                        onSuccess = {
                            // » perbarui tabel di layar,
                            payments = newWrapper
                            // » tampilkan "✓ Pembayaran berhasil dicatat!",
                            snackbar.showSnackbar("✓ Pembayaran berhasil dicatat!")
                            // » muat ulang saldo dan tabel.
                            reload()
                        },
                        // » gagal: tampilkan "Gagal menyimpan".
                        onFailure = { snackbar.showSnackbar("Gagal menyimpan") }
                    )
                }
            }
        )
    }

    // Dialog detail satu minggu (tampil bila selectedWeekDate tidak kosong). Admin bisa mencentang ulang semua siswa di minggu itu.
    // Week Detail Dialog (admin can edit)
    // » Kalau ada minggu yang dibuka detailnya dan tabel sudah ada, tampilkan dialog detail minggu.
    if (selectedWeekDate != null && payments != null) {
        // » AdminWeekDetailDialog:
        AdminWeekDetailDialog(
            // » nomor minggu,
            weekIndex = selectedWeekIndex,
            // » label minggu,
            date = selectedWeekDate!!,
            // » daftar siswa,
            members = members,
            // » tabel pembayaran,
            wrapper = payments!!,
            // » onDismiss = tutup,
            onDismiss = { selectedWeekDate = null },
            // » onSave = dijalankan saat Simpan, membawa tabel baru.
            onSave = { newWrapper ->
                // » Tutup dialog.
                selectedWeekDate = null
                // » Jalankan di jalur belakang:
                scope.launch {
                    // » simpan lewat repository.savePayments lalu:
                    repository.savePayments(newWrapper).fold(
                        // » berhasil:
                        onSuccess = {
                            // » perbarui tabel di layar,
                            payments = newWrapper
                            // » tampilkan "✓ Data pembayaran diperbarui!",
                            snackbar.showSnackbar("✓ Data pembayaran diperbarui!")
                            // » muat ulang.
                            reload()
                        },
                        // » gagal: tampilkan "Gagal menyimpan".
                        onFailure = { snackbar.showSnackbar("Gagal menyimpan") }
                    )
                }
            }
        )
    }

    // » Box penuh layar dengan latar sesuai tema.
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // » when = pilih satu keadaan:
        when {
            // » loading: lingkaran berputar di tengah.
            loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = OrangePrimary)
            // » error: ErrorView dengan tombol coba lagi.
            error != null -> ErrorView(error = error!!, onRetry = { repository.refresh() })
            // » data siap:
            payments != null -> {
                // » p = tabel pembayaran (pasti ada).
                val p = payments!!
                // » d = angka dashboard.
                val d = dashboard
                // » Kolom isi layar.
                Column(modifier = Modifier.fillMaxSize()) {
                    // Judul halaman admin.
                    // Page Header
                    // » Kolom judul halaman.
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                        // » Teks kecil "Admin".
                        Text("Admin", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        // » Judul "Kelola Pembayaran Kas".
                        Text("Kelola Pembayaran Kas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                    }

                    // Hero Card with balance
                    // » Kartu saldo oranye (AdminHeroCard).
                    AdminHeroCard(d = d)

                    // » Jarak kosong 12dp.
                    Spacer(modifier = Modifier.height(12.dp))

                    // Add Payment Button
                    // » Button Tambah Pembayaran:
                    Button(
                        // » saat ditekan: tampilkan dialog tambah,
                        onClick = { showAdminPayment = true },
                        // » Modifier:
                        modifier = Modifier
                            // » selebar layar,
                            .fillMaxWidth()
                            // » jarak kiri-kanan 16dp,
                            .padding(horizontal = 16.dp)
                            // » tinggi 52dp.
                            .height(52.dp),
                        // » Warna oranye.
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                        // » Sudut bulat 14dp.
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        // » Ikon plus putih.
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                        // » Jarak 8dp.
                        Spacer(modifier = Modifier.width(8.dp))
                        // » Tulisan "Tambah Pembayaran" putih tebal.
                        Text("Tambah Pembayaran", fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    // » Jarak kosong 16dp.
                    Spacer(modifier = Modifier.height(16.dp))

                    // Daftar 37 minggu. Tiap kartu menampilkan "Minggu ke-N", tanggal, dan "x/33 Anggota" yang sudah lunas (paidCount dihitung dari tabel centang).
                    // Payment Dates List
                    // » Kalau belum ada minggu sama sekali...
                    if (p.dates.isEmpty()) {
                        // » ...tampilkan kotak di tengah:
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            // » teks "Belum ada data pembayaran", abu, rata tengah.
                            Text("Belum ada data pembayaran", color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                        }
                    // » Kalau ada...
                    } else {
                        // » ...LazyColumn = daftar minggu yang bisa digulir:
                        LazyColumn(
                            // » mengisi sisa tinggi,
                            modifier = Modifier.weight(1f),
                            // » jarak isi 16dp kiri-kanan,
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                            // » jarak antar kartu 10dp.
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // » Untuk setiap minggu (idx = urutan, date = label):
                            itemsIndexed(p.dates) { idx, date ->
                                // » paidCount = jumlah siswa yang lunas minggu itu (hitung dari tabel).
                                val paidCount = p.records.count { (_, dates) -> dates[date] == true }
                                // » Tampilkan kartu minggu (PaymentDateCard):
                                PaymentDateCard(
                                    // » nomor minggu (urutan + 1),
                                    weekIndex = idx + 1,
                                    // » label minggu,
                                    date = date,
                                    // » jumlah lunas,
                                    paidCount = paidCount,
                                    // » jumlah siswa (33),
                                    totalMembers = members.size,
                                    // » mode admin,
                                    isAdmin = true,
                                    // » saat diketuk:
                                    onClick = {
                                        // » simpan nomor minggu,
                                        selectedWeekIndex = idx + 1
                                        // » simpan label minggu (buka dialog detail).
                                        selectedWeekDate = date
                                    },
                                    // » saat tombol edit ditekan:
                                    onEdit = {
                                        // » simpan nomor minggu,
                                        selectedWeekIndex = idx + 1
                                        // » simpan label minggu (buka dialog detail).
                                        selectedWeekDate = date
                                    }
                                )
                            }
                            // » item penutup: jarak kosong 8dp.
                            item { Spacer(modifier = Modifier.height(8.dp)) }
                        }
                    }
                }
            }
        }
        // » SnackbarHost = tempat pesan kecil di tengah bawah layar.
        SnackbarHost(hostState = snackbar, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

// Kartu oranye ringkasan saldo di tab Kas admin: total kas, pemasukan, pengeluaran (angka dari Dashboard).
// CARI-SALDO | Kartu oranye berisi total kas, pemasukan (kas + donasi), dan pengeluaran.
// » Penanda fungsi penggambar tampilan.
@Composable
// » AdminHeroCard = kartu oranye ringkasan saldo. d = paket angka dashboard (boleh null).
fun AdminHeroCard(d: Dashboard?) {
    // » Box kartu oranye:
    Box(
        // » Modifier:
        modifier = Modifier
            // » selebar layar,
            .fillMaxWidth()
            // » jarak kiri-kanan 16dp,
            .padding(horizontal = 16.dp)
            // » kartu oranye 20dp,
            .orangeCard(20.dp)
            // » jarak dalam 16dp.
            .padding(16.dp)
    ) {
        // » Kolom isi, jarak antar baris 12dp.
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // » Teks judul kartu "Total Kas XII PPLG" (kecil, putih agak transparan).
            Text("Total Kas XII PPLG", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.85f))
            // » Teks SALDO besar:
            Text(
                // » "Rp" + saldo buku (kalau data belum ada, 0),
                "Rp ${formatRupiah(d?.totalBalance ?: 0)}",
                // » gaya judul sangat besar,
                style = MaterialTheme.typography.headlineLarge,
                // » sangat tebal,
                fontWeight = FontWeight.ExtraBold,
                // » putih.
                color = Color.White
            )

            // » Baris dua kotak putih kecil, jarak 8dp.
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // » Kotak Total Pemasukan: sudut bulat 12dp, putih, lebar sama.
                Surface(shape = RoundedCornerShape(12.dp), color = Color.White, modifier = Modifier.weight(1f)) {
                    // » Kolom isi kotak, jarak dalam.
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                        // » Label "Total Pemasukan" (kecil, abu).
                        Text("Total Pemasukan", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        // » Nilai:
                        Text(
                            // » "Rp" + iuran + donasi,
                            "Rp ${formatRupiah((d?.totalKasIncome ?: 0) + (d?.totalDonationAmount ?: 0))}",
                            // » gaya label sedang,
                            style = MaterialTheme.typography.labelMedium,
                            // » tebal,
                            fontWeight = FontWeight.Bold,
                            // » hijau.
                            color = Color(0xFF22C55E)
                        )
                    }
                }
                // » Kotak Total Pengeluaran: sudut bulat 12dp, putih, lebar sama.
                Surface(shape = RoundedCornerShape(12.dp), color = Color.White, modifier = Modifier.weight(1f)) {
                    // » Kolom isi kotak.
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                        // » Label "Total Pengeluaran".
                        Text("Total Pengeluaran", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        // » Nilai:
                        Text(
                            // » "Rp" + total pengeluaran,
                            "Rp ${formatRupiah(d?.totalExpenseAmount ?: 0)}",
                            // » gaya label sedang,
                            style = MaterialTheme.typography.labelMedium,
                            // » tebal,
                            fontWeight = FontWeight.Bold,
                            // » merah.
                            color = Color(0xFFEF4444)
                        )
                    }
                }
            }
        }
    }
}

// » Judul bagian: DIALOG TAMBAH PEMBAYARAN (admin).
// ============================================================
// ADMIN PAYMENT DIALOG – Select student → show unpaid → confirm
// ============================================================

// DIALOG TAMBAH PEMBAYARAN (admin):
//   1. pilih siswa (dropdown berfoto)
//   2. daftar minggu yang BELUM lunas dan bukan dispensasi (unpaidDates), centang minggu yang dibayar. Ada tombol "pilih semua".
//   3. rincian: kas = jumlah minggu dicentang x Rp5.000, denda = dendaForWeeks(minggu dicentang), total = kas + denda
//   4. Konfirmasi: minggu dicentang ditandai lunas di tabel lalu disimpan.
// CARI-PEMBAYARAN CARI-DENDA | Admin memilih siswa lalu mencentang minggu yang dibayar; total = kas + denda minggu terpilih.
// » Mengizinkan fitur Material 3 eksperimental.
@OptIn(ExperimentalMaterial3Api::class)
// » Penanda fungsi penggambar tampilan.
@Composable
// » AdminPaymentDialog = dialog Tambah Pembayaran. Parameter:
fun AdminPaymentDialog(
    // » wrapper = tabel centang sekarang,
    wrapper: PaymentsWrapper,
    // » members = daftar siswa,
    members: List<Member>,
    // » repository = untuk menghitung denda,
    repository: AppCashRepository,
    // » onDismiss = tutup dialog,
    onDismiss: () -> Unit,
    // » onConfirm = kirim tabel baru yang sudah dicentang.
    onConfirm: (PaymentsWrapper) -> Unit
) {
    // » selectedMemberId = id siswa yang dipilih di dropdown; awalnya siswa pertama.
    var selectedMemberId by remember { mutableStateOf(members.firstOrNull()?.id ?: 1) }
    // » expandedMemberDropdown = apakah dropdown siswa terbuka.
    var expandedMemberDropdown by remember { mutableStateOf(false) }

    // » records = salinan tabel centang yang BOLEH diubah di dialog ini (agar tabel asli aman sampai
    // » Konfirmasi). remember(wrapper) = dibuat ulang hanya bila tabel asli berganti.
    val records = remember(wrapper) {
        // » Wadah state berbentuk peta: id siswa -> (minggu -> lunas?).
        mutableStateMapOf<String, MutableMap<String, Boolean>>().apply {
            // » Salin tiap baris tabel asli ke wadah baru.
            wrapper.records.forEach { (k, v) -> put(k, v.toMutableMap()) }
        }
    }

    // » selectedMember = data siswa terpilih (kalau tidak ketemu, pakai siswa pertama).
    val selectedMember = members.find { it.id == selectedMemberId } ?: members.firstOrNull()
    // » memberKey = id siswa terpilih dalam bentuk teks (kunci tabel).
    val memberKey = selectedMember?.id?.toString() ?: "1"
    // » memberRecords = centang milik siswa itu (kosong bila belum ada).
    val memberRecords: Map<String, Boolean> = records[memberKey] ?: emptyMap()
    // Minggu yang bisa dipilih = belum lunas DAN bukan minggu dispensasi siswa itu.
    // » unpaidDates = minggu yang BISA dipilih: yang belum lunas DAN bukan minggu dispensasi siswa itu.
    val unpaidDates = wrapper.dates.filter { date -> memberRecords[date] != true && !repository.isExempt(selectedMemberId, date) }
    // » selectedDates = minggu yang dicentang admin di dialog (peta minggu -> true/false).
    val selectedDates = remember { mutableStateMapOf<String, Boolean>() }
    // » selectedCount = jumlah minggu yang dicentang.
    val selectedCount = selectedDates.count { it.value }
    // Kas = jumlah minggu dicentang x Rp5.000. Denda di bawahnya hanya untuk minggu yang dicentang, bukan seluruh tunggakan.
    // » kasAmount = uang kas yang dibayar = jumlah minggu dicentang x Rp5.000.
    val kasAmount = selectedCount * 5000
    // Denda hanya untuk minggu yang dicentang, bukan seluruh tunggakan
    // » dendaAmount = denda hanya untuk minggu yang dicentang (repository.dendaForWeeks).
    val dendaAmount = repository.dendaForWeeks(selectedDates.filterValues { it }.keys)
    // » allSelected = true kalau semua minggu yang bisa dipilih sudah dicentang (dipakai tombol "Pilih Semua").
    val allSelected = unpaidDates.isNotEmpty() && unpaidDates.all { selectedDates[it] == true }

    // Reset selection when member changes
    // » Setiap siswa yang dipilih berganti, kosongkan centang minggu.
    LaunchedEffect(selectedMemberId) {
        // » Hapus semua centang.
        selectedDates.clear()
    }

    // » AlertDialog = jendela pop-up.
    AlertDialog(
        // » Tutup bila diketuk di luar.
        onDismissRequest = onDismiss,
        // » Sudut bulat 24dp.
        shape = RoundedCornerShape(24.dp),
        // » Judul dialog:
        title = {
            // » kolom judul.
            Column {
                // » Baris ikon + teks.
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // » Kotak ikon:
                    Box(
                        // » Modifier:
                        modifier = Modifier
                            // » ukuran 40dp,
                            .size(40.dp)
                            // » sudut bulat 10dp,
                            .clip(RoundedCornerShape(10.dp))
                            // » latar oranye.
                            .background(OrangePrimary),
                        // » Ikon di tengah.
                        contentAlignment = Alignment.Center
                    ) {
                        // » Ikon plus putih.
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    // » Kolom teks judul.
                    Column {
                        // » Judul "Tambah Pembayaran".
                        Text("Tambah Pembayaran", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = OrangePrimary)
                        // » Petunjuk "Pilih siswa & centang minggu yang dibayar".
                        Text("Pilih siswa & centang minggu yang dibayar", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        },
        text = {
            // » Isi dialog:
            Column(
                // » kolom dengan jarak 12dp,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                // » bisa digulir.
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                // Member Selection with photo
                // » Dropdown pilihan siswa (dengan foto).
                ExposedDropdownMenuBox(
                    // » Terbuka/tutup mengikuti expandedMemberDropdown.
                    expanded = expandedMemberDropdown,
                    // » Saat diketuk balik status.
                    onExpandedChange = { expandedMemberDropdown = !expandedMemberDropdown }
                ) {
                    // » Isian yang menampilkan pilihan:
                    OutlinedTextField(
                        // » teks "id - nama" atau "Pilih Anggota",
                        value = selectedMember?.let { "${it.id} - ${it.name}" } ?: "Pilih Anggota",
                        // » tidak menerima ketikan,
                        onValueChange = {},
                        // » hanya baca,
                        readOnly = true,
                        // » label "Pilih Siswa",
                        label = { Text("Pilih Siswa") },
                        // » ikon di kiri:
                        leadingIcon = {
                            // » kalau ada siswa terpilih...
                            if (selectedMember != null) {
                                // » ...tampilkan fotonya ukuran 28,
                                StudentAvatar(memberId = selectedMember.id, size = 28)
                            // » kalau tidak...
                            } else {
                                // » ...tampilkan ikon orang oranye.
                                Icon(Icons.Default.Person, contentDescription = null, tint = OrangePrimary)
                            }
                        },
                        // » ikon panah dropdown di kanan,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMemberDropdown) },
                        // » selebar dialog dan menjadi jangkar dropdown,
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        // » sudut bulat 12dp,
                        shape = RoundedCornerShape(12.dp),
                        // » warna oranye saat fokus.
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
                                // » isi pilihan:
                                text = {
                                    // » baris foto + nama,
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        // » foto siswa ukuran 28,
                                        StudentAvatar(memberId = m.id, size = 28)
                                        // » teks "id - nama" huruf kecil.
                                        Text("${m.id} - ${m.name}", style = MaterialTheme.typography.bodySmall)
                                    }
                                },
                                // » Saat dipilih: simpan id siswa dan tutup dropdown.
                                onClick = { selectedMemberId = m.id; expandedMemberDropdown = false }
                            )
                        }
                    }
                }

                // Unpaid weeks header
                // » Judul daftar: "Minggu yang belum dibayar (Rp 5.000/minggu):".
                Text("Minggu yang belum dibayar (Rp 5.000/minggu):", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = OrangeDark)

                // » Kalau tidak ada minggu yang belum dibayar...
                if (unpaidDates.isEmpty()) {
                    // » ...kotak hijau muda:
                    Surface(shape = RoundedCornerShape(10.dp), color = GreenPositive.copy(alpha = 0.1f)) {
                        // » baris isi kotak,
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            // » ikon centang hijau,
                            Icon(Icons.Default.Check, contentDescription = null, tint = GreenPositive, modifier = Modifier.size(18.dp))
                            // » jarak 8dp,
                            Spacer(modifier = Modifier.width(8.dp))
                            // » teks "Semua minggu kas sudah LUNAS!" hijau tebal.
                            Text("Semua minggu kas sudah LUNAS!", style = MaterialTheme.typography.bodyMedium, color = GreenPositive, fontWeight = FontWeight.Bold)
                        }
                    }
                // » Kalau ada minggu belum dibayar...
                } else {
                    // Select all button
                    // » Baris tombol "Pilih Semua", rata kanan.
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        // » Tombol teks:
                        TextButton(onClick = {
                            // » saat ditekan: kalau semua sudah terpilih, batalkan semua; kalau belum, centang semua minggu yang bisa
                            // » dipilih.
                            if (allSelected) selectedDates.clear() else unpaidDates.forEach { selectedDates[it] = true }
                        }) {
                            // » Teks tombol: "Batal Pilih Semua" atau "Pilih Semua (<jumlah>)", oranye, tebal, huruf kecil.
                            Text(if (allSelected) "Batal Pilih Semua" else "Pilih Semua (${unpaidDates.size})", color = OrangePrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    // » Untuk setiap minggu yang belum dibayar (date)...
                    unpaidDates.forEach { date ->
                        // » isChecked = apakah minggu ini sedang dicentang.
                        val isChecked = selectedDates[date] ?: false
                        // » ...satu baris:
                        Row(
                            // » Modifier:
                            modifier = Modifier
                                // » selebar dialog,
                                .fillMaxWidth()
                                // » sudut bulat 10dp,
                                .clip(RoundedCornerShape(10.dp))
                                // » latar oranye muda kalau dicentang, transparan kalau tidak,
                                .background(if (isChecked) OrangePrimary.copy(alpha = 0.08f) else Color.Transparent)
                                // » seluruh baris bisa diketuk untuk membalik centang,
                                .clickable { selectedDates[date] = !isChecked }
                                // » jarak dalam.
                                .padding(vertical = 2.dp, horizontal = 4.dp),
                            // » Rata tengah vertikal.
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // » Kotak centang (Checkbox):
                            Checkbox(
                                // » status = isChecked,
                                checked = isChecked,
                                // » saat diubah: simpan status baru,
                                onCheckedChange = { selectedDates[date] = it },
                                // » warna centang oranye.
                                colors = CheckboxDefaults.colors(checkedColor = OrangePrimary)
                            )
                            // » Teks label minggu, mengisi sisa lebar.
                            Text(date, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                            // » Teks nominal "Rp 5.000" kecil abu.
                            Text("Rp 5.000", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // » Kalau ada minggu dicentang, tampilkan rincian biaya.
                if (selectedCount > 0) {
                    // » Garis pemisah.
                    HorizontalDivider()
                    // » Card rincian:
                    Card(
                        // » selebar dialog,
                        modifier = Modifier.fillMaxWidth(),
                        // » sudut bulat 12dp,
                        shape = RoundedCornerShape(12.dp),
                        // » warna krem.
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                    ) {
                        // » Kolom isi, jarak dalam 12dp, jarak antar baris 4dp.
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            // » Baris Kas.
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                // » Teks "Kas (<n> minggu):" abu tua.
                                Text("Kas ($selectedCount minggu):", style = MaterialTheme.typography.bodySmall, color = TextMedium)
                                // » Nilai "Rp <kasAmount>" agak tebal.
                                Text("Rp ${formatRupiah(kasAmount)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            }
                            // » Kalau ada denda...
                            if (dendaAmount > 0) {
                                // » ...baris Denda.
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    // » Teks "Denda keterlambatan:" merah.
                                    Text("Denda keterlambatan:", style = MaterialTheme.typography.bodySmall, color = RedNegative)
                                    // » Nilai "Rp <dendaAmount>" merah agak tebal.
                                    Text("Rp ${formatRupiah(dendaAmount)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = RedNegative)
                                }
                            }
                            // » Garis pemisah.
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            // » Baris Total.
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                // » Teks "Total:" tebal.
                                Text("Total:", fontWeight = FontWeight.Bold)
                                // » Nilai "Rp <kas + denda>", sangat tebal, oranye.
                                Text("Rp ${formatRupiah(kasAmount + dendaAmount)}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold, color = OrangePrimary)
                            }
                        }
                    }
                }
            }
        },
        // » Tombol konfirmasi.
        confirmButton = {
            // » Button:
            Button(
                // » Saat ditekan:
                onClick = {
                    // » updated = salinan centang siswa itu yang boleh diubah.
                    val updated = memberRecords.toMutableMap()
                    // » Untuk tiap minggu yang dicentang...
                    selectedDates.forEach { (date, isSelected) ->
                        // » ...tandai lunas (true).
                        if (isSelected) updated[date] = true
                    }
                    // » Simpan centang baru siswa itu ke tabel salinan.
                    records[memberKey] = updated
                    // » Kirim tabel baru ke onConfirm (nanti disimpan ke repository).
                    onConfirm(PaymentsWrapper(dates = wrapper.dates, records = records))
                },
                // » Tombol aktif hanya kalau ada minggu dicentang.
                enabled = selectedCount > 0,
                // » Warna oranye.
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                // » Sudut bulat 10dp.
                shape = RoundedCornerShape(10.dp)
            ) {
                // » Ikon centang.
                Icon(Icons.Default.Check, contentDescription = null)
                // » Jarak 4dp.
                Spacer(modifier = Modifier.width(4.dp))
                // » Tulisan "Konfirmasi (<n> minggu)" tebal.
                Text("Konfirmasi ($selectedCount minggu)", fontWeight = FontWeight.Bold)
            }
        },
        // » Tombol batal.
        dismissButton = {
            // » Tombol Batal menutup dialog.
            TextButton(onClick = onDismiss) { Text("Batal", color = OrangePrimary) }
        }
    )
}

// » Judul bagian: DIALOG DETAIL MINGGU (admin), dengan kotak centang yang bisa diubah.
// ============================================================
// ADMIN WEEK DETAIL DIALOG – With editable checkboxes
// ============================================================

// Dialog daftar 33 siswa untuk satu minggu, dengan kotak centang yang bisa diubah. Tombol Simpan mengirim tabel baru ke repository.
// CARI-PEMBAYARAN | Edit centang lunas semua siswa untuk satu minggu.
// » Penanda fungsi penggambar tampilan.
@Composable
// » AdminWeekDetailDialog = dialog daftar 33 siswa untuk SATU minggu. Parameter:
fun AdminWeekDetailDialog(
    // » weekIndex = nomor minggu,
    weekIndex: Int,
    // » date = label minggu,
    date: String,
    // » members = daftar siswa,
    members: List<Member>,
    // » wrapper = tabel centang sekarang,
    wrapper: PaymentsWrapper,
    // » onDismiss = tutup,
    onDismiss: () -> Unit,
    // » onSave = simpan tabel baru.
    onSave: (PaymentsWrapper) -> Unit
) {
    // » records = salinan tabel centang yang boleh diubah di dialog ini.
    val records = remember(wrapper) {
        // » Wadah state peta.
        mutableStateMapOf<String, MutableMap<String, Boolean>>().apply {
            // » Salin tiap baris tabel asli.
            wrapper.records.forEach { (k, v) -> put(k, v.toMutableMap()) }
        }
    }

    // » paidCount = jumlah siswa yang lunas di minggu ini.
    val paidCount = members.count { member ->
        // » Cek centang siswa di minggu ini.
        records[member.id.toString()]?.get(date) == true
    }

    // » AlertDialog.
    AlertDialog(
        // » Tutup bila diketuk di luar.
        onDismissRequest = onDismiss,
        // » Sudut bulat 24dp.
        shape = RoundedCornerShape(24.dp),
        // » Judul dialog:
        title = {
            // » kolom judul.
            Column {
                // » Teks:
                Text(
                    // » "Minggu ke-<n>",
                    "Minggu ke-$weekIndex",
                    // » gaya sub-judul,
                    style = MaterialTheme.typography.titleMedium,
                    // » tebal,
                    fontWeight = FontWeight.Bold,
                    // » oranye.
                    color = OrangePrimary
                )
                // » Jarak kosong 2dp.
                Spacer(modifier = Modifier.height(2.dp))
                // » Teks tanggal minggu.
                Text(date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                // » Jarak kosong 4dp.
                Spacer(modifier = Modifier.height(4.dp))
                // » Lencana jumlah:
                Surface(
                    // » sudut bulat 20dp,
                    shape = RoundedCornerShape(20.dp),
                    // » latar oranye transparan 10%.
                    color = OrangePrimary.copy(alpha = 0.1f)
                ) {
                    // » Teks:
                    Text(
                        // » "<lunas> / <jumlah siswa> Sudah Bayar",
                        "$paidCount / ${members.size} Sudah Bayar",
                        // » huruf kecil,
                        style = MaterialTheme.typography.labelSmall,
                        // » oranye,
                        color = OrangePrimary,
                        // » tebal,
                        fontWeight = FontWeight.Bold,
                        // » jarak dalam lencana.
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        },
        // » Isi dialog: daftar siswa.
        text = {
            // » Kolom:
            Column(
                // » selebar dialog,
                modifier = Modifier
                    // » tinggi maksimal 400dp,
                    .fillMaxWidth()
                    // » bisa digulir,
                    .heightIn(max = 400.dp)
                    // » jarak antar baris 4dp.
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // » Untuk setiap siswa...
                members.forEach { member ->
                    // » memberKey = id siswa sebagai teks.
                    val memberKey = member.id.toString()
                    // » Catatan: jangan menulis state saat menggambar karena menimbulkan bug, hanya membaca.
                    // Jangan getOrPut di sini: menulis state saat composition menyebabkan bug
                    // » isPaid = apakah siswa lunas di minggu ini.
                    val isPaid = records[memberKey]?.get(date) == true
                    // » Catatan: simpan peta BARU agar sistem mendeteksi perubahan dan kotak centang ikut berubah.
                    // Simpan map BARU agar SnapshotStateMap mendeteksi perubahan & checkbox ikut berubah
                    // » setPaid = fungsi mengubah status lunas siswa ini:
                    val setPaid: (Boolean) -> Unit = { paid ->
                        // » ambil peta lama siswa (atau kosong), salin, ubah minggu ini jadi paid, simpan kembali.
                        records[memberKey] = (records[memberKey] ?: emptyMap()).toMutableMap().apply { put(date, paid) }
                    }

                    // » Satu baris siswa:
                    Row(
                        // » Modifier:
                        modifier = Modifier
                            // » selebar dialog,
                            .fillMaxWidth()
                            // » sudut bulat 10dp,
                            .clip(RoundedCornerShape(10.dp))
                            // » latar hijau muda kalau lunas, merah sangat muda kalau belum,
                            .background(if (isPaid) GreenPositive.copy(alpha = 0.08f) else RedNegative.copy(alpha = 0.04f))
                            // » bisa diketuk untuk membalik status,
                            .clickable { setPaid(!isPaid) }
                            // » jarak dalam.
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        // » Rata tengah vertikal.
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // » Kotak centang:
                        Checkbox(
                            // » status = isPaid,
                            checked = isPaid,
                            // » saat diubah: panggil setPaid,
                            onCheckedChange = { checked -> setPaid(checked) },
                            // » warna centang hijau,
                            colors = CheckboxDefaults.colors(checkedColor = GreenPositive),
                            // » ukuran 36dp.
                            modifier = Modifier.size(36.dp)
                        )
                        // » Foto siswa ukuran 30.
                        StudentAvatar(memberId = member.id, size = 30)
                        // » Jarak 8dp.
                        Spacer(modifier = Modifier.width(8.dp))
                        // » Teks nama siswa:
                        Text(
                            // » isinya nama,
                            member.name,
                            // » huruf kecil,
                            style = MaterialTheme.typography.bodySmall,
                            // » tebal sedang kalau lunas,
                            fontWeight = if (isPaid) FontWeight.SemiBold else FontWeight.Normal,
                            // » mengisi sisa lebar,
                            modifier = Modifier.weight(1f),
                            // » maksimal 1 baris,
                            maxLines = 1,
                            // » dipotong "..." bila panjang.
                            overflow = TextOverflow.Ellipsis
                        )
                        // » Lencana status:
                        Surface(
                            // » sudut bulat 20dp,
                            shape = RoundedCornerShape(20.dp),
                            // » warna hijau kalau lunas, merah transparan kalau belum.
                            color = if (isPaid) GreenPositive else RedNegative.copy(alpha = 0.2f)
                        ) {
                            // » Teks status:
                            Text(
                                // » "✓" kalau lunas, "✗" kalau belum,
                                if (isPaid) "✓" else "✗",
                                // » huruf kecil,
                                style = MaterialTheme.typography.labelSmall,
                                // » putih kalau lunas, merah kalau belum,
                                color = if (isPaid) Color.White else RedNegative,
                                // » tebal,
                                fontWeight = FontWeight.Bold,
                                // » jarak dalam.
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        },
        // » Tombol Simpan.
        confirmButton = {
            // » Button:
            Button(
                // » Saat ditekan:
                onClick = {
                    // » kirim tabel baru ke onSave (nanti disimpan ke repository).
                    onSave(PaymentsWrapper(dates = wrapper.dates, records = records))
                },
                // » Warna oranye.
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                // » Sudut bulat 10dp.
                shape = RoundedCornerShape(10.dp)
            ) {
                // » Ikon centang.
                Icon(Icons.Default.Check, contentDescription = null)
                // » Jarak 4dp.
                Spacer(modifier = Modifier.width(4.dp))
                // » Tulisan "Simpan" tebal.
                Text("Simpan", fontWeight = FontWeight.Bold)
            }
        },
        // » Tombol tutup.
        dismissButton = {
            // » Tombol "Tutup" menutup dialog tanpa menyimpan.
            TextButton(onClick = onDismiss) { Text("Tutup", color = OrangePrimary) }
        }
    )
}

// » Judul bagian: USER PAYMENTS SCREEN = tampilan kas untuk siswa (mendeteksi siswa yang sedang login).
// ============================================================
// USER PAYMENTS SCREEN – Auto-detect logged-in user
// ============================================================

// Tampilan siswa. currentMemberId = siswa yang login, jadi hanya datanya sendiri yang dipakai (records[memberKey]).
// Hitungan: dueDates = semua minggu dikurangi minggu dispensasi, paidCount = yang sudah dicentang, unpaidCount = sisanya.
// CARI-PEMBAYARAN CARI-DISPENSASI | Tampilan siswa: status kas pribadi per minggu (Lunas / Dispensasi / Belum Bayar).
// » Penanda fungsi penggambar tampilan.
@Composable
// » UserPaymentsScreen = tampilan siswa tab Kas: status kas pribadi dan riwayat tiap minggu.
fun UserPaymentsScreen(repository: AppCashRepository) {
    // » payments = tabel pembayaran seluruh siswa (dari situ diambil milik siswa ini).
    var payments by remember { mutableStateOf<PaymentsWrapper?>(null) }
    // » members = daftar siswa.
    var members by remember { mutableStateOf<List<Member>>(emptyList()) }
    // » dashboard = angka dashboard.
    var dashboard by remember { mutableStateOf<Dashboard?>(null) }
    // » currentMemberId = id siswa yang login.
    var currentMemberId by remember { mutableStateOf<Int?>(null) }
    // » error = pesan error.
    var error by remember { mutableStateOf<String?>(null) }
    // » loading = sedang memuat.
    var loading by remember { mutableStateOf(true) }
    // » showBniPayment = apakah dialog Bayar Kas tampil.
    var showBniPayment by remember { mutableStateOf(false) }
    // » scope = pengelola tugas belakang.
    val scope = rememberCoroutineScope()
    // » snackbar = pengelola pesan kecil.
    val snackbar = remember { SnackbarHostState() }

    // » dataVersion = nomor versi data.
    val dataVersion by repository.version.collectAsState()
    // » Blok dijalankan tiap dataVersion berubah:
    LaunchedEffect(dataVersion) {
        // » hapus error lama.
        error = null
        // » ambil id siswa yang login.
        currentMemberId = repository.getCurrentMemberId()
        // » ambil tabel pembayaran lalu:
        repository.getPayments().fold(
            // » berhasil: simpan,
            onSuccess = { payments = it },
            // » gagal: simpan pesan error.
            onFailure = { error = it.message ?: "Gagal memuat pembayaran" }
        )
        // » ambil daftar siswa lalu:
        repository.getMembers().fold(
            // » berhasil: simpan,
            onSuccess = { members = it },
            // » gagal: abaikan.
            onFailure = {}
        )
        // » ambil dashboard lalu:
        repository.getDashboard().fold(
            // » berhasil: simpan,
            onSuccess = { dashboard = it },
            // » gagal: abaikan.
            onFailure = {}
        )
        // » Selesai: loading = false.
        loading = false
    }

    // » currentMember = data siswa yang login.
    val currentMember = members.find { it.id == currentMemberId }
    // » memberKey = id siswa sebagai teks (kunci tabel).
    val memberKey = currentMemberId?.toString() ?: "0"
    // » memberRecords = centang milik siswa ini (kosong bila belum ada).
    val memberRecords = payments?.records?.get(memberKey) ?: emptyMap()
    // » allDates = daftar semua minggu (37).
    val allDates = payments?.dates ?: emptyList()
    // exemptDates = minggu dispensasi (BI/PKL) yang belum dibayar. Minggu ini TIDAK dihitung sebagai belum bayar dan tidak didenda.
    // » exemptDates = minggu dispensasi (BI/PKL) yang belum dibayar. Minggu ini TIDAK dihitung sebagai belum
    // » bayar dan tidak didenda.
    val exemptDates = allDates.filter { d -> currentMemberId?.let { repository.isExempt(it, d) } == true && memberRecords[d] != true }
    // » dueDates = minggu yang wajib bayar = semua minggu dikurangi minggu dispensasi.
    val dueDates = allDates - exemptDates.toSet()
    // » paidCount = jumlah minggu wajib yang sudah dicentang lunas.
    val paidCount = dueDates.count { memberRecords[it] == true }
    // » unpaidCount = jumlah minggu wajib yang belum dibayar.
    val unpaidCount = dueDates.size - paidCount
    // » unpaidDates = daftar minggu yang belum dibayar.
    val unpaidDates = dueDates.filter { memberRecords[it] != true }

    // Dialog pembayaran siswa. Setelah Konfirmasi, tabel centang disimpan dan muncul pesan "Pembayaran Kas Berhasil Dikonfirmasi".
    // Bayar Kas Dialog for user
    // » Kalau dialog Bayar diminta dan data sudah siap, tampilkan.
    if (showBniPayment && payments != null && currentMemberId != null) {
        // » UserBniPaymentDialog:
        UserBniPaymentDialog(
            // » tabel pembayaran,
            wrapper = payments!!,
            // » data siswa (kalau tidak ketemu pakai Member kosong "Unknown"),
            currentMember = currentMember ?: Member(0, "Unknown"),
            // » id siswa,
            currentMemberId = currentMemberId!!,
            // » repository (untuk denda dan dispensasi),
            repository = repository,
            // » onDismiss = tutup,
            onDismiss = { showBniPayment = false },
            // » onConfirm = dijalankan saat Konfirmasi, membawa tabel baru.
            onConfirm = { newWrapper ->
                // » Tutup dialog.
                showBniPayment = false
                // » Jalankan di jalur belakang:
                scope.launch {
                    // » simpan tabel baru lewat repository.savePayments lalu:
                    repository.savePayments(newWrapper).fold(
                        // » berhasil:
                        onSuccess = {
                            // » perbarui tabel di layar,
                            payments = newWrapper
                            // » tampilkan "✓ Pembayaran Kas Berhasil Dikonfirmasi!".
                            snackbar.showSnackbar("✓ Pembayaran Kas Berhasil Dikonfirmasi!")
                        },
                        // » gagal: tampilkan "Gagal memproses konfirmasi".
                        onFailure = { snackbar.showSnackbar("Gagal memproses konfirmasi") }
                    )
                }
            }
        )
    }

    // » Box penuh layar dengan latar sesuai tema.
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // » when = pilih satu keadaan:
        when {
            // » loading: lingkaran berputar.
            loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = OrangePrimary)
            // » error: ErrorView.
            error != null -> ErrorView(error = error!!, onRetry = { repository.refresh() })
            // » data siap:
            payments != null -> {
                // » LazyColumn = daftar yang bisa digulir:
                LazyColumn(
                    // » penuhi layar,
                    modifier = Modifier.fillMaxSize(),
                    // » jarak antar item 12dp.
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Page Header
                    // » item: header halaman.
                    item {
                        // » Kolom header.
                        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                            // » Teks kecil "Pembayaran Kas".
                            Text("Pembayaran Kas", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            // » Judul "Status Kas Saya".
                            Text("Status Kas Saya", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                        }
                    }

                    // Kartu nama siswa dan ringkasan "x/y Lunas" (atau tanda LUNAS bila semua beres).
                    // User Info Card
                    // » item: kartu info siswa.
                    item {
                        // » Box kartu oranye:
                        Box(
                            // » Modifier:
                            modifier = Modifier
                                // » selebar layar,
                                .fillMaxWidth()
                                // » jarak kiri-kanan 16dp,
                                .padding(horizontal = 16.dp)
                                // » kartu oranye 20dp,
                                .orangeCard(20.dp)
                                // » jarak dalam 16dp.
                                .padding(16.dp)
                        ) {
                            // » Baris foto + teks.
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // » Kalau id siswa ada...
                                if (currentMemberId != null) {
                                    // » ...tampilkan foto ukuran 56.
                                    StudentAvatar(memberId = currentMemberId!!, size = 56)
                                }
                                // » Jarak 12dp.
                                Spacer(modifier = Modifier.width(12.dp))
                                // » Kolom teks, mengisi sisa lebar.
                                Column(modifier = Modifier.weight(1f)) {
                                    // » Teks nama siswa (atau "Siswa"), tebal, putih.
                                    Text(currentMember?.name ?: "Siswa", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                    // » Teks NIS, kecil, putih agak transparan.
                                    Text(currentMember?.nis ?: "", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
                                    // » Jarak 8dp.
                                    Spacer(modifier = Modifier.height(8.dp))
                                    // » Baris dua lencana, jarak 8dp.
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        // » Lencana status: sudut bulat 20dp; hijau kalau tidak ada tunggakan, putih transparan kalau ada.
                                        Surface(shape = RoundedCornerShape(20.dp), color = if (unpaidCount == 0) GreenPositive else Color.White.copy(alpha = 0.25f)) {
                                            // » Teks lencana:
                                            Text(
                                                // » "✓ LUNAS" kalau semua beres, kalau tidak "<lunas>/<wajib> Lunas",
                                                if (unpaidCount == 0) "✓ LUNAS" else "$paidCount/${dueDates.size} Lunas",
                                                // » huruf kecil,
                                                style = MaterialTheme.typography.labelSmall,
                                                // » tebal,
                                                fontWeight = FontWeight.Bold,
                                                // » putih,
                                                color = Color.White,
                                                // » jarak dalam.
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                        // » Kalau ada minggu belum bayar...
                                        if (unpaidCount > 0) {
                                            // » ...lencana merah:
                                            Surface(shape = RoundedCornerShape(20.dp), color = RedNegative.copy(alpha = 0.9f)) {
                                                // » teks "<n> belum bayar",
                                                Text(
                                                    "$unpaidCount belum bayar",
                                                    // » huruf kecil,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    // » tebal,
                                                    fontWeight = FontWeight.Bold,
                                                    // » putih,
                                                    color = Color.White,
                                                    // » jarak dalam.
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Tombol "Bayar Kas (N minggu)" membuka dialog BNI/QRIS. Tidak tampil bila tidak ada minggu yang belum dibayar.
                    // Bayar Kas Button
                    // » Tombol "Bayar Kas" hanya muncul bila ada minggu yang belum dibayar.
                    if (unpaidCount > 0) {
                        // » item:
                        item {
                            // » Button:
                            Button(
                                // » saat ditekan: tampilkan dialog Bayar Kas (BNI/QRIS),
                                onClick = { showBniPayment = true },
                                // » Modifier:
                                modifier = Modifier
                                    // » selebar layar,
                                    .fillMaxWidth()
                                    // » jarak kiri-kanan 16dp,
                                    .padding(horizontal = 16.dp)
                                    // » tinggi 52dp.
                                    .height(52.dp),
                                // » Warna tombol ungu-biru (0xFF6366F1).
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                                // » Sudut bulat 14dp.
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                // » Ikon kirim putih.
                                Icon(Icons.Default.Send, contentDescription = null, tint = Color.White)
                                // » Jarak 8dp.
                                Spacer(modifier = Modifier.width(8.dp))
                                // » Tulisan "Bayar Kas (<n> minggu)" tebal putih.
                                Text("Bayar Kas ($unpaidCount minggu)", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    // Personal Payment List Header
                    // » item: judul daftar.
                    item {
                        // » Teks:
                        Text(
                            // » "Riwayat Pembayaran Per Minggu",
                            "Riwayat Pembayaran Per Minggu",
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

                    // Riwayat per minggu: "Minggu ke-N" dengan status Lunas (hijau), Dispensasi, atau Belum Bayar.
                    // Week-by-week status
                    // » itemsIndexed = satu baris untuk setiap minggu (idx = urutan, date = label).
                    itemsIndexed(allDates) { idx, date ->
                        // » isPaid = apakah minggu ini sudah lunas.
                        val isPaid = memberRecords[date] == true
                        // » isExempt = apakah minggu ini dispensasi.
                        val isExempt = date in exemptDates
                        // » Baris minggu:
                        Row(
                            // » Modifier:
                            modifier = Modifier
                                // » selebar layar,
                                .fillMaxWidth()
                                // » jarak kiri-kanan 16dp,
                                .padding(horizontal = 16.dp)
                                // » sudut bulat 12dp,
                                .clip(RoundedCornerShape(12.dp))
                                // » latar hijau muda kalau lunas, merah sangat muda kalau belum,
                                .background(if (isPaid) GreenPositive.copy(alpha = 0.08f) else RedNegative.copy(alpha = 0.05f))
                                // » jarak dalam 14dp.
                                .padding(14.dp),
                            // » Teks kiri dan lencana kanan berjauhan.
                            horizontalArrangement = Arrangement.SpaceBetween,
                            // » Rata tengah vertikal.
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // » Kolom teks kiri.
                            Column {
                                // » "Minggu ke-<nomor>" agak tebal.
                                Text("Minggu ke-${idx + 1}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                // » Label minggu kecil abu.
                                Text(date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            // » Lencana status kanan:
                            Surface(
                                // » sudut bulat 20dp,
                                shape = RoundedCornerShape(20.dp),
                                // » warna: hijau (lunas), biru muda (dispensasi), merah transparan (belum bayar).
                                color = when { isPaid -> GreenPositive; isExempt -> Color(0xFFE0E7FF); else -> RedNegative.copy(alpha = 0.15f) }
                            ) {
                                // » Teks lencana:
                                Text(
                                    // » "✓ Lunas", "Dispensasi", atau "Belum Bayar",
                                    when { isPaid -> "✓ Lunas"; isExempt -> "Dispensasi"; else -> "Belum Bayar" },
                                    // » huruf kecil,
                                    style = MaterialTheme.typography.labelSmall,
                                    // » warna teks: putih / biru tua / merah,
                                    color = when { isPaid -> Color.White; isExempt -> Color(0xFF3730A3); else -> RedNegative },
                                    // » tebal,
                                    fontWeight = FontWeight.Bold,
                                    // » jarak dalam.
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // » item penutup: jarak kosong 8dp.
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
            }
        }
        // » SnackbarHost = tempat pesan kecil muncul di tengah bawah.
        SnackbarHost(hostState = snackbar, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

// » Judul bagian: DIALOG BAYAR SISWA (BNI/QRIS).
// ============================================================
// USER BNI PAYMENT DIALOG – Pre-filled with logged-in user
// ============================================================

// DIALOG BAYAR SISWA. Dua tab: "Transfer BNI" (nomor rekening 1892077413 bisa disalin ke clipboard) dan "Scan QRIS" (gambar res/drawable/qris_payment).
// Langkah: pilih minggu -> bayar di luar aplikasi -> "1. Kirim Bukti ke WA Admin" (membuka WhatsApp admin dengan pesan terisi,
// isWaConfirmed jadi true) -> "2. Konfirmasi" (baru aktif bila isWaConfirmed dan ada minggu dipilih) -> minggu ditandai lunas.
// PERHATIAN: konfirmasi dilakukan siswa sendiri tanpa verifikasi admin. Ini batasan yang diketahui (lihat SRS 5.2).
// CARI-PEMBAYARAN CARI-QRIS CARI-WHATSAPP | Siswa membayar via BNI/QRIS, kirim bukti ke WA admin, lalu konfirmasi.
//   Nomor rekening BNI & nomor WA admin ditulis di dalam fungsi ini.
// » Mengizinkan fitur Material 3 eksperimental.
@OptIn(ExperimentalMaterial3Api::class)
// » Penanda fungsi penggambar tampilan.
@Composable
// » UserBniPaymentDialog = dialog Bayar Kas untuk siswa. Parameter:
fun UserBniPaymentDialog(
    // » wrapper = tabel pembayaran,
    wrapper: PaymentsWrapper,
    // » currentMember = data siswa,
    currentMember: Member,
    // » currentMemberId = id siswa,
    currentMemberId: Int,
    // » repository = untuk denda/dispensasi,
    repository: AppCashRepository,
    // » onDismiss = tutup,
    onDismiss: () -> Unit,
    // » onConfirm = simpan tabel baru.
    onConfirm: (PaymentsWrapper) -> Unit
) {
    // » context = pegangan aplikasi, dipakai untuk membuka WhatsApp dan menyalin ke clipboard.
    val context = LocalContext.current
    // » isWaConfirmed = apakah siswa sudah menekan "Kirim Bukti ke WA Admin". Tombol Konfirmasi baru aktif bila
    // » true.
    var isWaConfirmed by remember { mutableStateOf(false) }
    // » showQris = tab yang aktif: false = Transfer BNI, true = Scan QRIS.
    var showQris by remember { mutableStateOf(false) }

    // » records = salinan tabel yang boleh diubah.
    val records = remember(wrapper) {
        // » Wadah state peta.
        mutableStateMapOf<String, MutableMap<String, Boolean>>().apply {
            // » Salin tiap baris.
            wrapper.records.forEach { (k, v) -> put(k, v.toMutableMap()) }
        }
    }

    // » memberKey = id siswa sebagai teks.
    val memberKey = currentMemberId.toString()
    // » memberRecords = centang siswa ini.
    val memberRecords: Map<String, Boolean> = records[memberKey] ?: emptyMap()
    // » unpaidDates = minggu belum lunas dan bukan dispensasi (yang bisa dipilih).
    val unpaidDates = wrapper.dates.filter { date -> memberRecords[date] != true && !repository.isExempt(currentMemberId, date) }
    // » selectedDates = minggu yang dicentang siswa.
    val selectedDates = remember { mutableStateMapOf<String, Boolean>() }
    // » selectedCount = jumlah minggu dicentang.
    val selectedCount = selectedDates.count { it.value }
    // » baseKasAmount = uang kas = jumlah minggu x Rp5.000.
    val baseKasAmount = selectedCount * 5000

    // Denda dihitung dari minggu yang dicentang saja
    // » dendaAmount = denda hanya dari minggu yang dicentang.
    val dendaAmount = repository.dendaForWeeks(selectedDates.filterValues { it }.keys)
    // » totalAmount = total yang harus dibayar = kas + denda.
    val totalAmount = baseKasAmount + dendaAmount

    // » AlertDialog.
    AlertDialog(
        // » Tutup bila diketuk di luar.
        onDismissRequest = onDismiss,
        // » Sudut bulat 24dp.
        shape = RoundedCornerShape(24.dp),
        // » Judul dialog:
        title = {
            // » kolom judul.
            Column {
                // » Baris foto + teks.
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // » Foto siswa ukuran 40.
                    StudentAvatar(memberId = currentMemberId, size = 40)
                    // » Kolom teks.
                    Column {
                        // » Judul "Bayar Kas".
                        Text("Bayar Kas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = OrangePrimary)
                        // » Nama siswa kecil.
                        Text(currentMember.name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                // » Baris dua tab, jarak atas 12dp dan antar tab 8dp.
                Row(modifier = Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // » Tab 1: FilterChip (pilihan berbentuk pil).
                    FilterChip(
                        // » terpilih bila bukan QRIS,
                        selected = !showQris,
                        // » saat ditekan: pilih BNI,
                        onClick = { showQris = false },
                        // » label "Transfer BNI",
                        label = { Text("Transfer BNI", fontWeight = FontWeight.SemiBold) },
                        // » warna terpilih oranye dengan teks putih.
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = OrangePrimary, selectedLabelColor = Color.White)
                    )
                    // » Tab 2:
                    FilterChip(
                        // » terpilih bila QRIS aktif,
                        selected = showQris,
                        // » saat ditekan: pilih QRIS,
                        onClick = { showQris = true },
                        // » label "Scan QRIS",
                        label = { Text("Scan QRIS", fontWeight = FontWeight.SemiBold) },
                        // » warna terpilih oranye dengan teks putih.
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = OrangePrimary, selectedLabelColor = Color.White)
                    )
                }
            }
        },
        // » Isi dialog:
        text = {
            // » kolom jarak 12dp,
            Column(
                // » bisa digulir.
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                // » Isi sesuai tab: QRIS atau BNI.
                // BNI or QRIS content
                // » Kalau tab QRIS aktif...
                if (showQris) {
                    // » ...kartu QRIS:
                    Card(
                        // » selebar dialog,
                        modifier = Modifier.fillMaxWidth(),
                        // » sudut bulat 16dp,
                        shape = RoundedCornerShape(16.dp),
                        // » warna putih,
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        // » bayangan 4dp.
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        // » Kolom isi:
                        Column(
                            // » jarak dalam 12dp,
                            modifier = Modifier.padding(12.dp),
                            // » rata tengah,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            // » jarak antar baris 8dp.
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // » Teks petunjuk "Scan QR berikut untuk bayar kas:".
                            Text("Scan QR berikut untuk bayar kas:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                            // » Gambar QRIS:
                            Image(
                                // » diambil dari res/drawable/qris_payment,
                                painter = painterResource(id = R.drawable.qris_payment),
                                // » deskripsi aksesibilitas,
                                contentDescription = "QRIS Pembayaran Kas",
                                // » selebar kartu, sudut bulat 12dp,
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)),
                                // » skala gambar menyesuaikan lebar.
                                contentScale = ContentScale.FillWidth
                            )
                            // » Teks nama pemilik QRIS dan NMID:
                            Text(
                                // » "a.n. GEDE AGUS WIRA DARMA PUTRA, NMID: ID1026507245623",
                                "a.n. GEDE AGUS WIRA DARMA PUTRA\nNMID: ID1026507245623",
                                // » huruf kecil,
                                style = MaterialTheme.typography.labelSmall,
                                // » warna abu,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                // » rata tengah.
                                textAlign = TextAlign.Center
                            )
                        // » Tutup kolom isi kartu QRIS.
                        }
                    // » Tutup kartu QRIS.
                    }
                // » Kalau tab Transfer BNI yang aktif...
                } else {
                    // » ...kartu rekening:
                    Card(
                        // » selebar dialog,
                        modifier = Modifier.fillMaxWidth(),
                        // » sudut bulat 14dp,
                        shape = RoundedCornerShape(14.dp),
                        // » warna oranye transparan 8%.
                        colors = CardDefaults.cardColors(containerColor = OrangePrimary.copy(alpha = 0.08f))
                    ) {
                        // » Kolom isi: jarak dalam 14dp, jarak antar baris 4dp.
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            // » Teks petunjuk "Rekening Tujuan Transfer:".
                            Text("Rekening Tujuan Transfer:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            // » Baris: info rekening di kiri, tombol salin di kanan.
                            Row(
                                // » Selebar kartu.
                                modifier = Modifier.fillMaxWidth(),
                                // » Kiri dan kanan berjauhan.
                                horizontalArrangement = Arrangement.SpaceBetween,
                                // » Rata tengah vertikal.
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // » Kolom info rekening.
                                Column {
                                    // » Teks nama bank "Bank BNI" oranye tebal.
                                    Text("Bank BNI", fontWeight = FontWeight.Bold, color = OrangePrimary)
                                    // » Teks NOMOR REKENING "1892077413" besar dan tebal (ubah angka ini kalau rekening berganti; ada juga di
                                    // » baris pesan WhatsApp).
                                    Text("1892077413", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
                                    // » Teks "a.n. Kas XII PPLG".
                                    Text("a.n. Kas XII PPLG", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                // » Tombol ikon untuk menyalin nomor rekening.
                                IconButton(onClick = {
                                    // » Saat ditekan: ambil pengelola clipboard (papan salin) Android.
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    // » Salin nomor "1892077413" dengan nama "No Rekening BNI".
                                    clipboard.setPrimaryClip(ClipData.newPlainText("No Rekening BNI", "1892077413"))
                                    // » Tampilkan pesan singkat "Nomor rekening disalin!".
                                    Toast.makeText(context, "Nomor rekening disalin!", Toast.LENGTH_SHORT).show()
                                }) {
                                    // » Ikon bagikan oranye.
                                    Icon(Icons.Default.Share, contentDescription = null, tint = OrangePrimary)
                                }
                            }
                        }
                    }
                }

                // Unpaid week checkboxes
                // » Judul daftar minggu: "Pilih Minggu Kas (Rp 5.000 / minggu):".
                Text("Pilih Minggu Kas (Rp 5.000 / minggu):", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)

                // » Kalau tidak ada minggu yang belum dibayar...
                if (unpaidDates.isEmpty()) {
                    // » ...kotak hijau muda:
                    Surface(shape = RoundedCornerShape(10.dp), color = GreenPositive.copy(alpha = 0.1f)) {
                        // » baris isi,
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            // » ikon centang hijau,
                            Icon(Icons.Default.Check, contentDescription = null, tint = GreenPositive, modifier = Modifier.size(18.dp))
                            // » jarak 8dp,
                            Spacer(modifier = Modifier.width(8.dp))
                            // » teks "Semua minggu kas sudah LUNAS!" hijau tebal.
                            Text("Semua minggu kas sudah LUNAS!", style = MaterialTheme.typography.bodyMedium, color = GreenPositive, fontWeight = FontWeight.Bold)
                        }
                    }
                // » Kalau ada minggu belum dibayar...
                } else {
                    // » ...untuk setiap minggu belum dibayar (date):
                    unpaidDates.forEach { date ->
                        // » isChecked = apakah dicentang.
                        val isChecked = selectedDates[date] ?: false
                        // » Baris minggu:
                        Row(
                            // » Modifier:
                            modifier = Modifier
                                // » selebar dialog,
                                .fillMaxWidth()
                                // » sudut bulat 8dp,
                                .clip(RoundedCornerShape(8.dp))
                                // » latar oranye muda bila dicentang,
                                .background(if (isChecked) OrangePrimary.copy(alpha = 0.06f) else Color.Transparent)
                                // » seluruh baris bisa diketuk untuk membalik centang,
                                .clickable { selectedDates[date] = !isChecked }
                                // » jarak dalam.
                                .padding(vertical = 2.dp),
                            // » Rata tengah vertikal.
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // » Kotak centang:
                            Checkbox(
                                // » status = isChecked,
                                checked = isChecked,
                                // » saat diubah: simpan status baru,
                                onCheckedChange = { selectedDates[date] = it },
                                // » warna oranye.
                                colors = CheckboxDefaults.colors(checkedColor = OrangePrimary)
                            )
                            // » Teks label minggu, mengisi sisa lebar.
                            Text(date, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                            // » Teks "Rp 5.000" kecil abu.
                            Text("Rp 5.000", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // » Garis pemisah.
                HorizontalDivider()

                // Payment Breakdown
                // » Card rincian pembayaran:
                Card(
                    // » selebar dialog,
                    modifier = Modifier.fillMaxWidth(),
                    // » sudut bulat 12dp,
                    shape = RoundedCornerShape(12.dp),
                    // » warna oranye transparan 5%.
                    colors = CardDefaults.cardColors(containerColor = OrangePrimary.copy(alpha = 0.05f))
                ) {
                    // » Kolom isi, jarak dalam 12dp, jarak antar baris 4dp.
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        // » Baris Kas.
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            // » Teks "Kas (<n> minggu):".
                            Text("Kas ($selectedCount minggu):", style = MaterialTheme.typography.bodySmall, color = TextMedium)
                            // » Nilai "Rp <kas>".
                            Text("Rp ${formatRupiah(baseKasAmount)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                        }
                        // » Kalau ada denda...
                        if (dendaAmount > 0) {
                            // » ...baris Denda.
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                // » Teks "Denda Keterlambatan:" merah.
                                Text("Denda Keterlambatan:", style = MaterialTheme.typography.bodySmall, color = RedNegative, fontWeight = FontWeight.SemiBold)
                                // » Nilai "Rp <denda>" merah tebal.
                                Text("Rp ${formatRupiah(dendaAmount)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = RedNegative)
                            }
                        }
                        // » Garis pemisah.
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        // » Baris Total.
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            // » Teks "Total Transfer:" tebal.
                            Text("Total Transfer:", fontWeight = FontWeight.Bold)
                            // » Nilai "Rp <kas + denda>" besar oranye.
                            Text("Rp ${formatRupiah(totalAmount)}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold, color = OrangePrimary)
                        }
                    }
                }

                // » Tombol hijau WhatsApp: "1. Kirim Bukti ke WA Admin".
                Button(
                    // » Saat ditekan:
                    onClick = {
                        // » memberName = nama siswa untuk dicantumkan di pesan.
                        val memberName = currentMember.name
                        // » dendaText = tambahan "(termasuk denda Rp ...)" kalau ada denda, kalau tidak teks kosong.
                        val dendaText = if (dendaAmount > 0) " (termasuk denda Rp ${formatRupiah(dendaAmount)})" else ""
                        // » Penjelasan: isi pesan WhatsApp dibuat otomatis. Nomor WA admin: 6285600487433.
                        // Isi pesan WhatsApp otomatis: nama siswa, lewat BNI atau QRIS, nominal, dan denda bila ada. Nomor WA admin: 6285600487433.
                        // » message = isi pesan: nama siswa, cara bayar (QRIS atau BNI), nominal total, jumlah minggu, tambahan
                        // » denda, dan permintaan konfirmasi.
                        val message = "Halo Admin Kas XII PPLG, saya $memberName telah membayar iuran kas via ${if (showQris) "QRIS" else "BNI 1892077413"} sebesar Rp ${formatRupiah(totalAmount)} ($selectedCount minggu$dendaText). Mohon konfirmasi."
                        // » intent = niat membuka tautan api.whatsapp.com yang sudah berisi nomor admin dan pesan (Uri.encode
                        // » mengubah pesan agar aman di tautan).
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=6285600487433&text=${Uri.encode(message)}"))
                        // » Jalankan niat itu (buka WhatsApp). Kalau gagal...
                        try { context.startActivity(intent) } catch (e: Exception) {
                            // » ...tampilkan pesan singkat "Membuka WhatsApp Admin...".
                            Toast.makeText(context, "Membuka WhatsApp Admin...", Toast.LENGTH_SHORT).show()
                        }
                        // » Tandai bukti sudah dikirim (isWaConfirmed = true) sehingga tombol Konfirmasi terbuka.
                        isWaConfirmed = true
                    },
                    // » Selebar dialog.
                    modifier = Modifier.fillMaxWidth(),
                    // » Tombol aktif hanya kalau ada minggu yang dicentang.
                    enabled = selectedCount > 0,
                    // » Sudut bulat 12dp.
                    shape = RoundedCornerShape(12.dp),
                    // » Warna hijau WhatsApp (0xFF25D366).
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                ) {
                    // » Ikon kirim putih.
                    Icon(Icons.Default.Send, contentDescription = null, tint = Color.White)
                    // » Jarak 8dp.
                    Spacer(modifier = Modifier.width(8.dp))
                    // » Tulisan putih tebal.
                    Text("1. Kirim Bukti ke WA Admin", color = Color.White, fontWeight = FontWeight.Bold)
                }

                // » Kalau sudah menekan tombol WA...
                if (isWaConfirmed) {
                    // » ...tampilkan teks hijau "✓ Sudah kirim WA Admin. Klik 'Konfirmasi' di bawah.".
                    Text("✓ Sudah kirim WA Admin. Klik 'Konfirmasi' di bawah.", style = MaterialTheme.typography.bodySmall, color = Color(0xFF25D366))
                }
            }
        },
        // » Tombol konfirmasi di dialog.
        confirmButton = {
            // » Button "2. Konfirmasi".
            Button(
                // » Saat ditekan:
                onClick = {
                    // » updated = salinan centang siswa ini.
                    val updated = memberRecords.toMutableMap()
                    // » Untuk tiap minggu yang dicentang...
                    selectedDates.forEach { (date, isSelected) ->
                        // » ...tandai lunas (true).
                        if (isSelected) updated[date] = true
                    }
                    // » Simpan centang baru ke tabel salinan.
                    records[memberKey] = updated
                    // » Kirim tabel baru ke onConfirm (nanti disimpan ke repository sehingga saldo ikut naik).
                    onConfirm(PaymentsWrapper(dates = wrapper.dates, records = records))
                },
                // Tombol Konfirmasi terkunci sampai tombol "Kirim Bukti ke WA Admin" ditekan dulu.
                // » Tombol aktif hanya kalau bukti WA sudah dikirim DAN ada minggu dicentang.
                enabled = isWaConfirmed && selectedCount > 0,
                // » Warna oranye.
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                // » Sudut bulat 10dp.
                shape = RoundedCornerShape(10.dp)
            ) {
                // » Ikon centang.
                Icon(Icons.Default.Check, contentDescription = null)
                // » Jarak 4dp.
                Spacer(modifier = Modifier.width(4.dp))
                // » Tulisan "2. Konfirmasi" tebal.
                Text("2. Konfirmasi", fontWeight = FontWeight.Bold)
            }
        },
        // » Tombol batal.
        dismissButton = {
            // » Tombol "Batal" menutup dialog.
            TextButton(onClick = onDismiss) { Text("Batal", color = OrangePrimary) }
        }
    )
}

// » Judul bagian: KOMPONEN BERSAMA (dipakai beberapa layar).
// ============================================================
// SHARED COMPONENTS
// ============================================================

// Kartu satu minggu (dipakai daftar admin): oranye, "Minggu ke-N", tanggal, jumlah lunas dari total anggota, dan tombol Edit (admin).
// Kartu satu minggu kas (M01 sampai M37): jumlah siswa lunas dan tombol Edit.
// » Penanda fungsi penggambar tampilan.
@Composable
// » PaymentDateCard = kartu satu minggu kas. Parameter:
fun PaymentDateCard(
    // » weekIndex = nomor minggu (1-37),
    weekIndex: Int,
    // » date = label minggu,
    date: String,
    // » paidCount = jumlah siswa lunas,
    paidCount: Int,
    // » totalMembers = jumlah semua siswa,
    totalMembers: Int,
    // » isAdmin = apakah admin (tombol Edit tampil),
    isAdmin: Boolean,
    // » onClick = saat kartu diketuk,
    onClick: () -> Unit,
    // » onEdit = saat tombol Edit ditekan.
    onEdit: () -> Unit
) {
    // » Box kartu oranye:
    Box(
        // » Modifier:
        modifier = Modifier
            // » selebar layar,
            .fillMaxWidth()
            // » kartu oranye 16dp,
            .orangeCard(16.dp)
            // » seluruh kartu bisa diketuk,
            .clickable { onClick() }
            // » jarak dalam 16dp.
            .padding(16.dp)
    ) {
        // » Kolom isi kartu.
        Column {
            // » Baris dua sisi:
            Row(
                // » selebar kartu,
                modifier = Modifier.fillMaxWidth(),
                // » kiri dan kanan berjauhan,
                horizontalArrangement = Arrangement.SpaceBetween,
                // » rata atas.
                verticalAlignment = Alignment.Top
            ) {
                // » Kolom kiri.
                Column {
                    // » Teks "Minggu ke-<nomor>" putih tebal.
                    Text("Minggu ke-$weekIndex", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                    // » Jarak 4dp.
                    Spacer(modifier = Modifier.height(4.dp))
                    // » Lencana jumlah: sudut bulat 20dp, putih transparan 25%.
                    Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.25f)) {
                        // » Teks:
                        Text(
                            // » "<lunas>/<total> Anggota",
                            "$paidCount/$totalMembers Anggota",
                            // » huruf kecil,
                            style = MaterialTheme.typography.labelSmall,
                            // » putih,
                            color = Color.White,
                            // » tebal,
                            fontWeight = FontWeight.Bold,
                            // » jarak dalam lencana.
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
                // » Kolom kanan, rata kanan.
                Column(horizontalAlignment = Alignment.End) {
                    // » Teks label minggu putih agak transparan.
                    Text(date, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
                    // » Kalau admin...
                    if (isAdmin) {
                        // » ...tombol Edit (teks) tanpa jarak dalam.
                        TextButton(onClick = onEdit, contentPadding = PaddingValues(0.dp)) {
                            // » Teks "Edit" putih tebal.
                            Text("Edit", color = Color.White, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            // » Jarak 4dp.
                            Spacer(modifier = Modifier.width(4.dp))
                            // » Ikon centang kecil putih.
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }
}

// » Komentar dokumentasi cache gambar siswa (di bawah).
/**
 * Global LRU bitmap cache for student avatars.
 * Stores pre-decoded, downsampled ImageBitmap instances keyed by
 * "resId_targetPx" to avoid redundant BitmapFactory decoding cycles.
 * Limited to 20 entries (~200KB max at 200x200) to balance memory vs cache hits.
 */
// » avatarBitmapCache = "gudang sementara" (cache) gambar foto siswa yang sudah didekode. LinkedHashMap
// » dengan urutan akses supaya yang paling lama tidak dipakai dibuang duluan (LRU).
private val avatarBitmapCache = object : LinkedHashMap<String, androidx.compose.ui.graphics.ImageBitmap>(20, 0.75f, true) {
    // » Catatan: cache dibatasi 20 gambar supaya memori HP tidak penuh.
    // Cache gambar QRIS dibatasi 20 supaya memori HP tidak penuh.
    // » removeEldestEntry = dipanggil otomatis setiap ada gambar baru masuk; menentukan apakah gambar paling
    // » lama dibuang.
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, androidx.compose.ui.graphics.ImageBitmap>?): Boolean {
        // » Buang gambar tertua bila isi cache lebih dari 20.
        return size > 20
    }
}

// » Komentar dokumentasi fungsi di bawah: menghitung rasio pengecilan gambar.
/**
 * Calculates the optimal BitmapFactory inSampleSize power-of-2 factor.
 * Subsamples the image so the decoded bitmap's dimensions roughly match
 * the target display size, reducing memory allocation by up to 16x.
 *
 * @param outWidth  Original image width from BitmapFactory.Options.outWidth
 * @param outHeight Original image height from BitmapFactory.Options.outHeight
 * @param reqSize   Target display size in pixels
 * @return Power-of-2 sample size (1, 2, 4, 8, etc.)
 */
// » calculateInSampleSize = menghitung seberapa besar gambar foto dikecilkan saat dibuka (kelipatan 2) agar
// » hemat memori. Parameter: lebar asli, tinggi asli, ukuran tujuan (piksel).
private fun calculateInSampleSize(outWidth: Int, outHeight: Int, reqSize: Int): Int {
    // » inSampleSize = rasio pengecilan; mulai 1 (tidak dikecilkan).
    var inSampleSize = 1
    // » Kalau gambar asli lebih besar dari ukuran tujuan...
    if (outHeight > reqSize || outWidth > reqSize) {
        // » ...setengah tinggi,
        val halfHeight = outHeight / 2
        // » ...setengah lebar.
        val halfWidth = outWidth / 2
        // » Selama setelah dibagi rasio gambar masih >= ukuran tujuan...
        while ((halfHeight / inSampleSize) >= reqSize && (halfWidth / inSampleSize) >= reqSize) {
            // » ...gandakan rasio pengecilan.
            inSampleSize *= 2
        }
    }
    // » Kembalikan rasio pengecilan.
    return inSampleSize
}

// Foto bulat siswa dari file res/drawable/student_<id>. Gambar didecode kecil dan disimpan di cache supaya hemat memori.
// Bila file tidak ada atau rusak, tampil lingkaran dengan nomor id (tidak crash).
// CARI-ANGGOTA | Foto siswa dari res/drawable/student_<id>; bila tidak ada, tampil nomor id.
// » Komentar dokumentasi StudentAvatar (di bawah).
/**
 * Performance-optimized composable for rendering student profile photos.
 *
 * Optimization pipeline:
 * 1. Resolves drawable resource ID via cached `remember(memberId)`
 * 2. Uses BitmapFactory.Options.inJustDecodeBounds to read image dimensions
 *    WITHOUT allocating any pixel memory (zero-copy metadata read)
 * 3. Calculates power-of-2 inSampleSize to subsample the JPEG at decode time,
 *    reducing memory allocation proportional to (1/inSampleSize²)
 * 4. Decoded ImageBitmap is stored in a global LRU LinkedHashMap cache
 *    keyed by "resId_targetPx", eliminating redundant decode operations
 *    when the same avatar appears on multiple screens or recomposes
 * 5. Falls back to a styled initial-letter circle if no photo resource exists
 *
 * @param memberId Student member ID, mapped to R.drawable.student_{memberId}
 * @param size     Display size in dp (converted to px for sampling calculation)
 */
// » Penanda fungsi penggambar tampilan.
@Composable
// » StudentAvatar = menampilkan FOTO BULAT siswa dari file res/drawable/student_<id>. memberId = nomor
// » siswa, size = ukuran (dp).
fun StudentAvatar(memberId: Int, size: Int = 44) {
    // » context = pegangan aplikasi untuk membuka file gambar.
    val context = LocalContext.current
    // » density = kerapatan layar HP (untuk mengubah dp menjadi piksel).
    val density = LocalContext.current.resources.displayMetrics.density
    // » targetPx = ukuran tujuan dalam piksel.
    val targetPx = (size * density).toInt()

    // Resolve drawable resource ID once per memberId (cached across recompositions)
    // » resId = nomor internal gambar student_<id> (dicari sekali per siswa, lalu diingat).
    val resId = remember(memberId) {
        // » Nama file gambar yang dicari: "student_<id>".
        val resName = "student_$memberId"
        // » Cari nomor gambar di folder drawable; hasil 0 artinya file tidak ada.
        context.resources.getIdentifier(resName, "drawable", context.packageName)
    }

    // Decode and cache the bitmap with subsampling for the target display size.
    // Null jika file rusak / gagal didecode -> tampilkan fallback, bukan crash.
    // » imageBitmap = gambar siap tampil (diingat). Hasil null kalau file tidak ada atau rusak, nanti dipakai
    // » lingkaran cadangan.
    val imageBitmap = remember(resId, targetPx) {
        // » Kalau gambar tidak ada, hasil null.
        if (resId == 0) return@remember null
        // » cacheKey = kunci gudang: "<nomor gambar>_<ukuran>".
        val cacheKey = "${resId}_$targetPx"
        // » Ambil dari gudang; kalau belum ada, dekode dari file (runCatching = aman bila error):
        avatarBitmapCache[cacheKey] ?: runCatching {
                // » options = pengaturan pembukaan gambar:
                val options = android.graphics.BitmapFactory.Options().apply {
                    // Phase 1: Decode bounds only (no pixel allocation)
                    // » langkah 1: hanya baca ukuran gambar tanpa memakai memori piksel.
                    inJustDecodeBounds = true
                }
                // » Baca ukuran gambar.
                android.graphics.BitmapFactory.decodeResource(context.resources, resId, options)

                // Phase 2: Calculate optimal subsample ratio and decode pixels
                // » langkah 2: hitung rasio pengecilan...
                options.inSampleSize = calculateInSampleSize(options.outWidth, options.outHeight, targetPx)
                // » ...lalu matikan mode "hanya baca ukuran"...
                options.inJustDecodeBounds = false
                // » ...dan buka gambar dengan ukuran kecil, ubah ke ImageBitmap.
                android.graphics.BitmapFactory.decodeResource(context.resources, resId, options)?.asImageBitmap()
        // » Ambil hasilnya (null bila gagal), lalu simpan ke gudang.
        }.getOrNull()?.also { avatarBitmapCache[cacheKey] = it }
    }

    // » Kalau gambar berhasil dibuka...
    if (imageBitmap != null) {
        // » ...tampilkan:
        Image(
            // » bitmap gambar,
            bitmap = imageBitmap,
            // » deskripsi aksesibilitas "Foto Profil",
            contentDescription = "Foto Profil",
            // » ukuran sesuai size,
            modifier = Modifier
                .size(size.dp)
                // » dipotong bentuk lingkaran,
                .clip(CircleShape),
            // » skala Crop (penuhi lingkaran).
            contentScale = ContentScale.Crop
        )
    // » Kalau tidak ada gambar...
    } else {
        // » ...tampilkan lingkaran cadangan berisi nomor id:
        // Fallback: styled circle with member ID initial
        // » Box:
        Box(
            // » ukuran sesuai size,
            modifier = Modifier
                // » bentuk lingkaran,
                .size(size.dp)
                // » latar oranye transparan 20%,
                .clip(CircleShape)
                // » isi di tengah.
                .background(OrangePrimary.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            // » Teks nomor id:
            Text(
                // » isinya memberId,
                text = memberId.toString(),
                // » gaya label sedang,
                style = MaterialTheme.typography.labelMedium,
                // » sangat tebal,
                fontWeight = FontWeight.ExtraBold,
                // » oranye.
                color = OrangePrimary
            )
        }
    }
}

// Grafik batang kecil pemasukan dan pengeluaran. TIDAK DIPANGGIL dari layar mana pun (sisa rancangan). Grafik yang tampil adalah WeeklyKasLineChart di Dashboard.
// Grafik batang kecil pemasukan dan pengeluaran per bulan.
// » Penanda fungsi penggambar tampilan.
@Composable
// » MiniBarChart = grafik BATANG kecil. TIDAK dipanggil dari layar mana pun (sisa rancangan). Parameter:
fun MiniBarChart(
    // » incomes = pemasukan tiap bulan,
    incomes: List<Int>,
    // » expenses = pengeluaran tiap bulan,
    expenses: List<Int>,
    // » modifier = ukuran dari pemanggil.
    modifier: Modifier = Modifier
) {
    // » maxVal = nilai terbesar (minimal 1).
    val maxVal = maxOf(incomes.maxOrNull() ?: 0, expenses.maxOrNull() ?: 0, 1).toFloat()

    // » Canvas = kanvas untuk menggambar batang.
    Canvas(modifier = modifier) {
        // » Kalau kanvas tidak punya ukuran, berhenti.
        if (size.width <= 0f || size.height <= 0f) return@Canvas
        // » barCount = jumlah kelompok batang (jumlah bulan).
        val barCount = incomes.size.coerceAtLeast(1)
        // » barWidth = lebar satu batang, maksimal 12.
        val barWidth = (size.width / (barCount * 2.5f)).coerceAtMost(12f)
        // » spacing = jarak mendatar satu kelompok.
        val spacing = size.width / barCount

        // » Untuk setiap bulan (i = urutan, income = pemasukan):
        incomes.forEachIndexed { i, income ->
            // » xCenter = titik tengah kelompok batang bulan ini.
            val xCenter = spacing * i + (spacing / 2)
            // » expenseVal = pengeluaran bulan ini (0 bila tidak ada).
            val expenseVal = expenses.getOrElse(i) { 0 }

            // » incomeH = tinggi batang pemasukan (relatif terhadap nilai terbesar, minimal 4).
            val incomeH = ((income / maxVal) * size.height).coerceAtLeast(4f)
            // » expenseH = tinggi batang pengeluaran (0 bila pengeluaran 0).
            val expenseH = if (expenseVal > 0) ((expenseVal / maxVal) * size.height).coerceAtLeast(4f) else 0f

            // » Gambar batang pemasukan (kotak sudut bulat):
            drawRoundRect(
                // » warna hijau,
                color = androidx.compose.ui.graphics.Color(0xFF22C55E),
                // » posisi: kiri atas batang,
                topLeft = androidx.compose.ui.geometry.Offset(xCenter - barWidth - 1f, size.height - incomeH),
                // » ukuran: lebar x tinggi,
                size = androidx.compose.ui.geometry.Size(barWidth, incomeH),
                // » sudut bulat 3.
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f)
            )
            // » Kalau ada pengeluaran...
            if (expenseVal > 0) {
                // » ...gambar batang pengeluaran:
                drawRoundRect(
                    // » warna merah,
                    color = androidx.compose.ui.graphics.Color(0xFFEF4444),
                    // » posisi di sebelah batang hijau,
                    topLeft = androidx.compose.ui.geometry.Offset(xCenter + 1f, size.height - expenseH),
                    // » ukuran sesuai tinggi,
                    size = androidx.compose.ui.geometry.Size(barWidth, expenseH),
                    // » sudut bulat 3.
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f)
                )
            }
        }
    }
}
