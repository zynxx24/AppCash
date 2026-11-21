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
package com.appcash.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.List
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appcash.AppCashApplication
import com.appcash.data.repository.AppCashRepository
import com.appcash.ui.screens.*
import com.appcash.ui.theme.AppCashTheme
import com.appcash.ui.theme.OrangeContainer
import com.appcash.ui.theme.OrangePrimary
import kotlinx.coroutines.launch

// CARI-MULAI | Aplikasi dibuka dari sini: ambil repository lalu tampilkan AppRoot.
class MainActivity : ComponentActivity() {
    // Titik awal aplikasi: mengambil repository dari AppCashApplication lalu menampilkan layar.
    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as AppCashApplication
        val repository = app.repository

        setContent {
            AppCashTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppRoot(repository = repository)
                }
            }
        }
    }
}

// Penentu halaman paling atas. isLoggedIn dan currentRole disimpan dengan rememberSaveable supaya tidak hilang saat layar diputar.
// CARI-LOGIN | Menentukan yang tampil: LoginScreen (belum login) atau MainContent (sudah login).
@Composable
fun AppRoot(repository: AppCashRepository) {
    // Sesi disimpan di repository (singleton), jadi aman saat rotasi layar
    var isLoggedIn by rememberSaveable { mutableStateOf(false) }
    var currentRole by rememberSaveable { mutableStateOf("user") }
    LaunchedEffect(Unit) {
        // Jika activity dibuat ulang tapi sesi di repository sudah hilang (proses dimatikan), kembali ke login
        if (isLoggedIn && !repository.hasToken()) isLoggedIn = false
    }

    if (!isLoggedIn) {
        LoginScreen(
            repository = repository,
            onLoginSuccess = { role ->
                currentRole = role
                isLoggedIn = true
            }
        )
    } else {
        MainContent(
            repository = repository,
            isAdmin = currentRole == "admin",
            onLogout = {
                isLoggedIn = false
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
fun MainContent(repository: AppCashRepository, isAdmin: Boolean, onLogout: () -> Unit) {
    val navItems = if (isAdmin) bottomNavItemsAdmin else bottomNavItemsUser
    val pagerState = rememberPagerState(initialPage = 2) { navItems.size }
    val scope = rememberCoroutineScope()

    Scaffold(
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .height(72.dp)
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    navItems.forEachIndexed { index, screen ->
                        val selected = pagerState.currentPage == index
                        val isCenter = index == 2 // Dashboard = center

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(bottom = if (isCenter) 0.dp else 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            if (isCenter) {
                                // Elevated center Home button
                                Box(
                                    modifier = Modifier
                                        .offset(y = (-8).dp)
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(if (selected) OrangePrimary else OrangeContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    IconButton(
                                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } }
                                    ) {
                                        Icon(
                                            imageVector = if (selected) screen.iconFilled else screen.icon,
                                            contentDescription = screen.label,
                                            tint = if (selected) Color.White else OrangePrimary,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                }
                            } else {
                                // Regular tab with icon + label
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(if (selected) OrangeContainer else Color.Transparent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    IconButton(
                                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } }
                                    ) {
                                        Icon(
                                            imageVector = if (selected) screen.iconFilled else screen.icon,
                                            contentDescription = screen.label,
                                            tint = if (selected) OrangePrimary else Color(0xFF9E9E9E),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = screen.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) OrangePrimary else Color(0xFF9E9E9E),
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        // Isi halaman. when (page) memilih layar sesuai nomor halaman. key(page) membuat tiap halaman dikelola sendiri-sendiri.
        // beyondBoundsPageCount = 1: hanya halaman tetangga yang disiapkan lebih dulu supaya hemat memori.
        HorizontalPager(
            state = pagerState,
            beyondBoundsPageCount = 1, // Only pre-compose 1 neighbor page to reduce memory
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { page ->
            // key(page) ensures each page is independently composed/disposed
            key(page) {
                if (isAdmin) {
                    when (page) {
                        0 -> PaymentsScreen(repository = repository, isAdmin = true)
                        1 -> ExpensesScreen(repository = repository, isAdmin = true)
                        2 -> DashboardScreen(repository = repository, isAdmin = true)
                        3 -> MembersScreen(repository = repository, isAdmin = true)
                        4 -> ProfileScreen(repository = repository, isAdmin = true, onLogout = onLogout)
                    }
                } else {
                    when (page) {
                        0 -> PaymentsScreen(repository = repository, isAdmin = false)
                        1 -> ExpensesScreen(repository = repository, isAdmin = false)
                        2 -> DashboardScreen(repository = repository, isAdmin = false)
                        3 -> MembersScreen(repository = repository, isAdmin = false)
                        4 -> ProfileScreen(repository = repository, isAdmin = false, onLogout = onLogout)
                    }
                }
            }
        }
    }
}

// Satu tab: route (nama), label (tulisan), icon (ikon biasa), iconFilled (ikon terpilih).
data class ScreenItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val iconFilled: ImageVector = icon
)

// Admin: Kas, Pengeluaran, Dashboard, Anggota, Profil
// CARI-NAVIGASI | Label & ikon tab untuk admin (bendahara). Versi siswa ada di bottomNavItemsUser.
val bottomNavItemsAdmin = listOf(
    ScreenItem("payments", "Kas",        Icons.Outlined.ShoppingCart, Icons.Filled.ShoppingCart),
    ScreenItem("expenses", "Pengeluaran",Icons.Outlined.List,         Icons.Filled.List),
    ScreenItem("dashboard","Dashboard",  Icons.Outlined.Home,         Icons.Filled.Home),
    ScreenItem("members",  "Anggota",    Icons.Outlined.Face,          Icons.Filled.Face),
    ScreenItem("profile",  "Profil",     Icons.Outlined.AccountCircle, Icons.Filled.AccountCircle)
)

// User: Kas, Pengeluaran, Dashboard, Anggota, Profil
val bottomNavItemsUser = listOf(
    ScreenItem("payments", "Kas",        Icons.Outlined.ShoppingCart, Icons.Filled.ShoppingCart),
    ScreenItem("expenses", "Pengeluaran",Icons.Outlined.List,         Icons.Filled.List),
    ScreenItem("dashboard","Dashboard",  Icons.Outlined.Home,         Icons.Filled.Home),
    ScreenItem("members",  "Anggota",    Icons.Outlined.Face,          Icons.Filled.Face),
    ScreenItem("profile",  "Profil",     Icons.Outlined.AccountCircle, Icons.Filled.AccountCircle)
)
