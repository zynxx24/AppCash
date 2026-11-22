// ==========================================================================
// PETA FILE · AppCash (Kelompok 6) · LoginScreen.kt
// Isi file : Halaman masuk: siswa pilih nama, admin pakai email & password.
// Penanda  : CARI-LOGIN
// Cara cari: VS Code Ctrl+Shift+F (Android Studio Ctrl+Shift+F) lalu ketik
//            "CARI-" untuk semua penanda, atau salah satu nama di baris Penanda.
// ==========================================================================
//  BACA INI DULU
//  Layar login dengan DUA mode (tombol bawah untuk berganti):
//    Mode Siswa (isEmailMode = false): pilih nama dari daftar 33 siswa (dropdown), tanpa password.
//    Mode Admin (isEmailMode = true) : email admin@gmail.com + password admin123 (dicek lewat hash SHA-256 di repository).
//  Tombol Masuk memanggil repository.login(...). Berhasil -> onLoginSuccess(role) membuat AppRoot pindah ke MainContent.
//  Gagal -> pesan error merah di bawah isian.
// » Alamat folder file ini (package). Folder "screens" = semua layar aplikasi.
package com.appcash.ui.screens

// » AnimatedVisibility = animasi muncul/hilang sebuah tampilan.
import androidx.compose.animation.AnimatedVisibility
// » Image = menampilkan gambar.
import androidx.compose.foundation.Image
// » Layout dasar (Column, Row, Box, Spacer, padding, size, dll).
import androidx.compose.foundation.layout.*
// » rememberScrollState = menyimpan posisi gulir layar.
import androidx.compose.foundation.rememberScrollState
// » verticalScroll = membuat tampilan bisa digulir naik-turun.
import androidx.compose.foundation.verticalScroll
// » painterResource = mengambil gambar dari folder res/drawable.
import androidx.compose.ui.res.painterResource
// » R = daftar semua file resource (gambar, teks) yang dibuat otomatis Android.
import com.appcash.R
// » orangeCard = dekorasi kartu oranye dari Modifiers.kt.
import com.appcash.ui.theme.orangeCard
// » Bentuk persegi bersudut bulat.
import androidx.compose.foundation.shape.RoundedCornerShape
// » KeyboardOptions = pengaturan jenis keyboard (email, angka, dll).
import androidx.compose.foundation.text.KeyboardOptions
// » Komponen Material 3 (Card, Button, OutlinedTextField, dst).
import androidx.compose.material3.*
// » Alat state Compose (remember, mutableStateOf, dst).
import androidx.compose.runtime.*
// » Alignment = pengatur posisi.
import androidx.compose.ui.Alignment
// » Modifier = daftar dekorasi tampilan.
import androidx.compose.ui.Modifier
// » Color = warna.
import androidx.compose.ui.graphics.Color
// » FontWeight = ketebalan huruf.
import androidx.compose.ui.text.font.FontWeight
// » KeyboardType = jenis keyboard (Email, Password, Number).
import androidx.compose.ui.text.input.KeyboardType
// » PasswordVisualTransformation = menyamarkan teks jadi titik-titik.
import androidx.compose.ui.text.input.PasswordVisualTransformation
// » VisualTransformation = cara menampilkan teks isian (None = apa adanya).
import androidx.compose.ui.text.input.VisualTransformation
// » dp = satuan ukuran.
import androidx.compose.ui.unit.dp
// » Member = bentuk data siswa.
import com.appcash.data.model.Member
// » AppCashRepository = sumber data (untuk login).
import com.appcash.data.repository.AppCashRepository
// » Warna oranye utama.
import com.appcash.ui.theme.OrangePrimary
// » launch = menjalankan tugas di jalur belakang.
import kotlinx.coroutines.launch

// CARI-LOGIN | Tombol bawah mengganti mode Siswa <-> Email & Password; ada tombol lihat password.
// » Mengizinkan fitur Material 3 yang masih eksperimental (ExposedDropdownMenu).
@OptIn(ExperimentalMaterial3Api::class)
// » Penanda bahwa fungsi di bawah menggambar tampilan.
@Composable
// » LoginScreen = layar login. repository = sumber data; onLoginSuccess = fungsi yang dipanggil kalau login
// » berhasil, membawa peran ("admin"/"user").
fun LoginScreen(repository: AppCashRepository, onLoginSuccess: (role: String) -> Unit) {
    // » isEmailMode = mode login. false = mode SISWA (pilih nama), true = mode ADMIN (email + password). Awalnya
    // » false.
    var isEmailMode by remember { mutableStateOf(false) }
    // » email = teks email yang diketik admin.
    var email by remember { mutableStateOf("") }
    // » password = teks password yang diketik admin.
    var password by remember { mutableStateOf("") }
    // » showPassword = apakah password ditampilkan (true) atau disamarkan (false).
    var showPassword by remember { mutableStateOf(false) }
    // » memberId = id siswa yang dipilih dari dropdown, disimpan sebagai teks. Awalnya kosong.
    var memberId by remember { mutableStateOf("") }
    // » members = daftar 33 siswa untuk dropdown. Awalnya kosong, diisi di LaunchedEffect.
    var members by remember { mutableStateOf<List<Member>>(emptyList()) }
    // » error = pesan error merah; null = tidak ada error.
    var error by remember { mutableStateOf<String?>(null) }
    // » loading = true saat proses login berjalan (tombol berubah jadi lingkaran berputar).
    var loading by remember { mutableStateOf(false) }
    // » scope = pengelola tugas belakang untuk memanggil repository.login.
    val scope = rememberCoroutineScope()

    // » Dijalankan SEKALI saat layar muncul: mengambil daftar siswa.
    LaunchedEffect(Unit) {
        // » Minta daftar siswa dari repository, lalu tangani dua kemungkinan (fold):
        repository.getMembers().fold(
            // » berhasil: simpan daftarnya ke members.
            onSuccess = { members = it },
            // » gagal: tidak melakukan apa-apa.
            onFailure = { }
        )
    }

    // » Box = wadah tumpuk; membungkus seluruh layar login.
    Box(
        // » Modifier:
        modifier = Modifier
            // » penuhi layar,
            .fillMaxSize()
            // » naik otomatis saat keyboard muncul (imePadding) agar isian tidak tertutup,
            .imePadding()
            // » bisa digulir kalau layar pendek,
            .verticalScroll(rememberScrollState())
            // » beri jarak 20dp dari tepi.
            .padding(20.dp),
        // » Isi di tengah layar.
        contentAlignment = Alignment.Center
    ) {
        // » Card = kartu putih yang berisi form login.
        Card(
            // » Modifier:
            modifier = Modifier
                // » selebar layar,
                .fillMaxWidth()
                // » jarak kiri-kanan 8dp.
                .padding(horizontal = 8.dp),
            // » Sudut bulat 24dp.
            shape = RoundedCornerShape(24.dp),
            // » Warna kartu = warna surface tema (putih).
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            // » Bayangan 6dp.
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            // » Column = susunan atas-bawah isi kartu.
            Column(
                // » Jarak dalam 24dp.
                modifier = Modifier.padding(24.dp),
                // » Jarak antar elemen 16dp.
                verticalArrangement = Arrangement.spacedBy(16.dp),
                // » Semua isi rata tengah.
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // App Logo / Title
                // » Bagian logo: kotak oranye.
                Box(
                    // » Modifier:
                    modifier = Modifier
                        // » ukuran 64dp,
                        .size(64.dp)
                        // » kartu oranye bersudut 18dp.
                        .orangeCard(18.dp),
                    // » Logo di tengah kotak.
                    contentAlignment = Alignment.Center
                ) {
                    // » Image = menampilkan gambar logo.
                    Image(
                        // » Ambil gambar ic_app_logo dari res/drawable.
                        painter = painterResource(R.drawable.ic_app_logo),
                        // » Deskripsi untuk pembaca layar.
                        contentDescription = "Logo AppCash",
                        // » Ukuran gambar 96dp (lebih besar dari kotak; dipotong sudut kartu).
                        modifier = Modifier.size(96.dp)
                    )
                }

                // » Column judul dan sub-judul, rata tengah.
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // » Teks judul:
                    Text(
                        // » tulisannya "AppCash XII PPLG".
                        "AppCash XII PPLG",
                        // » Gaya huruf judul besar.
                        style = MaterialTheme.typography.titleLarge,
                        // » Tebal.
                        fontWeight = FontWeight.Bold,
                        // » Warna teks utama.
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    // » Teks sub-judul:
                    Text(
                        // » tulisannya "Sistem Pengelolaan Kas Kelas".
                        "Sistem Pengelolaan Kas Kelas",
                        // » Gaya huruf isi sedang.
                        style = MaterialTheme.typography.bodyMedium,
                        // » Warna abu tua.
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // » Spacer = jarak kosong setinggi 4dp.
                Spacer(modifier = Modifier.height(4.dp))

                // Unified Login Inputs
                // » Kalau mode SISWA (bukan email)...
                if (!isEmailMode) {
                    // Member Selection Dropdown
                    // » expanded = apakah daftar dropdown terbuka. Awalnya tertutup.
                    var expanded by remember { mutableStateOf(false) }
                    // » selectedMember = siswa yang sedang dipilih (cari di daftar yang id-nya sama dengan memberId); null kalau
                    // » belum ada.
                    val selectedMember = members.find { it.id == memberId.toIntOrNull() }

                    // » ExposedDropdownMenuBox = kotak dropdown pilihan nama siswa.
                    ExposedDropdownMenuBox(
                        // » Status terbuka/tutup.
                        expanded = expanded,
                        // » Saat diketuk, balik status buka <-> tutup.
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        // » Kolom isian yang menampilkan pilihan sekarang.
                        OutlinedTextField(
                            // » Teks yang tampil: "id - nama" siswa terpilih, atau "Pilih Anggota" kalau belum memilih.
                            value = selectedMember?.let { "${it.id} - ${it.name}" } ?: "Pilih Anggota",
                            // » Tidak menerima ketikan (kosong).
                            onValueChange = {},
                            // » Hanya baca; pilihan diubah lewat daftar saja.
                            readOnly = true,
                            // » Label kecil "Pilih Nama Siswa".
                            label = { Text("Pilih Nama Siswa") },
                            // » Ikon panah dropdown di kanan.
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            // » Modifier:
                            modifier = Modifier
                                // » selebar kartu,
                                .fillMaxWidth()
                                // » menjadi jangkar tempat daftar muncul.
                                .menuAnchor(),
                            // » Sudut bulat 14dp.
                            shape = RoundedCornerShape(14.dp),
                            // » Warna isian:
                            colors = OutlinedTextFieldDefaults.colors(
                                // » garis tepi oranye saat fokus,
                                focusedBorderColor = OrangePrimary,
                                // » label oranye saat fokus.
                                focusedLabelColor = OrangePrimary
                            )
                        )
                        // » ExposedDropdownMenu = daftar pilihan yang muncul di bawah isian.
                        ExposedDropdownMenu(
                            // » Terbuka/tutup mengikuti expanded.
                            expanded = expanded,
                            // » Kalau diketuk di luar daftar, tutup.
                            onDismissRequest = { expanded = false }
                        ) {
                            // » Untuk tiap siswa...
                            members.forEach { member ->
                                // » ...buat satu baris pilihan.
                                DropdownMenuItem(
                                    // » Teks pilihan: "id - nama".
                                    text = { Text("${member.id} - ${member.name}") },
                                    // » Saat dipilih:
                                    onClick = {
                                        // » simpan id siswa ke memberId,
                                        memberId = member.id.toString()
                                        // » tutup daftar,
                                        expanded = false
                                        // » hapus pesan error lama.
                                        error = null
                                    }
                                )
                            }
                        }
                    }
                // » Kalau mode ADMIN (email & password)...
                } else {
                    // Email & Password Fields
                    // » Kolom isian email.
                    OutlinedTextField(
                        // » Nilainya = email.
                        value = email,
                        // » Saat diketik, simpan teks (dirapikan trim) dan hapus error lama.
                        onValueChange = { email = it.trim(); error = null },
                        // » Label "Email".
                        label = { Text("Email") },
                        // » Keyboard bertipe email (ada tombol @).
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        // » Selebar kartu.
                        modifier = Modifier.fillMaxWidth(),
                        // » Satu baris saja.
                        singleLine = true,
                        // » Sudut bulat 14dp.
                        shape = RoundedCornerShape(14.dp),
                        // » Warna isian:
                        colors = OutlinedTextFieldDefaults.colors(
                            // » garis tepi oranye saat fokus,
                            focusedBorderColor = OrangePrimary,
                            // » label oranye saat fokus.
                            focusedLabelColor = OrangePrimary
                        )
                    )
                    // » Kolom isian password.
                    OutlinedTextField(
                        // » Nilainya = password.
                        value = password,
                        // » Saat diketik, simpan teks dan hapus error lama.
                        onValueChange = { password = it; error = null },
                        // » Label "Password".
                        label = { Text("Password") },
                        // » Kalau showPassword true tampil apa adanya, kalau false disamarkan jadi titik-titik.
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        // » Ikon di kanan isian: tombol teks untuk lihat/sembunyikan.
                        trailingIcon = {
                            // » Saat ditekan, ubah showPassword (true <-> false).
                            TextButton(onClick = { showPassword = !showPassword }) {
                                // » Tulisan tombol: "Sembunyikan" kalau sedang tampil, "Lihat" kalau tersamar.
                                Text(if (showPassword) "Sembunyikan" else "Lihat", color = OrangePrimary, style = MaterialTheme.typography.labelSmall)
                            }
                        },
                        // » Keyboard bertipe password.
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        // » Selebar kartu.
                        modifier = Modifier.fillMaxWidth(),
                        // » Satu baris saja.
                        singleLine = true,
                        // » Sudut bulat 14dp.
                        shape = RoundedCornerShape(14.dp),
                        // » Warna isian:
                        colors = OutlinedTextFieldDefaults.colors(
                            // » garis tepi oranye saat fokus,
                            focusedBorderColor = OrangePrimary,
                            // » label oranye saat fokus.
                            focusedLabelColor = OrangePrimary
                        )
                    )
                }

                // » Kalau ada pesan error...
                if (error != null) {
                    // » ...tampilkan teks:
                    Text(
                        // » isinya pesan error ("!!" = pasti tidak kosong di sini).
                        text = error!!,
                        // » Warna merah error.
                        color = MaterialTheme.colorScheme.error,
                        // » Huruf kecil.
                        style = MaterialTheme.typography.bodySmall,
                        // » Agak tebal.
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Submit Button
                // » Button = tombol "Masuk".
                Button(
                    // » Saat ditekan:
                    onClick = {
                        // » hapus error lama,
                        error = null
                        // » tandai sedang loading,
                        loading = true
                        // » jalankan proses login di jalur belakang.
                        scope.launch {
                            // » Kalau mode admin...
                            if (isEmailMode) {
                                // » ...panggil login dengan:
                                val result = repository.login(
                                    // » peran "admin",
                                    role = "admin",
                                    // » email yang diketik,
                                    email = email,
                                    // » password yang diketik.
                                    password = password
                                )
                                // » Kalau berhasil...
                                if (result.isSuccess) {
                                    // » ...beri tahu AppRoot (login sukses sebagai admin) sehingga pindah ke halaman utama.
                                    onLoginSuccess("admin")
                                // » Kalau gagal...
                                } else {
                                    // » ...tampilkan pesan dari repository, atau "Email atau password salah".
                                    error = result.exceptionOrNull()?.message ?: "Email atau password salah"
                                }
                            // » Kalau mode siswa...
                            } else {
                                // » Ubah memberId (teks) menjadi angka; null kalau kosong.
                                val memberIdInt = memberId.toIntOrNull()
                                // » Kalau sudah memilih siswa...
                                if (memberIdInt != null) {
                                    // » ...ambil nama siswanya.
                                    val memberName = members.find { it.id == memberIdInt }?.name
                                    // » ...panggil login dengan:
                                    val result = repository.login(
                                        // » peran "user",
                                        role = "user",
                                        // » id siswa,
                                        memberId = memberIdInt,
                                        // » nama siswa.
                                        name = memberName
                                    )
                                    // » Kalau berhasil...
                                    if (result.isSuccess) {
                                        // » ...simpan id siswa ke sesi,
                                        repository.setMemberId(memberIdInt)
                                        // » ...beri tahu AppRoot (sukses sebagai user).
                                        onLoginSuccess("user")
                                    // » Kalau gagal...
                                    } else {
                                        // » ...tampilkan pesan error dari repository, atau "Login gagal".
                                        error = result.exceptionOrNull()?.message ?: "Login gagal"
                                    }
                                // » Kalau belum memilih siswa...
                                } else {
                                    // » ...tampilkan error "Silakan pilih nama siswa terlebih dahulu".
                                    error = "Silakan pilih nama siswa terlebih dahulu"
                                }
                            }
                            // » Proses selesai: matikan status loading.
                            loading = false
                        }
                    },
                    // » Modifier tombol:
                    modifier = Modifier
                        // » selebar kartu,
                        .fillMaxWidth()
                        // » tinggi 50dp.
                        .height(50.dp),
                    // » Sudut bulat 14dp.
                    shape = RoundedCornerShape(14.dp),
                    // » Warna tombol oranye.
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                    // » Tombol hanya bisa ditekan kalau tidak sedang loading.
                    enabled = !loading
                ) {
                    // » Kalau sedang loading...
                    if (loading) {
                        // » ...tampilkan lingkaran berputar:
                        CircularProgressIndicator(
                            // » ukuran 22dp,
                            modifier = Modifier.size(22.dp),
                            // » warna putih,
                            color = Color.White,
                            // » tebal garis 2,5dp.
                            strokeWidth = 2.5.dp
                        )
                    // » Kalau tidak loading...
                    } else {
                        // » ...tampilkan teks:
                        Text(
                            // » "Masuk",
                            "Masuk",
                            // » tebal,
                            fontWeight = FontWeight.Bold,
                            // » gaya huruf isi besar.
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                // Toggle Email / Siswa Login Mode (without "Admin" label)
                // » TextButton = tombol teks untuk berganti mode login.
                TextButton(
                    // » Saat ditekan:
                    onClick = {
                        // » balik mode (siswa <-> admin),
                        isEmailMode = !isEmailMode
                        // » hapus error.
                        error = null
                    }
                ) {
                    // » Teks tombol:
                    Text(
                        // » mode admin: "← Masuk sebagai Siswa (Pilih Nama)"; mode siswa: "Masuk dengan Email & Password".
                        text = if (isEmailMode) "← Masuk sebagai Siswa (Pilih Nama)" else "Masuk dengan Email & Password",
                        // » Warna oranye.
                        color = OrangePrimary,
                        // » Huruf kecil.
                        style = MaterialTheme.typography.bodySmall,
                        // » Agak tebal.
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
