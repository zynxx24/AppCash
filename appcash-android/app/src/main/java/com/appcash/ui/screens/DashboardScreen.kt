// ==========================================================================
// PETA FILE · AppCash (Kelompok 6) · DashboardScreen.kt
// Isi file : Dashboard: saldo, ringkasan, status kas siswa, grafik bulanan, daftar denda.
// Penanda  : CARI-SALDO, CARI-DENDA, CARI-GRAFIK, CARI-VALIDASI, CARI-FORMAT-RUPIAH
// Cara cari: VS Code Ctrl+Shift+F (Android Studio Ctrl+Shift+F) lalu ketik
//            "CARI-" untuk semua penanda, atau salah satu nama di baris Penanda.
// ==========================================================================
//  BACA INI DULU : ISI DASHBOARD DARI ATAS KE BAWAH (halaman pertama setelah login)
//    1. Header            "Selamat datang," + nama (siswa: ada foto bulat)
//    2. Kartu oranye      HERO SALDO: "Total Kas XII PPLG", angka saldo besar, pencocokan saldo fisik, kotak Pemasukan dan Pengeluaran
//    3. Dua kartu kecil   Anggota XII PPLG (33 Siswa) dan Periode Mingguan (37 Minggu)
//    4. Status Kas Anda   (HANYA siswa) minggu sudah dibayar, belum, dan denda pribadi
//    5. GRAFIK            "Grafik Kas per Bulan" = WeeklyKasLineChart, dua garis: hijau Kas+Donasi, merah Pengeluaran
//    6. Denda Kas Tertunggak (HANYA admin) rekap denda semua siswa, bisa dibuka tutup
//  JAWABAN CEPAT:
//   - Grafik di mana?  -> fungsi WeeklyKasLineChart di bawah file ini. Jenis: GRAFIK GARIS dua warna, digambar manual (Canvas), tanpa library.
//   - Isi grafik?      -> pemasukan (kas + donasi) dan pengeluaran PER BULAN. Sumbu bawah = nama bulan (Jan, Feb, ... Nov).
//   - Data grafik dari mana? -> AppCashRepository.getDashboard() (bagian "Grafik per bulan").
//   - Saldo dari mana? -> getDashboard juga. Layar ini hanya menampilkan.
//   - Ada grafik batang? -> MiniBarChart ada di PaymentsScreen.kt tetapi TIDAK dipanggil. Yang tampil hanya grafik garis ini.
// » Alamat folder (package) file ini di dalam proyek.
package com.appcash.ui.screens

// » nativeCanvas = kanvas Android asli, dipakai untuk menggambar teks pada grafik.
import androidx.compose.ui.graphics.nativeCanvas

// » orangeCard = dekorasi kartu oranye bergradien (Modifiers.kt).
import com.appcash.ui.theme.orangeCard
// » AnimatedVisibility = animasi muncul/hilang sebuah tampilan.
import androidx.compose.animation.AnimatedVisibility
// » expandVertically = animasi membuka ke bawah saat tampilan muncul.
import androidx.compose.animation.expandVertically
// » shrinkVertically = animasi mengecil ke atas saat tampilan menghilang.
import androidx.compose.animation.shrinkVertically
// » Canvas = kanvas kosong untuk menggambar sendiri (garis, lingkaran, grafik).
import androidx.compose.foundation.Canvas
// » background = mengisi warna/gradien latar sebuah tampilan.
import androidx.compose.foundation.background
// » clickable = membuat tampilan bisa diketuk.
import androidx.compose.foundation.clickable
// » Layout dasar: Column (susun atas-bawah), Row (susun kiri-kanan), Box (tumpuk), Spacer (jarak kosong),
// » padding, size, fillMaxWidth, dll.
import androidx.compose.foundation.layout.*
// » rememberScrollState = menyimpan posisi gulir.
import androidx.compose.foundation.rememberScrollState
// » RoundedCornerShape = bentuk persegi dengan sudut membulat.
import androidx.compose.foundation.shape.RoundedCornerShape
// » verticalScroll = membuat isi bisa digulir naik-turun.
import androidx.compose.foundation.verticalScroll
// » Icons = daftar ikon bawaan Material.
import androidx.compose.material.icons.Icons
// » Ikon bawaan Material: Check (versi terisi).
import androidx.compose.material.icons.filled.Check
// » Ikon bawaan Material: DateRange (versi terisi).
import androidx.compose.material.icons.filled.DateRange
// » Ikon bawaan Material: KeyboardArrowDown (versi terisi).
import androidx.compose.material.icons.filled.KeyboardArrowDown
// » Ikon bawaan Material: KeyboardArrowUp (versi terisi).
import androidx.compose.material.icons.filled.KeyboardArrowUp
// » Ikon bawaan Material: Person (versi terisi).
import androidx.compose.material.icons.filled.Person
// » Ikon bawaan Material: ShoppingCart (versi terisi).
import androidx.compose.material.icons.filled.ShoppingCart
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
// » Path = jalur garis yang digambar di Canvas (dipakai garis grafik).
import androidx.compose.ui.graphics.Path
// » PathEffect = efek pada garis (mis. putus-putus).
import androidx.compose.ui.graphics.PathEffect
// » StrokeCap = bentuk ujung garis (bulat atau rata).
import androidx.compose.ui.graphics.StrokeCap
// » StrokeJoin = bentuk sambungan antar garis (bulat atau lancip).
import androidx.compose.ui.graphics.StrokeJoin
// » Stroke = gaya menggambar garis tepi (tebal garis, ujung).
import androidx.compose.ui.graphics.drawscope.Stroke
// » ImageVector = tipe gambar ikon vektor.
import androidx.compose.ui.graphics.vector.ImageVector
// » FontWeight = ketebalan huruf (Normal, Medium, Bold, ExtraBold).
import androidx.compose.ui.text.font.FontWeight
// » TextOverflow = cara menangani teks yang terlalu panjang (Ellipsis = dipotong dengan '...').
import androidx.compose.ui.text.style.TextOverflow
// » dp = satuan ukuran Android yang sama di semua layar (mis. 16.dp).
import androidx.compose.ui.unit.dp
// » sp = satuan ukuran huruf Android (mengikuti pengaturan ukuran font di HP).
import androidx.compose.ui.unit.sp
// » Dashboard = paket angka siap tampil untuk layar Dashboard.
import com.appcash.data.model.Dashboard
// » DendaInfo = hasil hitung denda satu siswa.
import com.appcash.data.model.DendaInfo
// » UserProfile = bentuk data profil akun yang login.
import com.appcash.data.model.UserProfile
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

// Layar Dashboard. isAdmin menentukan bagian mana yang tampil (Status Kas Anda untuk siswa, rekap denda untuk admin).
// CARI-SALDO CARI-DENDA | Kartu saldo (dicocokkan dengan saldo fisik), status kas pribadi, dan rekap denda (khusus admin).
// » Penanda fungsi penggambar tampilan.
@Composable
// » DashboardScreen = tab DASHBOARD (tab ke-3, halaman AWAL). repository = sumber data; isAdmin = true kalau
// » admin. Layar ini hanya MENAMPILKAN angka dari repository.
fun DashboardScreen(repository: AppCashRepository, isAdmin: Boolean) {
    // Variabel penampung data (kosong dulu, diisi setelah data dimuat): dashboard (angka saldo dan grafik), dendaList (rekap denda semua siswa),
    // userProfile, userPaidWeeks/userTotalWeeks/userDenda (status kas siswa yang login), dendaExpanded (rekap denda terbuka atau tertutup).
    // » dashboard = paket angka saldo & grafik dari getDashboard(). null = belum dimuat.
    var dashboard by remember { mutableStateOf<Dashboard?>(null) }
    // » dendaList = rekap denda semua siswa (untuk admin). Awalnya kosong.
    var dendaList by remember { mutableStateOf<List<DendaInfo>>(emptyList()) }
    // » dendaExpanded = apakah kartu rekap denda admin sedang terbuka. Awalnya tertutup.
    var dendaExpanded by remember { mutableStateOf(false) }
    // » error = pesan error; null = tidak ada.
    var error by remember { mutableStateOf<String?>(null) }
    // » loading = true selama data dimuat.
    var loading by remember { mutableStateOf(true) }
    // » userProfile = profil siswa yang login (untuk sapaan nama).
    var userProfile by remember { mutableStateOf<UserProfile?>(null) }
    // » userPaidWeeks = jumlah minggu yang sudah dibayar siswa yang login. mutableIntStateOf = state khusus
    // » angka bulat.
    var userPaidWeeks by remember { mutableIntStateOf(0) }
    // » userTotalWeeks = jumlah minggu WAJIB bayar siswa yang login.
    var userTotalWeeks by remember { mutableIntStateOf(0) }
    // » userDenda = denda pribadi siswa yang login (null = tidak ada denda).
    var userDenda by remember { mutableStateOf<DendaInfo?>(null) }
    // » currentMemberId = id siswa yang login (null kalau admin).
    var currentMemberId by remember { mutableStateOf<Int?>(null) }

    // Setiap data berubah, nomor versi naik, LaunchedEffect di bawah berjalan ulang dan semua angka dimuat ulang. Itu sebabnya
    // setelah admin mencentang pembayaran, saldo di Dashboard langsung berubah tanpa tombol refresh.
    // » dataVersion = nomor versi data dari repository. Kalau berubah (mis. admin mencentang pembayaran), blok
    // » di bawah berjalan lagi dan semua angka dimuat ulang.
    val dataVersion by repository.version.collectAsState()
    // Memuat data: dashboard, rekap denda, profil, lalu (khusus siswa) status minggu dan denda pribadinya. loading = false setelah selesai.
    // » LaunchedEffect(dataVersion) = muat data setiap dataVersion berubah.
    LaunchedEffect(dataVersion) {
        // » Hapus error lama.
        error = null
        // » Ambil angka Dashboard (saldo, grafik) lalu:
        repository.getDashboard().fold(
            // » berhasil: simpan ke dashboard,
            onSuccess = { dashboard = it },
            // » gagal: simpan pesan error.
            onFailure = { error = it.message ?: "Gagal memuat dashboard" }
        )
        // » Ambil rekap denda semua siswa lalu:
        repository.getDendaForAllMembers().fold(
            // » berhasil: simpan ke dendaList,
            onSuccess = { dendaList = it },
            // » gagal: abaikan.
            onFailure = { }
        )
        // Load user-specific data
        // » Ambil profil akun yang login lalu:
        repository.getCurrentUser().fold(
            // » berhasil: simpan ke userProfile,
            onSuccess = { userProfile = it },
            // » gagal: abaikan.
            onFailure = {}
        )
        // » Ambil id siswa yang login.
        val memberId = repository.getCurrentMemberId()
        // » Simpan ke currentMemberId.
        currentMemberId = memberId
        // » Kalau yang login siswa (bukan admin) dan id ada:
        if (memberId != null && !isAdmin) {
            // » ambil status bayar siswa itu (pasangan: lunas, wajib) lalu:
            repository.getPaymentStatusForMember(memberId).fold(
                // » berhasil, pasangan dibongkar jadi (paid, total):
                onSuccess = { (paid, total) ->
                    // » simpan jumlah lunas,
                    userPaidWeeks = paid
                    // » simpan jumlah wajib.
                    userTotalWeeks = total
                },
                // » gagal: abaikan.
                onFailure = {}
            )
            // » ambil denda siswa itu lalu:
            repository.getDendaForMember(memberId).fold(
                // » berhasil: simpan ke userDenda,
                onSuccess = { userDenda = it },
                // » gagal: abaikan.
                onFailure = {}
            )
        }
        // » Selesai memuat: loading = false.
        loading = false
    }

    // » Box penuh layar dengan latar sesuai tema.
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // » when = pilih satu keadaan:
        when {
            // Tiga keadaan layar: loading (lingkaran berputar), error (tombol Coba lagi), atau data siap (tampilan penuh di bawah).
            // » loading: tampilkan lingkaran berputar...
            loading -> CircularProgressIndicator(
                // » ...di tengah layar,
                modifier = Modifier.align(Alignment.Center),
                // » ...warna oranye.
                color = OrangePrimary
            )
            // » error: tampilkan ErrorView dengan tombol "Coba lagi" (memanggil repository.refresh).
            error != null -> ErrorView(error = error!!, onRetry = { repository.refresh() })
            // » data siap:
            dashboard != null -> {
                // » d = paket angka Dashboard (dipastikan tidak null).
                val d = dashboard!!
                // » Kolom isi seluruh dashboard:
                Column(
                    // » Modifier:
                    modifier = Modifier
                        // » penuhi layar,
                        .fillMaxSize()
                        // » bisa digulir naik-turun.
                        .verticalScroll(rememberScrollState())
                ) {
                    // BAGIAN 1: sapaan. Admin tampil "Administrator", siswa tampil nama dan fotonya (StudentAvatar).
                    // Top Header
                    // » BAGIAN 1: baris sapaan di atas.
                    Row(
                        // » Modifier:
                        modifier = Modifier
                            // » selebar layar,
                            .fillMaxWidth()
                            // » jarak kiri-kanan 20dp, atas-bawah 16dp.
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        // » Isi dua sisi.
                        horizontalArrangement = Arrangement.SpaceBetween,
                        // » Rata tengah vertikal.
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // » Baris foto + teks, jarak 12dp.
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            // » Kalau siswa yang login...
                            if (!isAdmin && currentMemberId != null) {
                                // » ...tampilkan foto siswa ukuran 44.
                                StudentAvatar(memberId = currentMemberId!!, size = 44)
                            }
                            // » Kolom dua baris teks.
                            Column {
                                // » Teks sapaan:
                                Text(
                                    // » "Selamat datang,",
                                    "Selamat datang,",
                                    // » gaya isi sedang,
                                    style = MaterialTheme.typography.bodyMedium,
                                    // » warna abu.
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                // » Teks nama:
                                Text(
                                    // » admin: "Administrator"; siswa: nama dari profil (atau "Student XII PPLG" kalau belum ada),
                                    if (isAdmin) "Administrator" else userProfile?.name ?: "Student XII PPLG",
                                    // » gaya judul besar,
                                    style = MaterialTheme.typography.titleLarge,
                                    // » sangat tebal,
                                    fontWeight = FontWeight.ExtraBold,
                                    // » warna teks utama.
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }

                    // BAGIAN 2: KARTU SALDO. Latar gradien oranye (orangeCard).
                    //   d.totalBalance = saldo buku (dihitung getDashboard).  selisih = saldo fisik di peti dikurangi saldo buku.
                    //   Bila selisih 0 tampil "Sama dengan saldo fisik di peti kas", bila tidak tampil angka selisihnya.
                    //   Pemasukan = kas + donasi (Rp4.885.000), Pengeluaran = Rp3.496.000.
                    // Hero Balance Card (orange gradient)
                    // » BAGIAN 2: kartu saldo utama (hero card) oranye.
                    Box(
                        // » Modifier:
                        modifier = Modifier
                            // » selebar layar,
                            .fillMaxWidth()
                            // » jarak kiri-kanan 16dp,
                            .padding(horizontal = 16.dp)
                            // » kartu oranye 24dp,
                            .orangeCard(24.dp)
                            // » jarak dalam 24dp.
                            .padding(24.dp)
                    ) {
                        // » Kolom isi kartu, jarak antar baris 12dp.
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // » Teks judul kartu:
                            Text(
                                // » "Total Kas XII PPLG",
                                "Total Kas XII PPLG",
                                // » gaya isi sedang,
                                style = MaterialTheme.typography.bodyMedium,
                                // » putih agak transparan,
                                color = Color.White.copy(alpha = 0.85f),
                                // » agak tebal.
                                fontWeight = FontWeight.SemiBold
                            )
                            // » Angka SALDO besar:
                            Text(
                                // » "Rp" + saldo buku (totalBalance) dengan titik ribuan,
                                "Rp ${formatRupiah(d.totalBalance)}",
                                // » ukuran huruf 34sp,
                                fontSize = 34.sp,
                                // » maksimal 1 baris,
                                maxLines = 1,
                                // » kalau kepanjangan dipotong dengan "...",
                                overflow = TextOverflow.Ellipsis,
                                // » sangat tebal,
                                fontWeight = FontWeight.ExtraBold,
                                // » putih.
                                color = Color.White
                            )
                            // Cocokkan saldo buku dengan uang fisik di peti kas
                            // » selisih = saldo fisik di peti dikurangi saldo buku. Kalau 0, berarti cocok.
                            val selisih = d.realBalance - d.totalBalance
                            // » Teks pencocokan:
                            Text(
                                // » kalau selisih 0 → "✓ Sama dengan saldo fisik di peti kas",
                                if (selisih == 0) "✓ Sama dengan saldo fisik di peti kas"
                                // » kalau tidak → tampilkan saldo fisik dan nilai selisih (selalu positif lewat abs).
                                else "Saldo fisik Rp ${formatRupiah(d.realBalance)} (selisih Rp ${formatRupiah(kotlin.math.abs(selisih))})",
                                // » huruf kecil,
                                style = MaterialTheme.typography.labelSmall,
                                // » putih agak transparan.
                                color = Color.White.copy(alpha = 0.9f)
                            )
                            // » Baris dua kotak kecil: Pemasukan dan Pengeluaran.
                            Row(
                                // » Selebar kartu.
                                modifier = Modifier.fillMaxWidth(),
                                // » Jarak antar kotak 12dp.
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Income pill
                                // » Kotak Pemasukan:
                                Box(
                                    // » Modifier:
                                    modifier = Modifier
                                        // » mengambil lebar sama (weight 1),
                                        .weight(1f)
                                        // » sudut bulat 12dp,
                                        .clip(RoundedCornerShape(12.dp))
                                        // » putih transparan 20%,
                                        .background(Color.White.copy(alpha = 0.2f))
                                        // » jarak dalam 10dp.
                                        .padding(10.dp)
                                ) {
                                    // » Kolom isi kotak.
                                    Column {
                                        // » Label "Pemasukan":
                                        Text(
                                            // » huruf kecil,
                                            "Pemasukan",
                                            // » putih agak transparan.
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                        // » Baris nilai.
                                        Row(
                                            // » Rata tengah vertikal.
                                            verticalAlignment = Alignment.CenterVertically,
                                            // » Jarak 4dp.
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            // » Teks nilai:
                                            Text(
                                                // » "Rp" + iuran + donasi (totalKasIncome + totalDonationAmount),
                                                "Rp ${formatRupiah(d.totalKasIncome + d.totalDonationAmount)}",
                                                // » maksimal 1 baris,
                                                maxLines = 1,
                                                // » dipotong "..." bila panjang,
                                                overflow = TextOverflow.Ellipsis,
                                                // » gaya isi sedang,
                                                style = MaterialTheme.typography.bodyMedium,
                                                // » tebal,
                                                fontWeight = FontWeight.Bold,
                                                // » putih.
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                                // Expense pill
                                // » Kotak Pengeluaran:
                                Box(
                                    // » Modifier:
                                    modifier = Modifier
                                        // » lebar sama,
                                        .weight(1f)
                                        // » sudut bulat 12dp,
                                        .clip(RoundedCornerShape(12.dp))
                                        // » putih transparan 20%,
                                        .background(Color.White.copy(alpha = 0.2f))
                                        // » jarak dalam 10dp.
                                        .padding(10.dp)
                                ) {
                                    // » Kolom isi kotak.
                                    Column {
                                        // » Label "Pengeluaran":
                                        Text(
                                            "Pengeluaran",
                                            // » huruf kecil,
                                            style = MaterialTheme.typography.labelSmall,
                                            // » putih agak transparan.
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                        // » Teks nilai:
                                        Text(
                                            // » "Rp" + total pengeluaran,
                                            "Rp ${formatRupiah(d.totalExpenseAmount)}",
                                                // » maksimal 1 baris,
                                                maxLines = 1,
                                                // » dipotong "..." bila panjang,
                                                overflow = TextOverflow.Ellipsis,
                                            // » gaya isi sedang,
                                            style = MaterialTheme.typography.bodyMedium,
                                            // » tebal,
                                            fontWeight = FontWeight.Bold,
                                            // » putih.
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // » Jarak kosong 20dp.
                    Spacer(modifier = Modifier.height(20.dp))

                    // BAGIAN 3: dua kartu kecil: jumlah siswa (d.totalMembers) dan jumlah minggu kas (d.totalPaymentDates).
                    // Stat Cards Row – 2 orange gradient cards matching design
                    // » BAGIAN 3: baris dua kartu kecil.
                    Row(
                        // » Modifier:
                        modifier = Modifier
                            // » selebar layar,
                            .fillMaxWidth()
                            // » jarak kiri-kanan 16dp.
                            .padding(horizontal = 16.dp),
                        // » Jarak antar kartu 12dp.
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Anggota Card
                        // » Kartu Anggota:
                        Box(
                            // » Modifier:
                            modifier = Modifier
                                // » lebar sama,
                                .weight(1f)
                                // » kartu oranye 16dp,
                                .orangeCard(16.dp)
                                // » jarak dalam 14dp.
                                .padding(14.dp)
                        ) {
                            // » Kolom isi, jarak 6dp.
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                // » Ikon orang.
                                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(24.dp))
                                // » Teks "Anggota XII PPLG".
                                Text("Anggota XII PPLG", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.85f))
                                // » Lencana jumlah:
                                Surface(
                                    // » sudut bulat 12dp,
                                    shape = RoundedCornerShape(12.dp),
                                    // » putih transparan 25%.
                                    color = Color.White.copy(alpha = 0.25f)
                                ) {
                                    // » Teks:
                                    Text(
                                        // » "<jumlah siswa> Siswa" (33),
                                        "${d.totalMembers} Siswa",
                                        // » huruf kecil,
                                        style = MaterialTheme.typography.labelSmall,
                                        // » tebal,
                                        fontWeight = FontWeight.Bold,
                                        // » putih,
                                        color = Color.White,
                                        // » jarak dalam lencana.
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                        // Periode Card
                        // » Kartu Periode:
                        Box(
                            // » Modifier:
                            modifier = Modifier
                                // » lebar sama,
                                .weight(1f)
                                // » kartu oranye 16dp,
                                .orangeCard(16.dp)
                                // » jarak dalam 14dp.
                                .padding(14.dp)
                        ) {
                            // » Kolom isi, jarak 6dp.
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                // » Ikon kalender.
                                Icon(Icons.Default.DateRange, contentDescription = null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(24.dp))
                                // » Teks "Periode Mingguan".
                                Text("Periode Mingguan", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.85f))
                                // » Lencana jumlah:
                                Surface(
                                    // » sudut bulat 12dp,
                                    shape = RoundedCornerShape(12.dp),
                                    // » putih transparan 25%.
                                    color = Color.White.copy(alpha = 0.25f)
                                ) {
                                    // » Teks:
                                    Text(
                                        // » "<jumlah minggu> Minggu" (37),
                                        "${d.totalPaymentDates} Minggu",
                                        // » huruf kecil,
                                        style = MaterialTheme.typography.labelSmall,
                                        // » tebal,
                                        fontWeight = FontWeight.Bold,
                                        // » putih,
                                        color = Color.White,
                                        // » jarak dalam lencana.
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    // » Jarak kosong 16dp.
                    Spacer(modifier = Modifier.height(16.dp))

                    // BAGIAN 4 (hanya siswa): kartu hijau bila semua lunas, kuning bila ada minggu belum bayar. unpaidWeeks = wajib dikurangi lunas.
                    // Denda pribadi tampil di kanan bila ada. Siswa hanya melihat dirinya sendiri di sini.
                    // USER: Status Kas Anda Card
                    // » BAGIAN 4: kartu "Status Kas Anda", hanya tampil bila yang login SISWA.
                    if (!isAdmin && currentMemberId != null) {
                        // » unpaidWeeks = minggu wajib dikurangi minggu lunas = minggu yang belum dibayar.
                        val unpaidWeeks = userTotalWeeks - userPaidWeeks
                        // » Card kartu status:
                        Card(
                            // » Modifier:
                            modifier = Modifier
                                // » selebar layar,
                                .fillMaxWidth()
                                // » jarak kiri-kanan 16dp.
                                .padding(horizontal = 16.dp),
                            // » Sudut bulat 20dp.
                            shape = RoundedCornerShape(20.dp),
                            // » Warna kartu: hijau muda kalau semua lunas, kuning muda kalau masih ada tunggakan.
                            colors = CardDefaults.cardColors(containerColor = if (unpaidWeeks == 0) Color(0xFFE8F5E9) else Color(0xFFFFF8E1)),
                            // » Bayangan 2dp.
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            // » Kolom isi, jarak dalam 16dp, jarak antar baris 8dp.
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                // » Teks judul:
                                Text(
                                    // » "Status Kas Anda",
                                    "Status Kas Anda",
                                    // » gaya sub-judul kecil,
                                    style = MaterialTheme.typography.titleSmall,
                                    // » tebal,
                                    fontWeight = FontWeight.Bold,
                                    // » warna: hijau kalau lunas, oranye gelap kalau belum.
                                    color = if (unpaidWeeks == 0) GreenPositive else OrangeDark
                                )
                                // » Baris: status di kiri, denda di kanan.
                                Row(
                                    // » Selebar kartu.
                                    modifier = Modifier.fillMaxWidth(),
                                    // » Teks kiri dan kanan berjauhan.
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    // » Rata tengah vertikal.
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // » Kolom status, mengambil sisa lebar.
                                    Column(modifier = Modifier.weight(1f)) {
                                        // » Kalau tidak ada tunggakan...
                                        if (unpaidWeeks == 0) {
                                            // » ...teks:
                                            Text(
                                                // » "✓ Semua kas sudah LUNAS!",
                                                "✓ Semua kas sudah LUNAS!",
                                                // » gaya isi sedang,
                                                style = MaterialTheme.typography.bodyMedium,
                                                // » tebal,
                                                fontWeight = FontWeight.Bold,
                                                // » hijau.
                                                color = GreenPositive
                                            )
                                        // » Kalau ada tunggakan...
                                        } else {
                                            // » ...teks:
                                            Text(
                                                // » "⚠ <n> minggu belum bayar",
                                                "⚠ $unpaidWeeks minggu belum bayar",
                                                // » gaya isi sedang,
                                                style = MaterialTheme.typography.bodyMedium,
                                                // » tebal,
                                                fontWeight = FontWeight.Bold,
                                                // » oranye gelap.
                                                color = OrangeDark
                                            )
                                        }
                                        // » Teks rincian:
                                        Text(
                                            // » "<lunas> / <wajib> minggu sudah dibayar",
                                            "$userPaidWeeks / $userTotalWeeks minggu sudah dibayar",
                                            // » huruf kecil,
                                            style = MaterialTheme.typography.bodySmall,
                                            // » abu tua.
                                            color = TextMedium
                                        )
                                    }
                                    // » Kalau siswa punya denda...
                                    if (userDenda != null) {
                                        // » ...kolom denda rata kanan:
                                        Column(horizontalAlignment = Alignment.End) {
                                            // » teks label:
                                            Text(
                                                // » "Denda",
                                                "Denda",
                                                // » huruf kecil,
                                                style = MaterialTheme.typography.labelSmall,
                                                // » merah.
                                                color = RedNegative
                                            )
                                            // » teks nilai:
                                            Text(
                                                // » "Rp" + jumlah denda,
                                                "Rp ${formatRupiah(userDenda!!.dendaAmount)}",
                                                // » gaya isi sedang,
                                                style = MaterialTheme.typography.bodyMedium,
                                                // » tebal,
                                                fontWeight = FontWeight.Bold,
                                                // » merah.
                                                color = RedNegative
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // » Jarak kosong 16dp.
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // BAGIAN 5: GRAFIK. Kartu putih berisi judul "Grafik Kas per Bulan", legenda dua warna, lalu WeeklyKasLineChart setinggi 180.dp.
                    // Kartu hanya digambar bila ada data (d.weeklyKasIncome.isNotEmpty()).
                    // Chart Card
                    // » Kartu grafik hanya digambar kalau ada data pemasukan per bulan (daftar tidak kosong).
                    if (d.weeklyKasIncome.isNotEmpty()) {
                        // » Card putih berisi grafik:
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
                            // » Kolom isi kartu:
                            Column(
                                // » jarak dalam 16dp,
                                modifier = Modifier.padding(16.dp),
                                // » jarak antar baris 12dp.
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // » Teks judul:
                                Text(
                                    // » "Grafik Kas per Bulan",
                                    "Grafik Kas per Bulan",
                                    // » gaya sub-judul kecil,
                                    style = MaterialTheme.typography.titleSmall,
                                    // » tebal,
                                    fontWeight = FontWeight.Bold,
                                    // » oranye.
                                    color = OrangePrimary
                                )
                                // » Baris keterangan warna (legenda), jarak 16dp.
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    // » Legenda hijau "Kas + Donasi" (garis pemasukan).
                                    LegendDot(color = GreenPositive, label = "Kas + Donasi")
                                    // » Legenda merah "Pengeluaran" (garis pengeluaran).
                                    LegendDot(color = RedNegative, label = "Pengeluaran")
                                }
                                // » Memanggil fungsi penggambar GRAFIK (di bawah file ini):
                                WeeklyKasLineChart(
                                    // » labels = nama bulan untuk sumbu bawah,
                                    labels = d.weeklyLabels,
                                    // » incomes = pemasukan tiap bulan (garis hijau),
                                    incomes = d.weeklyKasIncome,
                                    // » expenses = pengeluaran tiap bulan (garis merah),
                                    expenses = d.weeklyExpenses,
                                    // » ukuran: selebar kartu, tinggi 180dp.
                                    modifier = Modifier.fillMaxWidth().height(180.dp)
                                )
                            }
                        }
                    }

                    // BAGIAN 6 (hanya admin): kartu "Denda Kas Tertunggak". Kalau ditekan, daftar detail per siswa membuka/menutup (AnimatedVisibility).
                    // Siswa tidak melihat bagian ini supaya yang menunggak tidak dipermalukan.
                    // Denda Summary Card — Expandable
                    // CARI-DENDA | Rekap denda bernama hanya untuk admin, siswa melihat dendanya sendiri di kartu Status Kas Anda.
                    // TANYA-GURU: Kenapa rekap denda tidak muncul di akun siswa? Supaya yang belum bayar tidak dipermalukan (sasaran menyama braya).
                    // » Kartu denda hanya untuk ADMIN dan hanya kalau ada siswa yang punya denda.
                    if (isAdmin && dendaList.isNotEmpty()) {
                        // » Jarak kosong 16dp.
                        Spacer(modifier = Modifier.height(16.dp))
                        // » Card kartu denda:
                        Card(
                            // » Modifier:
                            modifier = Modifier
                                // » selebar layar,
                                .fillMaxWidth()
                                // » jarak kiri-kanan 16dp.
                                .padding(horizontal = 16.dp),
                            // » Kalau diketuk: balik status terbuka/tertutup (dendaExpanded).
                            onClick = { dendaExpanded = !dendaExpanded },
                            // » Sudut bulat 20dp.
                            shape = RoundedCornerShape(20.dp),
                            // » Warna putih.
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            // » Bayangan 2dp.
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            // » Kolom isi:
                            Column(
                                // » jarak dalam 16dp,
                                modifier = Modifier.padding(16.dp),
                                // » jarak antar baris 10dp.
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Header row with toggle icon
                                // » Baris judul + ikon panah.
                                Row(
                                    // » Selebar kartu.
                                    modifier = Modifier.fillMaxWidth(),
                                    // » Judul di kiri, ikon di kanan.
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    // » Rata tengah vertikal.
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // » Teks judul:
                                    Text(
                                        // » "⚠️ Denda Kas Tertunggak",
                                        "⚠️ Denda Kas Tertunggak",
                                        // » gaya sub-judul kecil,
                                        style = MaterialTheme.typography.titleSmall,
                                        // » tebal,
                                        fontWeight = FontWeight.Bold,
                                        // » merah.
                                        color = RedNegative
                                    )
                                    // » Ikon panah:
                                    Icon(
                                        // » panah atas kalau terbuka, panah bawah kalau tertutup,
                                        imageVector = if (dendaExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        // » deskripsi aksesibilitas "Tutup"/"Buka",
                                        contentDescription = if (dendaExpanded) "Tutup" else "Buka",
                                        // » warna merah.
                                        tint = RedNegative
                                    )
                                }
                                // » Teks keterangan:
                                Text(
                                    // » "Denda 5% per bulan dari total kas yang belum dibayar",
                                    "Denda 5% per bulan dari total kas yang belum dibayar",
                                    // » huruf kecil,
                                    style = MaterialTheme.typography.bodySmall,
                                    // » abu tua.
                                    color = TextMedium
                                )
                                // » Baris ringkasan dua sisi.
                                Row(
                                    // » Selebar kartu.
                                    modifier = Modifier.fillMaxWidth(),
                                    // » Kiri dan kanan berjauhan.
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // » Kolom kiri.
                                    Column {
                                        // » Teks jumlah anggota:
                                        Text(
                                            // » "<n> Anggota" (jumlah siswa yang punya denda),
                                            "${dendaList.size} Anggota",
                                            // » gaya sub-judul,
                                            style = MaterialTheme.typography.titleMedium,
                                            // » tebal,
                                            fontWeight = FontWeight.Bold,
                                            // » oranye gelap.
                                            color = OrangeDark
                                        )
                                        // » Teks "punya tunggakan":
                                        Text(
                                            "punya tunggakan",
                                            // » huruf kecil,
                                            style = MaterialTheme.typography.bodySmall,
                                            // » abu tua.
                                            color = TextMedium
                                        )
                                    }
                                    // » Kolom kanan rata kanan.
                                    Column(horizontalAlignment = Alignment.End) {
                                        // » Teks total denda:
                                        Text(
                                            // » "Rp" + jumlah semua dendaAmount,
                                            "Rp ${formatRupiah(dendaList.sumOf { it.dendaAmount })}",
                                            // » gaya sub-judul,
                                            style = MaterialTheme.typography.titleMedium,
                                            // » tebal,
                                            fontWeight = FontWeight.Bold,
                                            // » merah.
                                            color = RedNegative
                                        )
                                        // » Teks "total denda":
                                        Text(
                                            "total denda",
                                            // » huruf kecil,
                                            style = MaterialTheme.typography.bodySmall,
                                            // » abu tua.
                                            color = TextMedium
                                        )
                                    }
                                }

                                // Expandable detail list
                                // » AnimatedVisibility = bagian detail yang membuka/menutup dengan animasi.
                                AnimatedVisibility(
                                    // » Terlihat hanya kalau dendaExpanded true.
                                    visible = dendaExpanded,
                                    // » Animasi muncul: membuka ke bawah.
                                    enter = expandVertically(),
                                    // » Animasi hilang: menutup ke atas.
                                    exit = shrinkVertically()
                                ) {
                                    // » Kolom detail, jarak antar baris 8dp.
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        // » Garis pemisah merah tipis.
                                        HorizontalDivider(color = RedNegative.copy(alpha = 0.2f))
                                        // » Teks judul detail:
                                        Text(
                                            // » "Detail Per Anggota:",
                                            "Detail Per Anggota:",
                                            // » gaya label sedang,
                                            style = MaterialTheme.typography.labelMedium,
                                            // » tebal,
                                            fontWeight = FontWeight.Bold,
                                            // » oranye gelap.
                                            color = OrangeDark
                                        )
                                        // » Untuk setiap siswa yang punya denda...
                                        dendaList.forEach { denda ->
                                            // » ...satu baris kotak:
                                            Row(
                                                // » Modifier:
                                                modifier = Modifier
                                                    // » selebar kartu,
                                                    .fillMaxWidth()
                                                    // » sudut bulat 10dp,
                                                    .clip(RoundedCornerShape(10.dp))
                                                    // » latar merah sangat muda,
                                                    .background(RedNegative.copy(alpha = 0.06f))
                                                    // » jarak dalam 10dp.
                                                    .padding(10.dp),
                                                // » Nama kiri, jumlah kanan.
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                // » Rata tengah vertikal.
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // » Kolom nama dan rincian, mengisi sisa lebar.
                                                Column(modifier = Modifier.weight(1f)) {
                                                    // » Teks nama siswa:
                                                    Text(
                                                        // » isinya memberName,
                                                        denda.memberName,
                                                        // » huruf kecil,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        // » agak tebal,
                                                        fontWeight = FontWeight.SemiBold,
                                                        // » maksimal 1 baris,
                                                        maxLines = 1,
                                                        // » dipotong "..." bila panjang.
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    // » Teks rincian:
                                                    Text(
                                                        // » "<n> bulan tunggakan • <persen>%",
                                                        "${denda.unpaidMonths} bulan tunggakan • ${denda.dendaPercentage.toInt()}%",
                                                        // » huruf kecil,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        // » abu tua.
                                                        color = TextMedium
                                                    )
                                                }
                                                // » Teks jumlah denda:
                                                Text(
                                                    // » "Rp" + dendaAmount,
                                                    "Rp ${formatRupiah(denda.dendaAmount)}",
                                                    // » huruf kecil,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    // » tebal,
                                                    fontWeight = FontWeight.Bold,
                                                    // » merah.
                                                    color = RedNegative
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // » Jarak kosong 20dp di bagian bawah halaman.
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

// Kotak warna kecil + tulisan untuk keterangan grafik (pemasukan/pengeluaran).
// » Penanda fungsi penggambar tampilan.
@Composable
// » LegendDot = keterangan warna grafik: kotak kecil berwarna + tulisan.
fun LegendDot(color: Color, label: String) {
    // » Baris, jarak 4dp, rata tengah vertikal.
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        // » Kotak warna: ukuran 10dp, sudut bulat 2dp, warna sesuai parameter.
        Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(color))
        // » Tulisan keterangan: huruf kecil, warna abu.
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// Kartu angka di Dashboard (saldo, pemasukan, pengeluaran, jumlah anggota).
// » Penanda fungsi penggambar tampilan.
@Composable
// » DashStatCard = kartu angka kecil (judul, nilai, ikon). Parameter: title, value, icon, modifier.
fun DashStatCard(title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    // » Card kartu:
    Card(
        // » mengikuti modifier dari pemanggil.
        modifier = modifier,
        // » Sudut bulat 16dp.
        shape = RoundedCornerShape(16.dp),
        // » Warna putih.
        colors = CardDefaults.cardColors(containerColor = Color.White),
        // » Bayangan 2dp.
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        // » Kolom isi: jarak dalam 14dp, jarak antar baris 6dp.
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            // » Kotak ikon:
            Box(
                // » Modifier:
                modifier = Modifier
                    // » ukuran 36dp,
                    .size(36.dp)
                    // » sudut bulat 10dp,
                    .clip(RoundedCornerShape(10.dp))
                    // » latar oranye transparan 12%.
                    .background(OrangePrimary.copy(alpha = 0.12f)),
                // » Ikon di tengah.
                contentAlignment = Alignment.Center
            ) {
                // » Ikon oranye ukuran 20dp.
                Icon(icon, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(20.dp))
            }
            // » Teks judul kecil (abu).
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            // » Teks nilai: gaya sub-judul kecil, sangat tebal, oranye gelap.
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold, color = OrangeDark)
        }
    }
}

// =====================  GRAFIK DASHBOARD  =====================
// INI KODE YANG MENGGAMBAR GRAFIK. Jenis: GRAFIK GARIS DUA WARNA (hijau = pemasukan, merah = pengeluaran) dengan isi gradasi.
// Digambar manual di Canvas, tanpa library. Input: labels (nama bulan), incomes, expenses (satu angka per bulan).
// LANGKAH GAMBAR:
//   1. maxVal = nilai terbesar di antara semua pemasukan dan pengeluaran (minimal 1 agar tidak dibagi nol). Semua tinggi dibandingkan dengan angka ini.
//   2. stepX = jarak mendatar antar bulan = lebar dibagi (jumlah bulan - 1). getY(nilai) = posisi vertikal (nilai besar = makin ke atas).
//   3. 4 garis bantu mendatar tipis (i dari 0 sampai 3).
//   4. GARIS HIJAU: Path menyambung titik pemasukan tiap bulan, isi gradasi di bawahnya, lalu titik bulat putih-hijau.
//   5. GARIS MERAH: sama untuk pengeluaran (titik hanya digambar bila nilainya > 0).
//   6. Label bulan ditulis di bawah tiap titik (drawText), dijaga agar tidak keluar tepi kanan kiri (coerceIn).
// Tidak ada sumbu angka di sisi kiri: grafik menunjukkan BENTUK naik turun, angka pastinya ada di kartu saldo.
// CARI-GRAFIK | Grafik garis pemasukan (hijau) vs pengeluaran (merah) dengan label bulan di bawah.
// » Penanda fungsi penggambar tampilan.
@Composable
// » WeeklyKasLineChart = fungsi yang MENGGAMBAR GRAFIK GARIS. Parameter:
fun WeeklyKasLineChart(
    // » labels = nama bulan (sumbu bawah),
    labels: List<String>,
    // » incomes = pemasukan tiap bulan,
    incomes: List<Int>,
    // » expenses = pengeluaran tiap bulan,
    expenses: List<Int>,
    // » modifier = ukuran/posisi dari pemanggil.
    modifier: Modifier = Modifier
) {
    // coerceAtLeast(1) mencegah pembagian dengan nol saat semua nilai 0
    // » maxVal = nilai terbesar dari semua pemasukan dan pengeluaran (minimal 1 agar tidak dibagi 0). Semua
    // » tinggi titik dibandingkan dengan angka ini.
    val maxVal = maxOf(incomes.maxOrNull() ?: 0, expenses.maxOrNull() ?: 0, 1).toFloat()
    // » Warna garis pemasukan: hijau.
    val greenColor = Color(0xFF22C55E)
    // » Warna garis pengeluaran: merah.
    val redColor = Color(0xFFEF4444)

    // » Canvas = kanvas kosong tempat menggambar garis dengan kode.
    Canvas(modifier = modifier) {
        // » Kalau kanvas terlalu kecil (lebar 0 atau tinggi tidak cukup untuk label), berhenti.
        if (size.width <= 0f || size.height <= 24.dp.toPx()) return@Canvas
        // » count = jumlah titik (jumlah bulan), minimal 1.
        val count = labels.size.coerceAtLeast(1)
        // » chartHeight = tinggi area grafik = tinggi kanvas dikurangi 24dp untuk label bulan.
        val chartHeight = size.height - 24.dp.toPx()
        // » chartTop = jarak kosong 8dp di atas grafik.
        val chartTop = 8.dp.toPx()
        // » Kalau tinggi grafik tidak valid, berhenti.
        if (chartHeight <= 0f) return@Canvas
        // » stepX = jarak mendatar antar bulan = lebar kanvas dibagi (jumlah bulan - 1). Kalau hanya 1 bulan, titik
        // » ditaruh di tengah.
        val stepX = if (count > 1) size.width / (count - 1).toFloat() else size.width / 2f

        // Mengubah nilai rupiah jadi posisi Y di layar: makin besar nilainya makin kecil Y (makin ke atas), karena Y layar dihitung dari atas.
        // Helper to get Y coordinate
        // » getY = fungsi kecil: mengubah nilai rupiah menjadi posisi tinggi (Y) di kanvas.
        fun getY(value: Int): Float {
            // » Rumus: dasar grafik dikurangi (nilai / nilai terbesar x tinggi grafik). Nilai besar = Y kecil = makin ke
            // » atas.
            return chartTop + chartHeight - (value / maxVal) * chartHeight
        }

        // Draw grid lines
        // Empat garis bantu mendatar tipis (abu-abu transparan).
        // » Ulangi 4 kali (i = 0 sampai 3) untuk menggambar 4 garis bantu mendatar.
        for (i in 0..3) {
            // » y = posisi garis ke-i, dibagi rata dari atas ke dasar grafik.
            val y = chartTop + chartHeight * i / 3f
            // » Gambar garis:
            drawLine(
                // » warna abu transparan 15%,
                color = Color.Gray.copy(alpha = 0.15f),
                // » mulai dari tepi kiri...
                start = Offset(0f, y),
                // » ...sampai tepi kanan,
                end = Offset(size.width, y),
                // » tebal 1.
                strokeWidth = 1f
            )
        }

        // Draw income line + fill
        // Garis pemasukan hanya digambar bila ada minimal 2 bulan. incomePath = garis, fillPath = isi gradasi di bawah garis.
        // » GARIS PEMASUKAN: hanya digambar kalau ada minimal 2 bulan.
        if (incomes.size >= 2) {
            // » incomePath = jalur garis hijau.
            val incomePath = Path()
            // » fillPath = jalur area isi gradasi di bawah garis.
            val fillPath = Path()
            // » Untuk setiap bulan (i = urutan, v = nilai):
            incomes.forEachIndexed { i, v ->
                // » x = posisi mendatar bulan ini (stepX x urutan), atau tengah kalau hanya satu titik.
                val x = if (count > 1) stepX * i else size.width / 2f
                // » y = posisi tinggi dari nilai pemasukan.
                val y = getY(v)
                // » Titik pertama: pindahkan pena ke titik itu (moveTo) di kedua jalur.
                if (i == 0) { incomePath.moveTo(x, y); fillPath.moveTo(x, y) }
                // » Titik berikutnya: tarik garis ke titik itu (lineTo) di kedua jalur.
                else { incomePath.lineTo(x, y); fillPath.lineTo(x, y) }
            }
            // Fill area
            // » Menutup area isi: tarik garis turun ke dasar di bulan terakhir...
            fillPath.lineTo(stepX * (incomes.size - 1), chartTop + chartHeight)
            // » ...lalu ke dasar di kiri...
            fillPath.lineTo(0f, chartTop + chartHeight)
            // » ...lalu tutup jalur (close) jadi bentuk area tertutup.
            fillPath.close()
            // » Gambar area isi:
            drawPath(
                // » memakai jalur fillPath,
                path = fillPath,
                // » dengan gradasi vertikal:
                brush = Brush.verticalGradient(
                    // » warna hijau transparan 25% di atas menuju hampir transparan di bawah,
                    colors = listOf(greenColor.copy(alpha = 0.25f), greenColor.copy(alpha = 0.02f)),
                    // » mulai dari atas grafik...
                    startY = chartTop,
                    // » ...sampai dasar grafik.
                    endY = chartTop + chartHeight
                )
            )
            // Line
            // » Gambar garis hijau:
            drawPath(
                // » memakai jalur incomePath,
                path = incomePath,
                // » warna hijau,
                color = greenColor,
                // » tebal 3dp, ujung dan sambungan membulat.
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            // Dots
            // » Untuk setiap bulan gambar titik:
            incomes.forEachIndexed { i, v ->
                // » hitung posisi x.
                val x = if (count > 1) stepX * i else size.width / 2f
                // » lingkaran hijau jari-jari 4dp di titik nilai...
                drawCircle(color = greenColor, radius = 4.dp.toPx(), center = Offset(x, getY(v)))
                // » ...lalu lingkaran putih lebih kecil (2dp) di tengahnya, sehingga tampak seperti titik berlubang.
                drawCircle(color = Color.White, radius = 2.dp.toPx(), center = Offset(x, getY(v)))
            }
        }

        // Draw expense line + fill
        // Garis pengeluaran, caranya sama dengan garis pemasukan tetapi berwarna merah.
        // » GARIS PENGELUARAN: hanya digambar kalau ada minimal 2 bulan.
        if (expenses.size >= 2) {
            // » expensePath = jalur garis merah.
            val expensePath = Path()
            // » fillPath = jalur area isi di bawah garis.
            val fillPath = Path()
            // » Untuk setiap bulan:
            expenses.forEachIndexed { i, v ->
                // » hitung x.
                val x = if (count > 1) stepX * i else size.width / 2f
                // » hitung y dari nilai pengeluaran.
                val y = getY(v)
                // » titik pertama: moveTo di kedua jalur.
                if (i == 0) { expensePath.moveTo(x, y); fillPath.moveTo(x, y) }
                // » titik berikutnya: lineTo di kedua jalur.
                else { expensePath.lineTo(x, y); fillPath.lineTo(x, y) }
            }
            // » Tarik garis turun ke dasar di bulan terakhir...
            fillPath.lineTo(stepX * (expenses.size - 1), chartTop + chartHeight)
            // » ...lalu ke kiri...
            fillPath.lineTo(0f, chartTop + chartHeight)
            // » ...lalu tutup jalur.
            fillPath.close()
            // » Gambar area isi:
            drawPath(
                // » jalur fillPath,
                path = fillPath,
                // » gradasi vertikal:
                brush = Brush.verticalGradient(
                    // » merah transparan 20% di atas menuju hampir transparan,
                    colors = listOf(redColor.copy(alpha = 0.20f), redColor.copy(alpha = 0.02f)),
                    // » mulai dari atas grafik...
                    startY = chartTop,
                    // » ...sampai dasar grafik.
                    endY = chartTop + chartHeight
                )
            )
            // » Gambar garis merah:
            drawPath(
                // » jalur expensePath,
                path = expensePath,
                // » warna merah,
                color = redColor,
                // » tebal 3dp, ujung dan sambungan membulat.
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            // » Untuk setiap bulan gambar titik:
            expenses.forEachIndexed { i, v ->
                // » hanya jika nilai pengeluaran > 0:
                if (v > 0) {
                    // » hitung x.
                    val x = if (count > 1) stepX * i else size.width / 2f
                    // » lingkaran merah jari-jari 4dp...
                    drawCircle(color = redColor, radius = 4.dp.toPx(), center = Offset(x, getY(v)))
                    // » ...dan lingkaran putih kecil di tengah.
                    drawCircle(color = Color.White, radius = 2.dp.toPx(), center = Offset(x, getY(v)))
                }
            }
        }

        // Nama bulan di dasar grafik (Jan, Feb, Mar, ...). Juni tidak muncul karena tidak ada transaksi (libur).
        // Label bulan di sumbu bawah
        // » labelPaint = "kuas teks" Android untuk menulis nama bulan:
        val labelPaint = android.graphics.Paint().apply {
            // » warna abu kebiruan (#6B7280),
            color = android.graphics.Color.parseColor("#6B7280")
            // » ukuran huruf 10dp,
            textSize = 10.dp.toPx()
            // » rata tengah terhadap titik,
            textAlign = android.graphics.Paint.Align.CENTER
            // » huruf dihaluskan (anti-aliasing).
            isAntiAlias = true
        }
        // » Untuk setiap nama bulan:
        labels.forEachIndexed { i, label ->
            // » x = posisi bulan, ditahan (coerceIn) agar tidak keluar tepi kiri/kanan kanvas.
            val x = (if (count > 1) stepX * i else size.width / 2f).coerceIn(12.dp.toPx(), size.width - 12.dp.toPx())
            // » Tulis nama bulan di dasar kanvas (4dp dari bawah).
            drawContext.canvas.nativeCanvas.drawText(label, x, size.height - 4.dp.toPx(), labelPaint)
        }
    }
}

// Satu baris label dan nilai di dialog rincian.
// » Penanda fungsi penggambar tampilan.
@Composable
// » DetailRow = satu baris label (kiri) dan nilai (kanan) di dialog rincian.
fun DetailRow(label: String, value: String) {
    // » Baris selebar layar, label dan nilai berjauhan.
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        // » Teks label: huruf kecil, abu.
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        // » Teks nilai: huruf kecil, tebal.
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
    }
}

// CARI-VALIDASI | Tampilan error + tombol Coba lagi yang dipakai beberapa layar.
// » Penanda fungsi penggambar tampilan.
@Composable
// » ErrorView = tampilan pesan error dengan tombol "Coba lagi" (dipakai beberapa layar).
fun ErrorView(error: String, onRetry: () -> Unit) {
    // » Kolom penuh layar:
    Column(
        // » penuhi layar.
        modifier = Modifier.fillMaxSize(),
        // » isi di tengah secara vertikal.
        verticalArrangement = Arrangement.Center,
        // » isi di tengah secara mendatar.
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // » Teks pesan error warna merah.
        Text(text = error, color = MaterialTheme.colorScheme.error)
        // » Jarak kosong 8dp.
        Spacer(modifier = Modifier.height(8.dp))
        // » Tombol "Coba lagi": saat ditekan menjalankan onRetry; warna oranye.
        Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)) {
            // » Tulisan tombol "Coba lagi".
            Text("Coba lagi")
        }
    }
}

// Mengubah 1389000 jadi "1.389.000". Layar menambahkan "Rp " sendiri di depannya.
// CARI-FORMAT-RUPIAH | Mengubah 1389000 menjadi "1.389.000" (tanpa "Rp").
// » formatRupiah = mengubah angka 1389000 menjadi teks "1.389.000". Layar menambah "Rp " sendiri di
// » depannya.
fun formatRupiah(value: Int): String {
    // » Format angka dengan pemisah koma (%,d), lalu tanda koma diganti titik supaya jadi gaya Indonesia.
    return String.format(java.util.Locale.US, "%,d", value).replace(',', '.')
}
