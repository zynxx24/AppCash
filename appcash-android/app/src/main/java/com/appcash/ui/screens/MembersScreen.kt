// ==========================================================================
// PETA FILE · AppCash (Kelompok 6) · MembersScreen.kt
// Isi file : Halaman Anggota: daftar 33 siswa, cari, bio, edit (admin), hubungi WA.
// Penanda  : CARI-ANGGOTA, CARI-WHATSAPP
// Cara cari: VS Code Ctrl+Shift+F (Android Studio Ctrl+Shift+F) lalu ketik
//            "CARI-" untuk semua penanda, atau salah satu nama di baris Penanda.
// ==========================================================================
//  BACA INI DULU : TAB "ANGGOTA"
//  Daftar 33 siswa dengan kolom cari (nama, NIS, jabatan). Menekan satu siswa membuka dialog bio (MemberBioDialog)
//  berisi NIS, jabatan, umur, dan tombol "Hubungi WA" (nomor 08xx diubah jadi 628xx untuk wa.me).
//  Hanya ADMIN yang melihat tombol Edit di dialog bio (EditMemberDialog -> repository.updateMember).
// » Alamat folder (package) file ini di dalam proyek.
package com.appcash.ui.screens

// » orangeCard = dekorasi kartu oranye bergradien (Modifiers.kt).
import com.appcash.ui.theme.orangeCard
// » Intent = 'niat' membuka aplikasi lain, mis. membuka WhatsApp atau membagikan teks.
import android.content.Intent
// » Uri = alamat/tautan, mis. tautan WhatsApp wa.me.
import android.net.Uri
// » background = mengisi warna/gradien latar sebuah tampilan.
import androidx.compose.foundation.background
// » clickable = membuat tampilan bisa diketuk.
import androidx.compose.foundation.clickable
// » Layout dasar: Column (susun atas-bawah), Row (susun kiri-kanan), Box (tumpuk), Spacer (jarak kosong),
// » padding, size, fillMaxWidth, dll.
import androidx.compose.foundation.layout.*
// » verticalScroll = membuat isi bisa digulir naik-turun.
import androidx.compose.foundation.verticalScroll
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
// » Ikon bawaan Material: Edit (versi terisi).
import androidx.compose.material.icons.filled.Edit
// » Ikon bawaan Material: Phone (versi terisi).
import androidx.compose.material.icons.filled.Phone
// » Ikon bawaan Material: Search (versi terisi).
import androidx.compose.material.icons.filled.Search
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
// » Offset = titik koordinat (x, y) di Canvas.
import androidx.compose.ui.geometry.Offset
// » Brush = 'cat' warna, termasuk gradien (warna berubah halus).
import androidx.compose.ui.graphics.Brush
// » Color = tipe warna Compose; dipakai untuk menulis warna seperti Color.White atau Color(0xFF...).
import androidx.compose.ui.graphics.Color
// » LocalContext = mengambil Context aplikasi di dalam tampilan (untuk membuka WhatsApp, Toast, dll).
import androidx.compose.ui.platform.LocalContext
// » FontWeight = ketebalan huruf (Normal, Medium, Bold, ExtraBold).
import androidx.compose.ui.text.font.FontWeight
// » dp = satuan ukuran Android yang sama di semua layar (mis. 16.dp).
import androidx.compose.ui.unit.dp
// » Member = bentuk data satu siswa.
import com.appcash.data.model.Member
// » AppCashRepository = pusat data & rumus aplikasi (saldo, denda, pembayaran).
import com.appcash.data.repository.AppCashRepository
// » OrangeLight = oranye terang.
import com.appcash.ui.theme.OrangeLight
// » OrangePrimary = warna oranye utama.
import com.appcash.ui.theme.OrangePrimary
// » launch = menjalankan tugas di jalur belakang (coroutine) supaya layar tidak membeku.
import kotlinx.coroutines.launch

// State: members (daftar), searchQuery (kata cari), selectedMemberForBio (siswa yang bionya terbuka), editingMember (siswa yang sedang diedit admin).
// CARI-ANGGOTA | Daftar siswa + kolom cari nama/NIS/jabatan.
// » Penanda fungsi penggambar tampilan.
@Composable
// » MembersScreen = tab ANGGOTA (tab ke-4). repository = sumber data; isAdmin = true untuk admin (admin
// » melihat tombol Edit). Nilai awal isAdmin false.
fun MembersScreen(repository: AppCashRepository, isAdmin: Boolean = false) {
    // » members = daftar 33 siswa. Awalnya kosong.
    var members by remember { mutableStateOf<List<Member>>(emptyList()) }
    // » searchQuery = kata yang diketik di kolom cari.
    var searchQuery by remember { mutableStateOf("") }
    // » selectedMemberForBio = siswa yang sedang dibuka bionya (null = tidak ada dialog).
    var selectedMemberForBio by remember { mutableStateOf<Member?>(null) }
    // » editingMember = siswa yang sedang diedit admin (null = tidak ada).
    var editingMember by remember { mutableStateOf<Member?>(null) }
    // » error = pesan error; null = tidak ada.
    var error by remember { mutableStateOf<String?>(null) }
    // » loading = true selama data dimuat.
    var loading by remember { mutableStateOf(true) }
    // » scope = pengelola tugas belakang.
    val scope = rememberCoroutineScope()
    // » snackbar = pengelola pesan kecil di bawah layar.
    val snackbar = remember { SnackbarHostState() }

    // » dataVersion = nomor versi data; kalau berubah, daftar dimuat ulang.
    val dataVersion by repository.version.collectAsState()
    // » Blok dijalankan tiap dataVersion berubah:
    LaunchedEffect(dataVersion) {
        // » Hapus error lama.
        error = null
        // » Ambil daftar siswa lalu:
        repository.getMembers().fold(
            // » berhasil: simpan ke members,
            onSuccess = { members = it },
            // » gagal: simpan pesan error.
            onFailure = { error = it.message ?: "Gagal memuat anggota" }
        )
        // » Selesai: loading = false.
        loading = false
    }

    // » filteredMembers = daftar siswa setelah disaring sesuai kata cari. remember(members, searchQuery) =
    // » hitung ulang hanya bila daftar atau kata cari berubah.
    val filteredMembers = remember(members, searchQuery) {
        // » Kalau kata cari kosong, tampilkan semua siswa.
        if (searchQuery.isBlank()) members
        // » Kalau ada kata cari, saring siswa yang...
        else members.filter {
            // » ...namanya mengandung kata itu (tanpa peduli huruf besar/kecil), ATAU
            it.name.contains(searchQuery, ignoreCase = true) ||
            // » ...NIS-nya mengandung kata itu, ATAU
            it.nis.contains(searchQuery, ignoreCase = true) ||
            // » ...jabatannya mengandung kata itu.
            it.role.contains(searchQuery, ignoreCase = true)
        }
    }

    // » Kalau ada siswa terpilih, tampilkan dialog bio.
    if (selectedMemberForBio != null) {
        // » MemberBioDialog:
        MemberBioDialog(
            // » data siswa terpilih,
            member = selectedMemberForBio!!,
            // » mode admin (tombol Edit hanya untuk admin),
            isAdmin = isAdmin,
            // » onDismiss = tutup dialog,
            onDismiss = { selectedMemberForBio = null },
            // » onEdit = buka dialog edit untuk siswa itu dan tutup dialog bio.
            onEdit = { editingMember = it; selectedMemberForBio = null }
        )
    }

    // » Kalau ada siswa yang diedit, tampilkan dialog edit.
    if (editingMember != null) {
        // » EditMemberDialog:
        EditMemberDialog(
            // » data siswa,
            member = editingMember!!,
            // » onDismiss = tutup,
            onDismiss = { editingMember = null },
            // » onConfirm = dijalankan saat Simpan, membawa nama, NIS, jabatan, bio, telepon baru.
            onConfirm = { name, nis, role, bio, phone ->
                // » Ambil id siswa yang diedit.
                val mId = editingMember!!.id
                // » Tutup dialog.
                editingMember = null
                // » Jalankan di jalur belakang:
                scope.launch {
                    // » simpan lewat repository.updateMember.
                    val result = repository.updateMember(mId, name, nis, role, bio, phone)
                    // » Kalau berhasil...
                    if (result.isSuccess) {
                        // » ...ganti data siswa itu di daftar layar dengan nilai baru,
                        members = members.map { if (it.id == mId) it.copy(name = name, nis = nis, role = role, bio = bio, phone = phone) else it }
                        // » ...tampilkan "Anggota diperbarui".
                        snackbar.showSnackbar("Anggota diperbarui")
                    // » Kalau gagal...
                    } else {
                        // » ...tampilkan "Gagal memperbarui anggota".
                        snackbar.showSnackbar("Gagal memperbarui anggota")
                    }
                }
            }
        )
    }

    // » Box penuh layar dengan latar sesuai tema.
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // » when = pilih satu keadaan:
        when {
            // » masih loading: lingkaran berputar di tengah.
            loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = OrangePrimary)
            // » ada error: ErrorView dengan tombol coba lagi.
            error != null -> ErrorView(error = error!!, onRetry = { repository.refresh() })
            // » selain itu: tampilkan kolom isi layar.
            else -> Column(modifier = Modifier.fillMaxSize()) {
                // Page Header
                // » Kolom header halaman.
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                    // » Teks kecil "Dashboard".
                    Text("Dashboard", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    // » Judul "Anggota Kelas".
                    Text("Anggota Kelas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                }

                // Class Info Header Card (orange gradient)
                // » Box kartu info kelas (oranye).
                Box(
                    // » Modifier:
                    modifier = Modifier
                        // » selebar layar,
                        .fillMaxWidth()
                        // » jarak kiri-kanan 16dp,
                        .padding(horizontal = 16.dp)
                        // » kartu oranye 16dp,
                        .orangeCard(16.dp)
                        // » jarak dalam 16dp.
                        .padding(16.dp)
                ) {
                    // » Baris: info kelas di kiri, jumlah siswa di kanan.
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        // » Kolom teks kiri.
                        Column {
                            // » Teks kecil "KELAS".
                            Text("KELAS", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
                            // » Teks besar "XII PPLG".
                            Text("XII PPLG", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = Color.White)
                            // » Teks "Wali Kelas : Pak Restu".
                            Text("Wali Kelas : Pak Restu", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
                        }
                        // » Lencana jumlah siswa:
                        Surface(
                            // » sudut bulat 20dp,
                            shape = RoundedCornerShape(20.dp),
                            // » putih transparan 20%.
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            // » Teks:
                            Text(
                                // » "<jumlah> Siswa" dari ukuran daftar,
                                "${members.size} Siswa",
                                // » gaya label sedang,
                                style = MaterialTheme.typography.labelMedium,
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

                // » Jarak kosong 12dp.
                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar
                // » Kolom cari (OutlinedTextField).
                OutlinedTextField(
                    // » Nilainya searchQuery.
                    value = searchQuery,
                    // » Saat diketik: simpan ke searchQuery (daftar langsung tersaring).
                    onValueChange = { searchQuery = it },
                    // » Teks petunjuk "Cari Nama/NIS Siswa".
                    placeholder = { Text("Cari Nama/NIS Siswa") },
                    // » Ikon kaca pembesar di kiri.
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                    // » Selebar layar dengan jarak 16dp.
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    // » Sudut bulat 12dp.
                    shape = RoundedCornerShape(12.dp),
                    // » Warna garis tepi:
                    colors = OutlinedTextFieldDefaults.colors(
                        // » saat tidak fokus abu,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        // » saat fokus oranye.
                        focusedBorderColor = OrangePrimary
                    ),
                    // » Satu baris.
                    singleLine = true
                )

                // » Jarak kosong 12dp.
                Spacer(modifier = Modifier.height(12.dp))

                // Members List
                // » LazyColumn = daftar siswa yang bisa digulir.
                LazyColumn(
                    // » Mengisi sisa tinggi layar.
                    modifier = Modifier.weight(1f),
                    // » Jarak isi.
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    // » Jarak antar kartu 10dp.
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // » Kalau hasil saringan kosong, tampilkan teks:
                    if (filteredMembers.isEmpty()) item {
                        // » "Siswa "<kata>" tidak ditemukan",
                        Text(
                            "Siswa \"$searchQuery\" tidak ditemukan",
                            // » selebar layar dengan jarak 32dp,
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            // » warna abu,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            // » rata tengah.
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                    // » Untuk setiap siswa hasil saringan (kunci = id):
                    items(filteredMembers, key = { it.id }) { member ->
                        // » tampilkan kartu siswa: nomor urut = posisi di daftar asli + 1; saat diketuk buka bio.
                        MemberCard(member = member, index = members.indexOf(member) + 1, onClick = { selectedMemberForBio = member })
                    }
                    // » item penutup: jarak kosong 8dp.
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
            }
        }
        // » SnackbarHost = tempat pesan kecil di tengah bawah layar.
        SnackbarHost(hostState = snackbar, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

// Kartu satu anggota di daftar Anggota Kelas (inisial, nama, jabatan).
// » Penanda fungsi penggambar tampilan.
@Composable
// » MemberCard = kartu satu siswa. member = data, index = nomor urut, onClick = aksi saat kartu diketuk.
fun MemberCard(member: Member, index: Int, onClick: () -> Unit) {
    // » Box kartu oranye:
    Box(
        // » Modifier:
        modifier = Modifier
            // » selebar layar,
            .fillMaxWidth()
            // » kartu oranye 16dp,
            .orangeCard(16.dp)
            // » bisa diketuk (clickable),
            .clickable(onClick = onClick)
            // » jarak dalam 14dp.
            .padding(14.dp)
    ) {
        // » Baris: foto, teks, nomor.
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Profile Photo Avatar
            // » Kotak foto:
            Box(
                // » Modifier:
                modifier = Modifier
                    // » ukuran 50dp,
                    .size(50.dp)
                    // » bentuk lingkaran.
                    .clip(CircleShape),
                // » Isi di tengah.
                contentAlignment = Alignment.Center
            ) {
                // » Foto siswa (StudentAvatar) ukuran 50.
                StudentAvatar(memberId = member.id, size = 50)
            }
            // » Jarak mendatar 12dp.
            Spacer(modifier = Modifier.width(12.dp))
            // » Kolom teks, mengisi sisa lebar, jarak antar baris 2dp.
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                // » Teks NIS kecil.
                Text(member.nis, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
                // » Teks nama (besar, tebal, putih).
                Text(member.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color.White)
                // » Lencana jabatan: sudut bulat 6dp, putih transparan 25%.
                Surface(shape = RoundedCornerShape(6.dp), color = Color.White.copy(alpha = 0.25f)) {
                    // » Teks jabatan:
                    Text(
                        // » isinya jabatan,
                        member.role,
                        // » huruf kecil,
                        style = MaterialTheme.typography.labelSmall,
                        // » putih,
                        color = Color.White,
                        // » agak tebal,
                        fontWeight = FontWeight.SemiBold,
                        // » jarak dalam lencana.
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            // » Teks nomor urut di kanan:
            Text(
                // » "No. <index>",
                "No. $index",
                // » huruf kecil,
                style = MaterialTheme.typography.labelSmall,
                // » putih agak transparan,
                color = Color.White.copy(alpha = 0.8f),
                // » tebal.
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// CARI-ANGGOTA CARI-WHATSAPP | Detail siswa; tombol Hubungi WA mengubah nomor 08xx menjadi 628xx.
// » Penanda fungsi penggambar tampilan.
@Composable
// » MemberBioDialog = dialog detail siswa. Parameter: member, isAdmin, onDismiss (tutup), onEdit (buka form
// » edit).
fun MemberBioDialog(member: Member, isAdmin: Boolean, onDismiss: () -> Unit, onEdit: (Member) -> Unit) {
    // » context = pegangan aplikasi, dibutuhkan untuk membuka WhatsApp.
    val context = LocalContext.current
    // » AlertDialog = jendela pop-up.
    AlertDialog(
        // » Tutup bila diketuk di luar.
        onDismissRequest = onDismiss,
        // » Sudut bulat 24dp.
        shape = RoundedCornerShape(24.dp),
        // » Judul dialog:
        title = {
            // » baris foto + nama.
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // » Foto siswa ukuran 48.
                StudentAvatar(memberId = member.id, size = 48)
                // » Kolom nama dan jabatan.
                Column {
                    // » Nama (sangat tebal).
                    Text(member.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                    // » Jabatan (oranye).
                    Text(member.role, style = MaterialTheme.typography.bodySmall, color = OrangePrimary, fontWeight = FontWeight.SemiBold)
                }
            }
        },
        // » Isi dialog:
        text = {
            // » kolom, jarak antar blok 10dp.
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // » Box kotak oranye berisi data:
                Box(
                    // » Modifier:
                    modifier = Modifier
                        // » selebar dialog,
                        .fillMaxWidth()
                        // » kartu oranye 14dp,
                        .orangeCard(14.dp)
                        // » jarak dalam 14dp.
                        .padding(14.dp)
                ) {
                    // » Kolom baris data, jarak 6dp.
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        // » Baris NIS.
                        BioPill("NIS", member.nis)
                        // » Baris Jabatan Kelas.
                        BioPill("Jabatan Kelas", member.role)
                        // » Baris Umur.
                        BioPill("Umur", member.umur)
                        // » Baris Kontak WA ("-" bila nomor kosong).
                        BioPill("Kontak WA", member.phone.ifBlank { "-" })
                    }
                }
                // » Judul bio "Bio & Catatan Siswa :".
                Text("Bio & Catatan Siswa :", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = OrangePrimary)
                // » Isi bio ("Belum ada bio" bila kosong).
                Text(member.bio.ifBlank { "Belum ada bio" }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
            }
        },
        // » Tombol di kanan bawah dialog.
        confirmButton = {
            // » Baris tombol, jarak 8dp.
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // » Kalau admin...
                if (isAdmin) {
                    // » ...tombol Edit:
                    OutlinedButton(
                        // » saat ditekan: onEdit(member),
                        onClick = { onEdit(member) },
                        // » sudut bulat 10dp.
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        // » Ikon pensil oranye.
                        Icon(Icons.Default.Edit, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(16.dp))
                        // » Jarak 4dp.
                        Spacer(modifier = Modifier.width(4.dp))
                        // » Tulisan "Edit" oranye tebal.
                        Text("Edit", color = OrangePrimary, fontWeight = FontWeight.Bold)
                    }
                }
                // » Tombol Hubungi WA.
                Button(
                    // » Aktif hanya bila siswa punya nomor telepon.
                    enabled = member.phone.isNotBlank(),
                    // » Saat ditekan:
                    onClick = {
                        // » Penjelasan: nomor 0812xxx atau +62812xxx diubah menjadi 62812xxx supaya cocok dengan format wa.me.
                        // Normalisasi nomor: 0812xxx / +62812xxx -> 62812xxx
                        // » digits = nomor hanya angka (buang +, spasi, dll).
                        val digits = member.phone.filter { it.isDigit() }
                        // » waNumber = kalau diawali "0", ganti "0" dengan "62"; kalau tidak, pakai apa adanya.
                        val waNumber = if (digits.startsWith("0")) "62" + digits.drop(1) else digits
                        // » Buat niat (Intent) membuka tautan https://wa.me/<nomor> (WhatsApp).
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$waNumber"))
                        // » Jalankan (startActivity); kalau gagal (WhatsApp tidak ada) diabaikan.
                        try { context.startActivity(intent) } catch (e: Exception) {}
                    },
                    // » Warna oranye.
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                    // » Sudut bulat 10dp.
                    shape = RoundedCornerShape(10.dp)
                ) {
                    // » Ikon telepon putih.
                    Icon(Icons.Default.Phone, contentDescription = null, tint = Color.White)
                    // » Jarak 6dp.
                    Spacer(modifier = Modifier.width(6.dp))
                    // » Tulisan "Hubungi WA" putih tebal.
                    Text("Hubungi WA", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        },
        // » Tombol tutup.
        dismissButton = {
            // » TextButton "Tutup" menutup dialog.
            TextButton(onClick = onDismiss) { Text("Tutup", color = OrangePrimary) }
        }
    )
}

// Baris label dan nilai pada bio anggota.
// » Penanda fungsi penggambar tampilan.
@Composable
// » BioPill = satu baris label (kiri) dan nilai (kanan) di kotak bio.
fun BioPill(label: String, value: String) {
    // » Baris selebar kotak, label dan nilai berjauhan.
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        // » Teks label: huruf kecil, putih agak transparan.
        Text(label, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.75f))
        // » Teks nilai: huruf kecil, tebal, putih.
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

// Nama lain BioPill agar kode lama tetap jalan.
// » Penanda fungsi penggambar tampilan.
@Composable
// » BioRow = nama lain dari BioPill (dipertahankan agar kode lama tetap jalan).
fun BioRow(label: String, value: String) {
    // » Memanggil BioPill.
    BioPill(label, value)
}

// CARI-ANGGOTA | Form edit siswa (khusus admin).
// » Penanda fungsi penggambar tampilan.
@Composable
// » EditMemberDialog = form edit siswa (khusus admin). onConfirm membawa 5 teks: nama, NIS, jabatan, bio,
// » telepon.
fun EditMemberDialog(member: Member, onDismiss: () -> Unit, onConfirm: (String, String, String, String, String) -> Unit) {
    // » name = isi kolom nama (awal dari data lama).
    var name by remember { mutableStateOf(member.name) }
    // » nis = isi kolom NIS.
    var nis by remember { mutableStateOf(member.nis) }
    // » role = isi kolom jabatan.
    var role by remember { mutableStateOf(member.role) }
    // » bio = isi kolom bio.
    var bio by remember { mutableStateOf(member.bio) }
    // » phone = isi kolom telepon.
    var phone by remember { mutableStateOf(member.phone) }

    // » AlertDialog.
    AlertDialog(
        // » Tutup bila diketuk di luar.
        onDismissRequest = onDismiss,
        // » Sudut bulat 20dp.
        shape = RoundedCornerShape(20.dp),
        // » Judul dialog:
        title = {
            // » "Edit Anggota" tebal oranye.
            Text("Edit Anggota", fontWeight = FontWeight.Bold, color = OrangePrimary)
        },
        // » Isi dialog:
        text = {
            // » kolom isian:
            Column(
                // » bisa digulir kalau layar pendek,
                modifier = Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState()),
                // » jarak antar isian 10dp.
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // » Isian nama:
                OutlinedTextField(
                    // » nilai dan simpan saat diketik,
                    value = name, onValueChange = { name = it },
                    // » label "Nama", selebar dialog,
                    label = { Text("Nama") }, modifier = Modifier.fillMaxWidth(),
                    // » sudut bulat 12dp,
                    shape = RoundedCornerShape(12.dp),
                    // » warna oranye saat fokus.
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary, focusedLabelColor = OrangePrimary)
                )
                // » Isian NIS:
                OutlinedTextField(
                    // » nilai dan simpan saat diketik,
                    value = nis, onValueChange = { nis = it },
                    // » label "NIS", selebar dialog,
                    label = { Text("NIS") }, modifier = Modifier.fillMaxWidth(),
                    // » sudut bulat 12dp,
                    shape = RoundedCornerShape(12.dp),
                    // » warna oranye saat fokus.
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary, focusedLabelColor = OrangePrimary)
                )
                // » Isian jabatan:
                OutlinedTextField(
                    // » nilai dan simpan saat diketik,
                    value = role, onValueChange = { role = it },
                    // » label "Jabatan", selebar dialog,
                    label = { Text("Jabatan") }, modifier = Modifier.fillMaxWidth(),
                    // » sudut bulat 12dp,
                    shape = RoundedCornerShape(12.dp),
                    // » warna oranye saat fokus.
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary, focusedLabelColor = OrangePrimary)
                )
                // » Isian telepon:
                OutlinedTextField(
                    // » saat diketik hanya angka dan tanda + yang diterima,
                    value = phone, onValueChange = { phone = it.filter { c -> c.isDigit() || c == '+' } },
                    // » label "No. Telepon", selebar dialog, satu baris,
                    label = { Text("No. Telepon") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                    // » keyboard khusus telepon,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone),
                    // » sudut bulat 12dp,
                    shape = RoundedCornerShape(12.dp),
                    // » warna oranye saat fokus.
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary, focusedLabelColor = OrangePrimary)
                )
                // » Isian bio:
                OutlinedTextField(
                    // » nilai dan simpan saat diketik,
                    value = bio, onValueChange = { bio = it },
                    // » label "Bio", selebar dialog,
                    label = { Text("Bio") }, modifier = Modifier.fillMaxWidth(),
                    // » sudut bulat 12dp,
                    shape = RoundedCornerShape(12.dp),
                    // » minimal 2 baris,
                    minLines = 2,
                    // » warna oranye saat fokus.
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary, focusedLabelColor = OrangePrimary)
                )
            }
        },
        // » Tombol konfirmasi "Simpan".
        confirmButton = {
            Button(
                // » Saat ditekan:
                onClick = {
                    // » kalau nama dan NIS tidak kosong, panggil onConfirm dengan semua isian.
                    if (name.isNotBlank() && nis.isNotBlank()) onConfirm(name, nis, role, bio, phone)
                },
                // » Warna oranye.
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                // » Sudut bulat 10dp.
                shape = RoundedCornerShape(10.dp)
            // » Tulisan "Simpan" putih tebal.
            ) { Text("Simpan", color = Color.White, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            // » Tombol Batal menutup dialog.
            TextButton(onClick = onDismiss) { Text("Batal", color = OrangePrimary) }
        }
    )
}
