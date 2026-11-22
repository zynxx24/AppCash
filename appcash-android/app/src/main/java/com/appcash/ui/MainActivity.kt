// ==========================================================================
// PETA FILE · AppCash (Kelompok 6) · MainActivity.kt
// Isi file : Activity utama, pilihan login/halaman utama, dan bottom bar 5 tab.
// Penanda  : CARI-MULAI, CARI-LOGIN, CARI-NAVIGASI
// Cara cari: VS Code Ctrl+Shift+F (Android Studio Ctrl+Shift+F) lalu ketik
//            "CARI-" untuk semua penanda, atau salah satu nama di baris Penanda.
// ==========================================================================
//  BACA INI DULU
//  File pertama yang jalan saat aplikasi dibuka. Alur:
//    1. AppCashApplication membuat satu AppCashRepository (data)
//    2. MainActivity.onCreate mengambilnya lalu memanggil AppRoot
//    3. AppRoot: belum login -> LoginScreen. Sudah login -> MainContent
//    4. MainContent: bar bawah 5 tab + HorizontalPager (geser kiri kanan antar halaman)
//  Urutan tab (nomor halaman): 0 Kas (PaymentsScreen), 1 Pengeluaran (ExpensesScreen), 2 Dashboard (DashboardScreen, halaman AWAL),
//                              3 Anggota (MembersScreen), 4 Profil (ProfileScreen)
//  isAdmin dikirim ke tiap layar: admin = akun email admin, siswa = akun pilih nama. Tampilan tiap layar berbeda sesuai isAdmin.
// » Alamat folder file ini (package com.appcash.ui).
package com.appcash.ui

// » Bundle = paket data kecil yang diberikan Android saat Activity dibuat (mis. kondisi sebelum layar
// » diputar).
import android.os.Bundle
// » ComponentActivity = jenis layar dasar untuk aplikasi Compose.
import androidx.activity.ComponentActivity
// » setContent = perintah memasang tampilan Compose ke layar.
import androidx.activity.compose.setContent
// » ExperimentalFoundationApi = penanda bahwa fitur pager (geser halaman) masih "eksperimental"; harus
// » diizinkan dengan @OptIn.
import androidx.compose.foundation.ExperimentalFoundationApi
// » background = warna latar sebuah tampilan.
import androidx.compose.foundation.background
// » Layout dasar: Row (baris), Column (kolom), Box (tumpukan), Spacer (jarak), padding, dll. (tanda * =
// » semuanya).
import androidx.compose.foundation.layout.*
// » HorizontalPager = halaman yang bisa digeser kiri-kanan (dipakai antar tab).
import androidx.compose.foundation.pager.HorizontalPager
// » rememberPagerState = menyimpan posisi halaman pager saat ini.
import androidx.compose.foundation.pager.rememberPagerState
// » CircleShape = bentuk lingkaran.
import androidx.compose.foundation.shape.CircleShape
// » Icons = daftar ikon bawaan.
import androidx.compose.material.icons.Icons
// » Ikon kalender (terisi).
import androidx.compose.material.icons.filled.DateRange
// » Ikon rumah (Dashboard) versi terisi.
import androidx.compose.material.icons.filled.Home
// » Ikon info.
import androidx.compose.material.icons.filled.Info
// » Ikon orang.
import androidx.compose.material.icons.filled.Person
// » Ikon keranjang belanja (dipakai untuk tab Kas).
import androidx.compose.material.icons.filled.ShoppingCart
// » Ikon kalender versi garis (outlined).
import androidx.compose.material.icons.outlined.DateRange
// » Ikon rumah versi garis.
import androidx.compose.material.icons.outlined.Home
// » Ikon info versi garis.
import androidx.compose.material.icons.outlined.Info
// » Ikon orang versi garis.
import androidx.compose.material.icons.outlined.Person
// » Ikon keranjang versi garis.
import androidx.compose.material.icons.outlined.ShoppingCart
// » Komponen Material 3: Scaffold, Surface, Text, IconButton, dst. (tanda * = semuanya).
import androidx.compose.material3.*
// » Alat state Compose: remember, mutableStateOf, LaunchedEffect, dll. (tanda * = semuanya).
import androidx.compose.runtime.*
// » rememberSaveable = seperti remember, tapi nilainya TIDAK hilang saat layar diputar.
import androidx.compose.runtime.saveable.rememberSaveable
// » Ikon akun (terisi).
import androidx.compose.material.icons.filled.AccountCircle
// » Ikon wajah (terisi), dipakai tab Anggota.
import androidx.compose.material.icons.filled.Face
// » Ikon daftar (terisi), dipakai tab Pengeluaran.
import androidx.compose.material.icons.filled.List
// » Ikon akun versi garis (tab Profil).
import androidx.compose.material.icons.outlined.AccountCircle
// » Ikon wajah versi garis.
import androidx.compose.material.icons.outlined.Face
// » Ikon daftar versi garis.
import androidx.compose.material.icons.outlined.List
// » Alignment = pengatur posisi (tengah, bawah, dll).
import androidx.compose.ui.Alignment
// » Modifier = daftar dekorasi tampilan (ukuran, jarak, warna).
import androidx.compose.ui.Modifier
// » clip = memotong tampilan sesuai bentuk.
import androidx.compose.ui.draw.clip
// » Color = tipe warna.
import androidx.compose.ui.graphics.Color
// » ImageVector = tipe gambar ikon vektor.
import androidx.compose.ui.graphics.vector.ImageVector
// » FontWeight = ketebalan huruf.
import androidx.compose.ui.text.font.FontWeight
// » dp = satuan ukuran (mis. 16.dp).
import androidx.compose.ui.unit.dp
// » sp = satuan ukuran huruf.
import androidx.compose.ui.unit.sp
// » Memanggil AppCashApplication (tempat repository dibuat).
import com.appcash.AppCashApplication
// » Memanggil AppCashRepository (tempat semua data).
import com.appcash.data.repository.AppCashRepository
// » Memanggil semua layar dari folder screens (PaymentsScreen, DashboardScreen, dst).
import com.appcash.ui.screens.*
// » Memanggil tema AppCash.
import com.appcash.ui.theme.AppCashTheme
// » Memanggil warna oranye muda.
import com.appcash.ui.theme.OrangeContainer
// » Memanggil warna oranye utama.
import com.appcash.ui.theme.OrangePrimary
// » launch = menjalankan tugas di "jalur belakang" (coroutine) supaya animasi tidak membekukan layar.
import kotlinx.coroutines.launch

// CARI-MULAI | Aplikasi dibuka dari sini: ambil repository lalu tampilkan AppRoot.
// » class MainActivity = layar utama yang dibuka Android saat ikon AppCash ditekan (didaftarkan di
// » AndroidManifest.xml). Satu-satunya Activity di aplikasi ini.
class MainActivity : ComponentActivity() {
    // Titik awal aplikasi: mengambil repository dari AppCashApplication lalu menampilkan layar.
    // » Mengizinkan fitur pager eksperimental.
    @OptIn(ExperimentalFoundationApi::class)
    // » onCreate = fungsi yang dijalankan Android PERTAMA KALI saat layar dibuat. savedInstanceState = kondisi
    // » tersimpan (bisa null).
    override fun onCreate(savedInstanceState: Bundle?) {
        // » Menjalankan onCreate versi bawaan Android (wajib dipanggil).
        super.onCreate(savedInstanceState)
        // » Mengambil objek Application lalu dianggap sebagai AppCashApplication ("as").
        val app = application as AppCashApplication
        // » Mengambil satu-satunya repository (sumber data) dari Application.
        val repository = app.repository

        // » setContent = mulai menggambar tampilan Compose di layar.
        setContent {
            // » Membungkus semuanya dengan tema AppCash (warna & huruf oranye).
            AppCashTheme {
                // » Surface = lapisan dasar layar.
                Surface(
                    // » fillMaxSize = penuhi seluruh layar.
                    modifier = Modifier.fillMaxSize(),
                    // » Warna latar mengikuti tema (abu muda).
                    color = MaterialTheme.colorScheme.background
                ) {
                    // » Memanggil AppRoot: penentu tampil LoginScreen atau halaman utama.
                    AppRoot(repository = repository)
                }
            }
        }
    }
}

// Penentu halaman paling atas. isLoggedIn dan currentRole disimpan dengan rememberSaveable supaya tidak hilang saat layar diputar.
// CARI-LOGIN | Menentukan yang tampil: LoginScreen (belum login) atau MainContent (sudah login).
@Composable
// » @Composable = fungsi penggambar tampilan. AppRoot(repository) = penentu halaman paling atas: login atau
// » utama.
fun AppRoot(repository: AppCashRepository) {
    // Sesi disimpan di repository (singleton), jadi aman saat rotasi layar
    // » isLoggedIn = status sudah login. mutableStateOf(false) = awalnya belum login; "by" membuat tampilan
    // » otomatis digambar ulang kalau nilainya berubah. rememberSaveable = tidak hilang saat layar diputar.
    var isLoggedIn by rememberSaveable { mutableStateOf(false) }
    // » currentRole = peran akun yang login ("user" atau "admin"); awalnya "user".
    var currentRole by rememberSaveable { mutableStateOf("user") }
    // » LaunchedEffect(Unit) = blok yang dijalankan SEKALI saat AppRoot muncul pertama kali.
    LaunchedEffect(Unit) {
        // Jika activity dibuat ulang tapi sesi di repository sudah hilang (proses dimatikan), kembali ke login
        // » Jika dicatat sudah login tapi repository sudah kehilangan sesi (proses aplikasi dimatikan Android),
        // » kembalikan ke layar login agar aman.
        if (isLoggedIn && !repository.hasToken()) isLoggedIn = false
    }

    // » Kalau BELUM login...
    if (!isLoggedIn) {
        // » ...tampilkan layar login.
        LoginScreen(
            // » Berikan repository ke layar login.
            repository = repository,
            // » onLoginSuccess = fungsi yang dijalankan LoginScreen kalau login berhasil; "role" adalah peran akun.
            onLoginSuccess = { role ->
                // » Simpan peran yang berhasil login.
                currentRole = role
                // » Tandai sudah login; tampilan otomatis berganti ke halaman utama.
                isLoggedIn = true
            }
        )
    // » Kalau SUDAH login...
    } else {
        // » ...tampilkan halaman utama (bar bawah 5 tab).
        MainContent(
            // » Berikan repository ke halaman utama.
            repository = repository,
            // » isAdmin = true kalau peran = "admin" (bendahara). Dikirim ke semua layar untuk menentukan tampilan.
            isAdmin = currentRole == "admin",
            // » onLogout = dijalankan ketika tombol Keluar ditekan di Profil.
            onLogout = {
                // » Tandai belum login; tampilan kembali ke layar login.
                isLoggedIn = false
                // » Reset peran ke "user".
                currentRole = "user"
            }
        )
    }
}

// Kerangka halaman utama. pagerState.initialPage = 2 membuat Dashboard yang pertama tampil.
// bottomBar menggambar 5 tombol; tombol tengah (Dashboard) lebih besar dan menonjol naik (offset -8.dp).
// Menekan tombol -> animateScrollToPage(index), sama seperti menggeser halaman.
// CARI-NAVIGASI | Bottom bar + HorizontalPager. Nomor halaman: 0 Kas, 1 Pengeluaran, 2 Dashboard, 3 Anggota, 4 Profil.
//   Menambah tab = tambah item di bottomNavItems* dan cabang `when (page)` di bawah.
@OptIn(ExperimentalFoundationApi::class)
@Composable
// » MainContent = kerangka halaman utama (bar bawah + isi halaman). Terima repository, isAdmin, dan fungsi
// » onLogout.
fun MainContent(repository: AppCashRepository, isAdmin: Boolean, onLogout: () -> Unit) {
    // » Pilih daftar tab: untuk admin bottomNavItemsAdmin, untuk siswa bottomNavItemsUser (isinya sekarang sama,
    // » 5 tab).
    val navItems = if (isAdmin) bottomNavItemsAdmin else bottomNavItemsUser
    // » Menyimpan posisi halaman pager. initialPage = 2 = Dashboard tampil pertama. Jumlah halaman = jumlah tab.
    val pagerState = rememberPagerState(initialPage = 2) { navItems.size }
    // » scope = "pengelola tugas belakang"; dipakai untuk menjalankan animasi geser halaman.
    val scope = rememberCoroutineScope()

    // » Scaffold = kerangka layar standar Material (ada tempat bar bawah dan isi).
    Scaffold(
        // » bottomBar = bagian bar navigasi di bawah.
        bottomBar = {
            // » Surface = lapisan bar bawah.
            Surface(
                // » Bayangan setinggi 8dp supaya bar terlihat terangkat.
                shadowElevation = 8.dp,
                // » Warna latar putih.
                color = Color.White
            ) {
                // » Row = baris mendatar yang berisi 5 tombol tab.
                Row(
                    // » Modifier = dekorasi baris:
                    modifier = Modifier
                        // » lebar penuh,
                        .fillMaxWidth()
                        // » beri ruang agar tidak tertutup tombol navigasi sistem Android,
                        .navigationBarsPadding()
                        // » tinggi 72dp,
                        .height(72.dp)
                        // » jarak kiri-kanan 4dp.
                        .padding(horizontal = 4.dp),
                    // » SpaceEvenly = 5 tombol dibagi rata.
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    // » Tombol rata ke bawah.
                    verticalAlignment = Alignment.Bottom
                ) {
                    // » Untuk setiap tab (index = nomor 0..4, screen = data tab): ulangi isi di bawah.
                    navItems.forEachIndexed { index, screen ->
                        // » selected = apakah tab ini yang sedang dibuka.
                        val selected = pagerState.currentPage == index
                        // » isCenter = apakah tab ini tombol TENGAH (nomor 2 = Dashboard).
                        val isCenter = index == 2 // Dashboard = center

                        // » Column = susunan atas-bawah satu tab (ikon di atas, tulisan di bawah).
                        Column(
                            // » Modifier:
                            modifier = Modifier
                                // » weight(1f) = tiap tab dapat lebar sama.
                                .weight(1f)
                                // » Jarak bawah: tab tengah 0, tab lain 6dp.
                                .padding(bottom = if (isCenter) 0.dp else 6.dp),
                            // » Isi rata tengah mendatar.
                            horizontalAlignment = Alignment.CenterHorizontally,
                            // » Isi rata tengah menurun.
                            verticalArrangement = Arrangement.Center
                        ) {
                            // » Kalau ini tab TENGAH (Dashboard)...
                            if (isCenter) {
                                // Elevated center Home button
                                // » ...gambar tombol bulat yang menonjol naik.
                                Box(
                                    // » Modifier:
                                    modifier = Modifier
                                        // » Dinaikkan 8dp ke atas (offset negatif) supaya menonjol.
                                        .offset(y = (-8).dp)
                                        // » Ukuran 52dp.
                                        .size(52.dp)
                                        // » Bentuk lingkaran.
                                        .clip(CircleShape)
                                        // » Warna latar: oranye kalau terpilih, krem kalau tidak.
                                        .background(if (selected) OrangePrimary else OrangeContainer),
                                    // » Ikon di tengah lingkaran.
                                    contentAlignment = Alignment.Center
                                ) {
                                    // » IconButton = tombol berbentuk ikon.
                                    IconButton(
                                        // » onClick = saat ditekan: jalankan animasi geser ke halaman nomor index (sama seperti menggeser dengan
                                        // » jari).
                                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } }
                                    ) {
                                        // » Icon = menggambar ikon.
                                        Icon(
                                            // » Ikon terisi kalau terpilih, ikon garis kalau tidak.
                                            imageVector = if (selected) screen.iconFilled else screen.icon,
                                            // » Deskripsi untuk pembaca layar (aksesibilitas): nama tab.
                                            contentDescription = screen.label,
                                            // » Warna ikon: putih kalau terpilih, oranye kalau tidak.
                                            tint = if (selected) Color.White else OrangePrimary,
                                            // » Ukuran ikon 26dp.
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                }
                            // » Kalau BUKAN tab tengah...
                            } else {
                                // Regular tab with icon + label
                                // » ...gambar lingkaran kecil tempat ikon.
                                Box(
                                    // » Modifier:
                                    modifier = Modifier
                                        // » Ukuran 42dp.
                                        .size(42.dp)
                                        // » Bentuk lingkaran.
                                        .clip(CircleShape)
                                        // » Warna latar krem kalau terpilih, transparan kalau tidak.
                                        .background(if (selected) OrangeContainer else Color.Transparent),
                                    // » Ikon di tengah.
                                    contentAlignment = Alignment.Center
                                ) {
                                    // » Tombol ikon.
                                    IconButton(
                                        // » Saat ditekan: geser ke halaman tab ini.
                                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } }
                                    ) {
                                        // » Gambar ikon.
                                        Icon(
                                            // » Ikon terisi kalau terpilih.
                                            imageVector = if (selected) screen.iconFilled else screen.icon,
                                            // » Deskripsi aksesibilitas.
                                            contentDescription = screen.label,
                                            // » Warna: oranye kalau terpilih, abu kalau tidak.
                                            tint = if (selected) OrangePrimary else Color(0xFF9E9E9E),
                                            // » Ukuran 22dp.
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                // » Tulisan nama tab di bawah ikon.
                                Text(
                                    // » Teks = nama tab (Kas, Pengeluaran, dst).
                                    text = screen.label,
                                    // » Gaya huruf kecil.
                                    style = MaterialTheme.typography.labelSmall,
                                    // » Warna: oranye kalau terpilih, abu kalau tidak.
                                    color = if (selected) OrangePrimary else Color(0xFF9E9E9E),
                                    // » Tebal kalau terpilih, normal kalau tidak.
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    // » Ukuran huruf 10sp.
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    // » Bagian ISI halaman (di atas bar bawah). innerPadding = jarak yang disisakan agar isi tidak tertutup bar
    // » bawah.
    ) { innerPadding ->
        // Isi halaman. when (page) memilih layar sesuai nomor halaman. key(page) membuat tiap halaman dikelola sendiri-sendiri.
        // beyondBoundsPageCount = 1: hanya halaman tetangga yang disiapkan lebih dulu supaya hemat memori.
        // » HorizontalPager = kumpulan halaman yang bisa digeser kiri-kanan.
        HorizontalPager(
            // » state = posisi halaman yang dibuat di baris 127.
            state = pagerState,
            // » beyondBoundsPageCount = 1: hanya 1 halaman tetangga yang disiapkan lebih dulu (hemat memori).
            beyondBoundsPageCount = 1, // Only pre-compose 1 neighbor page to reduce memory
            // » Modifier:
            modifier = Modifier
                // » Penuhi layar,
                .fillMaxSize()
                // » beri jarak mengikuti bar bawah.
                .padding(innerPadding)
        // » "page" = nomor halaman yang sedang digambar.
        ) { page ->
            // key(page) ensures each page is independently composed/disposed
            // » key(page) = tiap halaman dikelola terpisah supaya state-nya tidak tercampur.
            key(page) {
                // » Kalau akun admin...
                if (isAdmin) {
                    // » Pilih layar sesuai nomor halaman.
                    when (page) {
                        // » Halaman 0: layar Kas (PaymentsScreen) mode admin.
                        0 -> PaymentsScreen(repository = repository, isAdmin = true)
                        // » Halaman 1: Pengeluaran (ExpensesScreen) mode admin.
                        1 -> ExpensesScreen(repository = repository, isAdmin = true)
                        // » Halaman 2: Dashboard mode admin.
                        2 -> DashboardScreen(repository = repository, isAdmin = true)
                        // » Halaman 3: Anggota (MembersScreen) mode admin.
                        3 -> MembersScreen(repository = repository, isAdmin = true)
                        // » Halaman 4: Profil, bawa onLogout agar tombol Keluar bisa kembali ke login.
                        4 -> ProfileScreen(repository = repository, isAdmin = true, onLogout = onLogout)
                    }
                // » Kalau bukan admin (siswa)...
                } else {
                    // » Pilih layar sesuai nomor halaman (isAdmin = false).
                    when (page) {
                        // » Halaman 0: Kas mode siswa.
                        0 -> PaymentsScreen(repository = repository, isAdmin = false)
                        // » Halaman 1: Pengeluaran mode siswa.
                        1 -> ExpensesScreen(repository = repository, isAdmin = false)
                        // » Halaman 2: Dashboard mode siswa.
                        2 -> DashboardScreen(repository = repository, isAdmin = false)
                        // » Halaman 3: Anggota mode siswa.
                        3 -> MembersScreen(repository = repository, isAdmin = false)
                        // » Halaman 4: Profil mode siswa.
                        4 -> ProfileScreen(repository = repository, isAdmin = false, onLogout = onLogout)
                    }
                }
            }
        }
    }
}

// Satu tab: route (nama), label (tulisan), icon (ikon biasa), iconFilled (ikon terpilih).
// » ScreenItem = data satu tab di bar bawah.
data class ScreenItem(
    // » route = nama internal tab.
    val route: String,
    // » label = tulisan di bawah ikon.
    val label: String,
    // » icon = ikon versi garis (tab tidak terpilih).
    val icon: ImageVector,
    // » iconFilled = ikon versi terisi (tab terpilih); kalau tidak diisi, sama dengan icon.
    val iconFilled: ImageVector = icon
)

// Admin: Kas, Pengeluaran, Dashboard, Anggota, Profil
// CARI-NAVIGASI | Label & ikon tab untuk admin (bendahara). Versi siswa ada di bottomNavItemsUser.
// » Daftar 5 tab untuk admin. Urutan = nomor halaman 0 sampai 4.
val bottomNavItemsAdmin = listOf(
    // » Tab 0: Kas (ikon keranjang).
    ScreenItem("payments", "Kas",        Icons.Outlined.ShoppingCart, Icons.Filled.ShoppingCart),
    // » Tab 1: Pengeluaran (ikon daftar).
    ScreenItem("expenses", "Pengeluaran",Icons.Outlined.List,         Icons.Filled.List),
    // » Tab 2: Dashboard (ikon rumah), TENGAH.
    ScreenItem("dashboard","Dashboard",  Icons.Outlined.Home,         Icons.Filled.Home),
    // » Tab 3: Anggota (ikon wajah).
    ScreenItem("members",  "Anggota",    Icons.Outlined.Face,          Icons.Filled.Face),
    // » Tab 4: Profil (ikon akun).
    ScreenItem("profile",  "Profil",     Icons.Outlined.AccountCircle, Icons.Filled.AccountCircle)
)

// User: Kas, Pengeluaran, Dashboard, Anggota, Profil
// » Daftar 5 tab untuk siswa (isinya sama dengan admin; perbedaan tampilan ada di tiap layar lewat isAdmin).
val bottomNavItemsUser = listOf(
    // » Tab 0: Kas.
    ScreenItem("payments", "Kas",        Icons.Outlined.ShoppingCart, Icons.Filled.ShoppingCart),
    // » Tab 1: Pengeluaran.
    ScreenItem("expenses", "Pengeluaran",Icons.Outlined.List,         Icons.Filled.List),
    // » Tab 2: Dashboard.
    ScreenItem("dashboard","Dashboard",  Icons.Outlined.Home,         Icons.Filled.Home),
    // » Tab 3: Anggota.
    ScreenItem("members",  "Anggota",    Icons.Outlined.Face,          Icons.Filled.Face),
    // » Tab 4: Profil.
    ScreenItem("profile",  "Profil",     Icons.Outlined.AccountCircle, Icons.Filled.AccountCircle)
)
