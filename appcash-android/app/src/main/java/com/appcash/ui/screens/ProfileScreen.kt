// ==========================================================================
// PETA FILE · AppCash (Kelompok 6) · ProfileScreen.kt
// Isi file : Halaman Profil: data diri, denda pribadi, edit profil, keluar.
// Penanda  : CARI-PROFIL, CARI-LOGIN
// Cara cari: VS Code Ctrl+Shift+F (Android Studio Ctrl+Shift+F) lalu ketik
//            "CARI-" untuk semua penanda, atau salah satu nama di baris Penanda.
// ==========================================================================
//  BACA INI DULU : TAB "PROFIL"
//  Susunan: header (foto, nama, label peran) -> kartu info diri -> kartu denda (hanya siswa yang punya denda) -> tombol Edit Profil dan Keluar -> versi aplikasi.
//  Admin: profil virtual (tidak bisa diedit nama/HP). Siswa: bisa ubah nama, no HP, bio (updateProfile).
//  Tombol Keluar membuka dialog konfirmasi, lalu repository.logout() dan kembali ke LoginScreen.
// » Alamat folder (package) file ini di dalam proyek.
package com.appcash.ui.screens

// » background = mengisi warna/gradien latar sebuah tampilan.
import androidx.compose.foundation.background
// » Layout dasar: Column (susun atas-bawah), Row (susun kiri-kanan), Box (tumpuk), Spacer (jarak kosong),
// » padding, size, fillMaxWidth, dll.
import androidx.compose.foundation.layout.*
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
// » Ikon keluar (tombol Keluar).
import androidx.compose.material.icons.automirrored.filled.ExitToApp
// » Ikon bawaan Material: Edit (versi terisi).
import androidx.compose.material.icons.filled.Edit
// » Ikon bawaan Material: Person (versi terisi).
import androidx.compose.material.icons.filled.Person
// » Ikon bawaan Material: Warning (versi terisi).
import androidx.compose.material.icons.filled.Warning
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
// » Color = tipe warna Compose; dipakai untuk menulis warna seperti Color.White atau Color(0xFF...).
import androidx.compose.ui.graphics.Color
// » FontWeight = ketebalan huruf (Normal, Medium, Bold, ExtraBold).
import androidx.compose.ui.text.font.FontWeight
// » dp = satuan ukuran Android yang sama di semua layar (mis. 16.dp).
import androidx.compose.ui.unit.dp
// » sp = satuan ukuran huruf Android (mengikuti pengaturan ukuran font di HP).
import androidx.compose.ui.unit.sp
// » DendaInfo = hasil hitung denda satu siswa.
import com.appcash.data.model.DendaInfo
// » UserProfile = bentuk data profil akun yang login.
import com.appcash.data.model.UserProfile
// » AppCashRepository = pusat data & rumus aplikasi (saldo, denda, pembayaran).
import com.appcash.data.repository.AppCashRepository
// » Semua warna & gaya dari folder theme.
import com.appcash.ui.theme.*
// » launch = menjalankan tugas di jalur belakang (coroutine) supaya layar tidak membeku.
import kotlinx.coroutines.launch

// CARI-PROFIL CARI-LOGIN | Menampilkan profil, kartu denda (siswa), tombol Edit Profil dan Logout.
// » Penanda fungsi penggambar tampilan.
@Composable
// » ProfileScreen = tab PROFIL (tab ke-5). Parameter:
fun ProfileScreen(
    // » repository = sumber data;
    repository: AppCashRepository,
    // » isAdmin = true kalau yang login admin;
    isAdmin: Boolean,
    // » onLogout = fungsi yang dijalankan saat tombol Keluar dikonfirmasi (kembali ke login).
    onLogout: () -> Unit
) {
    // » profile = data profil akun yang login. Awalnya kosong (null) sampai selesai dimuat.
    var profile by remember { mutableStateOf<UserProfile?>(null) }
    // » dendaInfo = data denda siswa (null = tidak punya denda).
    var dendaInfo by remember { mutableStateOf<DendaInfo?>(null) }
    // » showEditDialog = apakah dialog Edit Profil sedang tampil.
    var showEditDialog by remember { mutableStateOf(false) }
    // » showLogoutConfirm = apakah dialog konfirmasi Keluar sedang tampil.
    var showLogoutConfirm by remember { mutableStateOf(false) }
    // » loading = true selama data profil belum selesai dimuat.
    var loading by remember { mutableStateOf(true) }
    // » currentMemberId = id siswa yang login (null kalau admin).
    var currentMemberId by remember { mutableStateOf<Int?>(null) }
    // » scope = pengelola tugas belakang (dipakai saat logout dan simpan profil).
    val scope = rememberCoroutineScope()

    // » dataVersion = nomor versi data dari repository; kalau berubah (data diubah di tempat lain), layar ini
    // » memuat ulang.
    val dataVersion by repository.version.collectAsState()
    // » LaunchedEffect(dataVersion) = jalankan blok ini setiap kali dataVersion berubah (termasuk pertama kali
    // » layar muncul).
    LaunchedEffect(dataVersion) {
        // » Ambil profil akun yang login lalu tangani dua kemungkinan:
        repository.getCurrentUser().fold(
            // » berhasil: simpan ke profile dan matikan loading;
            onSuccess = { profile = it; loading = false },
            // » gagal: matikan loading saja.
            onFailure = { loading = false }
        )
        // » Ambil id siswa yang sedang login.
        currentMemberId = repository.getCurrentMemberId()
        // If user, get their denda
        // » Ambil peran akun yang login.
        val role = repository.getRole()
        // » Kalau yang login siswa dan id-nya ada...
        if (role == "user" && currentMemberId != null) {
            // » ...minta data denda siswa itu dan tangani hasilnya:
            repository.getDendaForMember(currentMemberId!!).fold(
                // » berhasil: simpan ke dendaInfo;
                onSuccess = { dendaInfo = it },
                // » gagal: abaikan.
                onFailure = { }
            )
        }
    }

    // » Column = susunan atas-bawah seluruh layar Profil.
    Column(
        // » Modifier:
        modifier = Modifier
            // » penuhi layar,
            .fillMaxSize()
            // » warna latar abu muda,
            .background(GreyBg)
            // » bisa digulir naik-turun.
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        // » Box = header oranye di bagian atas.
        Box(
            // » Modifier:
            modifier = Modifier
                // » selebar layar,
                .fillMaxWidth()
                // » warna latar oranye,
                .background(OrangePrimary)
                // » jarak atas 48dp dan bawah 32dp.
                .padding(top = 48.dp, bottom = 32.dp),
            // » Isi di tengah.
            contentAlignment = Alignment.Center
        ) {
            // » Kolom isi header, rata tengah.
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // » Kalau yang login siswa dan id-nya ada...
                if (!isAdmin && currentMemberId != null) {
                    // » ...tampilkan foto siswa (StudentAvatar) ukuran 80.
                    StudentAvatar(memberId = currentMemberId!!, size = 80)
                // » Kalau admin...
                } else {
                    // » ...gambar lingkaran putih:
                    Box(
                        // » Modifier:
                        modifier = Modifier
                            // » ukuran 80dp,
                            .size(80.dp)
                            // » bentuk lingkaran,
                            .clip(CircleShape)
                            // » warna putih.
                            .background(Color.White),
                        // » Ikon di tengah lingkaran.
                        contentAlignment = Alignment.Center
                    ) {
                        // » Ikon orang:
                        Icon(
                            // » gambar ikon Person,
                            Icons.Filled.Person,
                            // » deskripsi "Avatar",
                            contentDescription = "Avatar",
                            // » ukuran 48dp,
                            modifier = Modifier.size(48.dp),
                            // » warna oranye.
                            tint = OrangePrimary
                        )
                    }
                }
                // » Jarak kosong 12dp.
                Spacer(modifier = Modifier.height(12.dp))
                // » Teks nama:
                Text(
                    // » isinya nama profil, atau "Loading..." bila belum selesai dimuat.
                    text = profile?.name ?: "Loading...",
                    // » gaya judul besar,
                    style = MaterialTheme.typography.titleLarge,
                    // » tebal,
                    fontWeight = FontWeight.Bold,
                    // » putih.
                    color = Color.White
                )
                // » Jarak kosong 4dp.
                Spacer(modifier = Modifier.height(4.dp))
                // » Surface = lencana peran:
                Surface(
                    // » sudut bulat 12dp,
                    shape = RoundedCornerShape(12.dp),
                    // » warna putih transparan 20%.
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    // » Teks peran:
                    Text(
                        // » kalau admin tulis "👑 Admin", kalau siswa tulis jabatannya (mis. "Bendahara 1").
                        text = if (isAdmin) "👑 Admin" else profile?.role ?: "",
                        // » jarak dalam lencana,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        // » huruf kecil,
                        style = MaterialTheme.typography.bodySmall,
                        // » putih,
                        color = Color.White,
                        // » ketebalan sedang.
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // » Jarak kosong 16dp.
        Spacer(modifier = Modifier.height(16.dp))

        // Info Card
        // » Card = kartu putih "Informasi Profil".
        Card(
            // » Modifier:
            modifier = Modifier
                // » selebar layar,
                .fillMaxWidth()
                // » jarak kiri-kanan 16dp.
                .padding(horizontal = 16.dp),
            // » Sudut bulat 16dp.
            shape = RoundedCornerShape(16.dp),
            // » Bayangan 2dp.
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            // » Warna kartu putih.
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            // » Kolom isi kartu, jarak dalam 20dp.
            Column(modifier = Modifier.padding(20.dp)) {
                // » Teks judul kartu:
                Text(
                    // » tulisannya "Informasi Profil",
                    "Informasi Profil",
                    // » gaya sub-judul,
                    style = MaterialTheme.typography.titleMedium,
                    // » tebal,
                    fontWeight = FontWeight.Bold,
                    // » warna teks gelap.
                    color = TextDark
                )
                // » Jarak kosong 16dp.
                Spacer(modifier = Modifier.height(16.dp))

                // » Kalau admin...
                if (isAdmin) {
                    // » ...tampilkan baris Email (atau "-" bila kosong).
                    ProfileInfoRow("Email", profile?.email ?: "-")
                // » Kalau siswa...
                } else {
                    // » ...tampilkan baris NIS.
                    ProfileInfoRow("NIS", profile?.nis ?: "-")
                }
                // » Garis pemisah tipis antar baris.
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = GreyBorder)
                // » Baris Jabatan.
                ProfileInfoRow("Jabatan", profile?.role ?: "-")
                // » Garis pemisah.
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = GreyBorder)
                // » Baris Umur.
                ProfileInfoRow("Umur", profile?.umur ?: "-")
                // » Garis pemisah.
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = GreyBorder)
                // » Baris Telepon (tampil "-" bila kosong/spasi saja).
                ProfileInfoRow("Telepon", profile?.phone.takeIf { !it.isNullOrBlank() } ?: "-")
                // » Garis pemisah.
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = GreyBorder)
                // » Baris Bio (tampil "-" bila kosong).
                ProfileInfoRow("Bio", profile?.bio.takeIf { !it.isNullOrBlank() } ?: "-")
            }
        }

        // Denda Card (for users with outstanding fines)
        // » Kartu denda hanya untuk siswa yang punya denda (dendaInfo tidak kosong).
        if (!isAdmin && dendaInfo != null) {
            // » Jarak kosong 12dp.
            Spacer(modifier = Modifier.height(12.dp))
            // » Card kartu denda kuning muda.
            Card(
                // » Modifier:
                modifier = Modifier
                    // » selebar layar,
                    .fillMaxWidth()
                    // » jarak kiri-kanan 16dp.
                    .padding(horizontal = 16.dp),
                // » Sudut bulat 16dp.
                shape = RoundedCornerShape(16.dp),
                // » Bayangan 2dp.
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                // » Warna kartu krem kekuningan (0xFFFFF8E1).
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
            ) {
                // » Baris mendatar isi kartu.
                Row(
                    // » Jarak dalam 20dp.
                    modifier = Modifier.padding(20.dp),
                    // » Rata tengah vertikal.
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // » Ikon peringatan:
                    Icon(
                        // » ikon Warning,
                        Icons.Filled.Warning,
                        // » deskripsi "Denda",
                        contentDescription = "Denda",
                        // » warna oranye gelap,
                        tint = OrangeDark,
                        // » ukuran 32dp.
                        modifier = Modifier.size(32.dp)
                    )
                    // » Jarak mendatar 12dp.
                    Spacer(modifier = Modifier.width(12.dp))
                    // » Kolom teks, mengambil sisa lebar.
                    Column(modifier = Modifier.weight(1f)) {
                        // » Teks judul denda:
                        Text(
                            // » tulisannya "Denda Kas Tertunggak",
                            "Denda Kas Tertunggak",
                            // » gaya sub-judul kecil,
                            style = MaterialTheme.typography.titleSmall,
                            // » tebal,
                            fontWeight = FontWeight.Bold,
                            // » warna oranye gelap.
                            color = OrangeDark
                        )
                        // » Teks rincian:
                        Text(
                            // » "<n> bulan belum dibayar (<persen>%)" diambil dari dendaInfo.
                            "${dendaInfo!!.unpaidMonths} bulan belum dibayar (${dendaInfo!!.dendaPercentage.toInt()}%)",
                            // » huruf kecil,
                            style = MaterialTheme.typography.bodySmall,
                            // » warna abu tua.
                            color = TextMedium
                        )
                    }
                    // » Teks jumlah rupiah denda di kanan:
                    Text(
                        // » "Rp" lalu angka diformat titik ribuan (formatRupiah),
                        "Rp ${formatRupiah(dendaInfo!!.dendaAmount)}",
                        // » gaya sub-judul,
                        style = MaterialTheme.typography.titleMedium,
                        // » tebal,
                        fontWeight = FontWeight.Bold,
                        // » warna merah.
                        color = RedNegative
                    )
                }
            }
        }

        // » Jarak kosong 16dp.
        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons
        // » Card berisi tombol Edit Profil dan Keluar.
        Card(
            // » Modifier:
            modifier = Modifier
                // » selebar layar,
                .fillMaxWidth()
                // » jarak kiri-kanan 16dp.
                .padding(horizontal = 16.dp),
            // » Sudut bulat 16dp.
            shape = RoundedCornerShape(16.dp),
            // » Bayangan 2dp.
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            // » Warna putih.
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            // » Kolom isi kartu, jarak dalam 8dp.
            Column(modifier = Modifier.padding(8.dp)) {
                // Edit Profile (admin tidak punya data anggota, jadi hanya siswa yang bisa edit)
                // » Tombol Edit Profil, hanya untuk siswa (admin tidak punya data siswa untuk diedit).
                if (!isAdmin) TextButton(
                    // » Saat ditekan: tampilkan dialog edit (showEditDialog = true).
                    onClick = { showEditDialog = true },
                    // » Selebar kartu.
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // » Baris isi tombol.
                    Row(
                        // » Selebar tombol.
                        modifier = Modifier.fillMaxWidth(),
                        // » Rata tengah vertikal.
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // » Ikon pensil:
                        Icon(
                            // » ikon Edit,
                            Icons.Filled.Edit,
                            // » deskripsi "Edit",
                            contentDescription = "Edit",
                            // » warna oranye,
                            tint = OrangePrimary,
                            // » ukuran 24dp.
                            modifier = Modifier.size(24.dp)
                        )
                        // » Jarak mendatar 16dp.
                        Spacer(modifier = Modifier.width(16.dp))
                        // » Teks:
                        Text(
                            // » tulisannya "Edit Profil",
                            "Edit Profil",
                            // » gaya isi besar,
                            style = MaterialTheme.typography.bodyLarge,
                            // » warna teks gelap,
                            color = TextDark,
                            // » mengisi sisa lebar.
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // » Garis pemisah hanya dipasang untuk siswa (karena tombol Edit hanya ada di siswa).
                if (!isAdmin) HorizontalDivider(color = GreyBorder)

                // Logout
                // » Tombol Keluar (logout).
                TextButton(
                    // » Saat ditekan: tampilkan dialog konfirmasi keluar.
                    onClick = { showLogoutConfirm = true },
                    // » Selebar kartu.
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // » Baris isi tombol.
                    Row(
                        // » Selebar tombol.
                        modifier = Modifier.fillMaxWidth(),
                        // » Rata tengah vertikal.
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // » Ikon keluar:
                        Icon(
                            // » ikon ExitToApp,
                            Icons.AutoMirrored.Filled.ExitToApp,
                            // » deskripsi "Logout",
                            contentDescription = "Logout",
                            // » warna merah,
                            tint = RedNegative,
                            // » ukuran 24dp.
                            modifier = Modifier.size(24.dp)
                        )
                        // » Jarak mendatar 16dp.
                        Spacer(modifier = Modifier.width(16.dp))
                        // » Teks:
                        Text(
                            // » tulisannya "Keluar",
                            "Keluar",
                            // » gaya isi besar,
                            style = MaterialTheme.typography.bodyLarge,
                            // » warna merah,
                            color = RedNegative,
                            // » mengisi sisa lebar.
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // » Jarak kosong 32dp.
        Spacer(modifier = Modifier.height(32.dp))

        // App version
        // » Teks versi aplikasi di bawah:
        Text(
            // » "AppCash v<versi dari build.gradle> • XII PPLG" (VERSION_NAME otomatis dari pengaturan build, versi
            // » 3.3.1).
            "AppCash v${com.appcash.BuildConfig.VERSION_NAME} • XII PPLG",
            // » selebar layar,
            modifier = Modifier.fillMaxWidth(),
            // » huruf kecil,
            style = MaterialTheme.typography.bodySmall,
            // » warna abu muda,
            color = TextMuted,
            // » rata tengah.
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        // » Jarak kosong 16dp di bawah.
        Spacer(modifier = Modifier.height(16.dp))
    }

    // » Kalau showLogoutConfirm true, tampilkan dialog konfirmasi keluar.
    if (showLogoutConfirm) {
        // » AlertDialog = jendela pop-up pertanyaan.
        AlertDialog(
            // » Kalau diketuk di luar dialog, tutup dialog.
            onDismissRequest = { showLogoutConfirm = false },
            // » Sudut bulat 20dp.
            shape = RoundedCornerShape(20.dp),
            // » Judul dialog: "Keluar dari AppCash?" tebal oranye.
            title = { Text("Keluar dari AppCash?", fontWeight = FontWeight.Bold, color = OrangePrimary) },
            // » Isi dialog: "Kamu perlu login lagi untuk masuk."
            text = { Text("Kamu perlu login lagi untuk masuk.") },
            // » Tombol konfirmasi (kanan).
            confirmButton = {
                // » Button Keluar.
                Button(
                    // » Saat ditekan:
                    onClick = {
                        // » tutup dialog,
                        showLogoutConfirm = false
                        // » jalankan di jalur belakang:
                        scope.launch {
                            // » hapus sesi login di repository,
                            repository.logout()
                            // » panggil onLogout (AppRoot kembali ke layar login).
                            onLogout()
                        }
                    },
                    // » Warna tombol merah.
                    colors = ButtonDefaults.buttonColors(containerColor = RedNegative),
                    // » Sudut bulat 10dp.
                    shape = RoundedCornerShape(10.dp)
                // » Tulisan "Keluar" putih tebal.
                ) { Text("Keluar", color = Color.White, fontWeight = FontWeight.Bold) }
            },
            // » Tombol batal: menutup dialog, tulisan "Batal" oranye.
            dismissButton = { TextButton(onClick = { showLogoutConfirm = false }) { Text("Batal", color = OrangePrimary) } }
        )
    }

    // Edit Profile Dialog
    // » Kalau dialog edit diminta DAN profil sudah dimuat...
    if (showEditDialog && profile != null) {
        // » ...tampilkan EditProfileDialog:
        EditProfileDialog(
            // » bawa data profil,
            profile = profile!!,
            // » bawa status admin,
            isAdmin = isAdmin,
            // » onDismiss = tutup dialog,
            onDismiss = { showEditDialog = false },
            // » onSave = dijalankan saat Simpan ditekan membawa nama, telepon, bio baru.
            onSave = { name, phone, bio ->
                // » Jalankan di jalur belakang:
                scope.launch {
                    // » simpan lewat repository.updateProfile lalu tangani hasilnya:
                    repository.updateProfile(name, phone, bio).fold(
                        // » berhasil:
                        onSuccess = {
                            // Refresh profile
                            // » muat ulang profil agar tampilan terbaru,
                            repository.getCurrentUser().fold(
                                // » simpan ke profile,
                                onSuccess = { profile = it },
                                // » abaikan gagal,
                                onFailure = { }
                            )
                            // » tutup dialog.
                            showEditDialog = false
                        },
                        // » gagal: abaikan.
                        onFailure = { }
                    )
                }
            }
        )
    }
}

// Satu baris data diri di layar Profil.
// » Penanda fungsi tampilan (private = hanya dipakai di file ini).
@Composable
// » ProfileInfoRow = satu baris data diri: label di kiri, nilai di kanan.
private fun ProfileInfoRow(label: String, value: String) {
    // » Baris mendatar.
    Row(
        // » Selebar layar.
        modifier = Modifier.fillMaxWidth(),
        // » Label di kiri, nilai di kanan (SpaceBetween).
        horizontalArrangement = Arrangement.SpaceBetween,
        // » Rata tengah vertikal.
        verticalAlignment = Alignment.CenterVertically
    ) {
        // » Teks label:
        Text(
            // » isinya label (mis. "NIS"),
            label,
            // » gaya isi sedang,
            style = MaterialTheme.typography.bodyMedium,
            // » warna abu muda,
            color = TextMuted,
            // » mengambil 35% lebar.
            modifier = Modifier.weight(0.35f)
        )
        // » Teks nilai:
        Text(
            // » isinya nilai,
            value,
            // » gaya isi sedang,
            style = MaterialTheme.typography.bodyMedium,
            // » ketebalan sedang,
            fontWeight = FontWeight.Medium,
            // » warna gelap,
            color = TextDark,
            // » mengambil 65% lebar.
            modifier = Modifier.weight(0.65f)
        )
    }
}

// Dialog ubah nama, no. HP, dan bio, lalu disimpan lewat updateProfile.
// » Penanda fungsi tampilan.
@Composable
// » EditProfileDialog = dialog ubah profil. Parameter:
private fun EditProfileDialog(
    // » profile = data awal,
    profile: UserProfile,
    // » isAdmin = apakah admin,
    isAdmin: Boolean,
    // » onDismiss = fungsi tutup dialog,
    onDismiss: () -> Unit,
    // » onSave = fungsi simpan (nama, telepon, bio).
    onSave: (name: String, phone: String, bio: String) -> Unit
) {
    // » name = isi kolom nama (awal dari profil).
    var name by remember { mutableStateOf(profile.name) }
    // » phone = isi kolom telepon.
    var phone by remember { mutableStateOf(profile.phone) }
    // » bio = isi kolom bio.
    var bio by remember { mutableStateOf(profile.bio) }

    // » AlertDialog = jendela pop-up.
    AlertDialog(
        // » Tutup dialog bila diketuk di luar.
        onDismissRequest = onDismiss,
        // » Sudut bulat 20dp.
        shape = RoundedCornerShape(20.dp),
        // » Judul "Edit Profil".
        title = { Text("Edit Profil", fontWeight = FontWeight.Bold, color = OrangePrimary) },
        // » Isi dialog:
        text = {
            // » kolom susunan atas-bawah, jarak antar isian 12dp.
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // » Kolom Nama hanya untuk siswa...
                if (!isAdmin) {
                    // » ...isian nama:
                    OutlinedTextField(
                        // » nilainya name,
                        value = name,
                        // » saat diketik, simpan ke name,
                        onValueChange = { name = it },
                        // » label "Nama",
                        label = { Text("Nama") },
                        // » selebar dialog,
                        modifier = Modifier.fillMaxWidth(),
                        // » satu baris.
                        singleLine = true
                    )
                }
                // » Isian telepon:
                OutlinedTextField(
                    // » nilainya phone,
                    value = phone,
                    // » saat diketik, hanya angka dan tanda + yang diterima (filter),
                    onValueChange = { phone = it.filter { c -> c.isDigit() || c == '+' } },
                    // » label "No. Telepon",
                    label = { Text("No. Telepon") },
                    // » selebar dialog,
                    modifier = Modifier.fillMaxWidth(),
                    // » keyboard khusus nomor telepon,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone),
                    // » satu baris.
                    singleLine = true
                )
                // » Isian bio:
                OutlinedTextField(
                    // » nilainya bio,
                    value = bio,
                    // » saat diketik, simpan ke bio,
                    onValueChange = { bio = it },
                    // » label "Bio",
                    label = { Text("Bio") },
                    // » selebar dialog,
                    modifier = Modifier.fillMaxWidth(),
                    // » maksimal 3 baris.
                    maxLines = 3
                )
            }
        },
        // » Tombol konfirmasi (Simpan).
        confirmButton = {
            // » Button Simpan.
            Button(
                // » Saat ditekan: panggil onSave dengan nama, telepon, bio yang diisi.
                onClick = { onSave(name, phone, bio) },
                // » Tombol aktif hanya bila nama tidak kosong.
                enabled = name.isNotBlank(),
                // » Warna oranye.
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                // » Sudut bulat 10dp.
                shape = RoundedCornerShape(10.dp)
            ) {
                // » Tulisan "Simpan" putih tebal.
                Text("Simpan", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        // » Tombol batal.
        dismissButton = {
            // » TextButton Batal: menutup dialog (onDismiss).
            TextButton(onClick = onDismiss) {
                // » Tulisan "Batal" oranye.
                Text("Batal", color = OrangePrimary)
            }
        }
    )
}
