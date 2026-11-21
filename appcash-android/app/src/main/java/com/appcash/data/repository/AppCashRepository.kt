// ==========================================================================
// PETA FILE · AppCash (Kelompok 6) · AppCashRepository.kt
// Isi file : Pusat data offline: login, anggota, minggu kas, pembayaran, denda, pengeluaran, donasi, penyimpanan.
// Penanda  : CARI-PUSAT-DATA, CARI-DENDA, CARI-SIMPAN-DATA, CARI-LOGIN, CARI-ANGGOTA, CARI-DATA-AWAL, CARI-NOMINAL-KAS, CARI-PEMBAYARAN, CARI-PENGELUARAN, CARI-DONASI, CARI-DISPENSASI, CARI-SALDO, CARI-PROFIL, CARI-GRAFIK, CARI-VALIDASI, CARI-TIDAK-DIPAKAI
// Cara cari: VS Code Ctrl+Shift+F (Android Studio Ctrl+Shift+F) lalu ketik
//            "CARI-" untuk semua penanda, atau salah satu nama di baris Penanda.
// ==========================================================================
//  BACA INI DULU : INI "OTAK" APPCASH
//  Semua data dan rumus ada di file ini. Layar hanya meminta hasilnya (tanpa server, semua offline).
//  Urutan isi file:
//    1. Konstanta       : DENDA_RATE_PERCENT = 5 (persen), SNAPSHOT_KEY (kunci simpanan)
//    2. Login admin     : ADMIN_EMAIL, ADMIN_PASSWORD_HASH (hash SHA-256, bukan password asli)
//    3. Penyimpanan     : persist() menyimpan ke HP, restore() memuat kembali (Gson + SharedPreferences "appcash_db")
//    4. DATA AWAL       : membersList (33 siswa), datesList (37 minggu M01..M37), seedStatus (centang awal),
//                         expensesList (24 pengeluaran), donationsList (2 donasi), exemptions (dispensasi BI/PKL)
//    5. Sesi dan login  : login(), logout(), getCurrentUser()
//    6. DENDA           : parseDateLabel(), calculateDenda(), dendaForWeeks()
//    7. getDashboard()  : SALDO dan data GRAFIK
//    8. Pembayaran      : getPayments(), savePayments(), updatePaymentStatus()
//    9. Pengeluaran     : getExpenses(), addExpense(), updateExpense(), deleteExpense()
//   10. Anggota/Config  : updateMember(), updateProfile(), updateConfig()
//  JAWABAN CEPAT:
//   - Saldo     = saldo awal + (jumlah centang lunas x Rp5.000) + donasi - total pengeluaran = Rp1.389.000 (getDashboard)
//   - Denda     = Rp5.000 x 5% x bulan penuh telat, untuk SETIAP minggu yang belum lunas (calculateDenda)
//   - Data      = disimpan di HP sebagai JSON, belum ada server atau Firebase
//   - Pembayaran = tabel centang: paymentRecords[idSiswa][minggu] = true/false
package com.appcash.data.repository

import android.content.Context
import com.appcash.data.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.security.MessageDigest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

// Satu-satunya tempat data. Dibuat SEKALI di AppCashApplication supaya data dan sesi tidak hilang saat layar diputar.
// CARI-PUSAT-DATA | Semua layar mengambil dan mengubah data lewat kelas ini (tanpa server).
class AppCashRepository(private val context: Context) {

    companion object {
        // CARI-DENDA | Persentase denda per bulan keterlambatan (5 = 5%). Ubah angka ini untuk mengganti tarif denda.
        // Tarif denda 5 persen per bulan keterlambatan. Mau ganti tarif: ubah angka ini saja.
        const val DENDA_RATE_PERCENT = 5
        // Versi data: dinaikkan saat data awal diganti agar data demo lama tidak dipakai lagi
        // CARI-SIMPAN-DATA | Nama kunci penyimpanan. Naikkan versinya (mis. _v3) bila data awal diganti, supaya HP memuat data baru.
        private const val SNAPSHOT_KEY = "snapshot_v3_pengurus"
    }

    // --- SESSION STATE ---
    // Sesi login yang sedang berjalan (hanya di memori): peran (admin/user), id siswa yang login, status login, email admin.
    private var currentRole: String? = null
    private var currentMemberId: Int? = null
    private var isLoggedIn: Boolean = false
    private var adminEmail: String = ""

    // --- ADMIN CREDENTIALS ---
    // Password disimpan sebagai hash SHA-256 (bukan plaintext). Nilai = sha256("admin123")
    // CARI-LOGIN | Akun admin: admin@gmail.com / admin123. Password disimpan sebagai hash SHA-256 di ADMIN_PASSWORD_HASH;
    //   kalau password diganti, isi hash baru (sha256 dari password baru).
    private val ADMIN_EMAIL = "admin@gmail.com"
    private val ADMIN_PASSWORD_HASH = "240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9"

    // Mengacak teks jadi 64 karakter hex yang tidak bisa dibalik. Password yang diketik diacak lalu DIBANDINGKAN dengan ADMIN_PASSWORD_HASH,
    // jadi password asli ("admin123") tidak pernah tersimpan di kode.
    // Mengubah teks (password) menjadi kode hash SHA-256 supaya password asli tidak tersimpan.
    private fun sha256(text: String): String =
        MessageDigest.getInstance("SHA-256").digest(text.toByteArray())
            .joinToString("") { "%02x".format(it) }

    // prefs = laci penyimpanan aplikasi di HP bernama "appcash_db". gson = penerjemah objek <-> teks JSON.
    // --- PERSISTENCE (SharedPreferences + Gson) ---
    // Data tetap tersimpan walau aplikasi ditutup / layar diputar.
    private val prefs = context.getSharedPreferences("appcash_db", Context.MODE_PRIVATE)
    private val gson = Gson()

    // "Foto" seluruh data (anggota, centang bayar, pengeluaran, riwayat, config, donasi) yang disimpan sebagai satu teks JSON.
    private data class Snapshot(
        val members: List<Member>,
        val payments: Map<String, Map<String, Boolean>>,
        val expenses: List<Expense>,
        val history: List<PaymentRecord>,
        val config: Config,
        val donations: List<Donation>? = null
    )

    // Nomor versi data. Setiap persist() menaikkannya. Layar memantau angka ini (repository.version.collectAsState) dan
    // menjalankan LaunchedEffect ulang untuk memuat data baru. Inilah cara layar otomatis ikut berubah setelah ada perubahan.
    // Sinyal perubahan data: layar memuat ulang otomatis setiap kali nilainya naik
    private val _version = MutableStateFlow(0)
    val version: StateFlow<Int> = _version.asStateFlow()
    // Menaikkan nomor versi data supaya semua layar yang mengamati ikut memuat ulang.
    fun refresh() { _version.value++ }

    // Dipanggil setiap data berubah: naikkan versi (layar memuat ulang), bungkus semua data jadi Snapshot, simpan JSON ke HP.
    // CARI-SIMPAN-DATA | Menyimpan semua data ke HP (Gson -> SharedPreferences). Dipanggil setiap ada perubahan.
    // TANYA-GURU: Data disimpan di mana? Di HP (SharedPreferences, format JSON lewat Gson). Belum online, Firebase masih rancangan.
    private fun persist() {
        _version.value++
        val snap = Snapshot(membersList, paymentRecords, expensesList, paymentHistoryList, configData, donationsList)
        prefs.edit().putString(SNAPSHOT_KEY, gson.toJson(snap)).apply()
    }

    // Dipanggil saat aplikasi dibuka. Bila ada JSON tersimpan -> isi ulang semua daftar. Bila tidak ada atau rusak -> false,
    // lalu init memakai DATA AWAL (seedPayments). Kunci SNAPSHOT_KEY dinaikkan bila data awal diganti.
    // CARI-SIMPAN-DATA | Memuat data tersimpan saat aplikasi dibuka; bila kosong/rusak dipakai data awal.
    private fun restore(): Boolean {
        val json = prefs.getString(SNAPSHOT_KEY, null) ?: return false
        return try {
            val snap = gson.fromJson<Snapshot>(json, object : TypeToken<Snapshot>() {}.type) ?: return false
            membersList.clear(); membersList.addAll(snap.members)
            paymentRecords.clear()
            snap.payments.forEach { (k, v) -> paymentRecords[k] = v.toMutableMap() }
            expensesList.clear(); expensesList.addAll(snap.expenses)
            paymentHistoryList.clear(); paymentHistoryList.addAll(snap.history)
            configData = snap.config
            snap.donations?.let { donationsList.clear(); donationsList.addAll(it) }
            true
        } catch (e: Exception) {
            false // data korup → pakai data awal
        }
    }

    // DATA AWAL SISWA: 33 anak XII PPLG. Urutan kolom Member: id, nama, NIS, jabatan, bio, umur, nomor WA, foto.
    // --- IN-MEMORY STANDALONE DATABASE (XII PPLG) ---
    // CARI-ANGGOTA CARI-DATA-AWAL | Daftar 33 siswa XII PPLG: id, nama, NIS, jabatan, bio, umur, no. WA.
    //   Foto siswa diambil dari res/drawable/student_<id>.
    private val membersList = mutableListOf(
        Member(1,  "Boyke Vilano Hamonangan Sihite",          "2026-PPLG-001", "Anggota", "Siswa XII PPLG", "17 Tahun", "", ""),
        Member(2,  "Bintang Leonita Christya Renata",         "2026-PPLG-002", "Anggota", "Siswa XII PPLG", "17 Tahun", "", ""),
        Member(3,  "Carolina Timuthy Janggur",                "2026-PPLG-003", "Anggota", "Siswa XII PPLG", "17 Tahun", "", ""),
        Member(4,  "Dewa Gede Dalem Oka Adnyana Sandi",      "2026-PPLG-004", "Anggota", "Siswa XII PPLG", "17 Tahun", "", ""),
        Member(5,  "Galistan Ramadhan Kurnia Taunaes",        "2026-PPLG-005", "Ketua Kelas", "Ketua Kelas XII PPLG", "17 Tahun", "", ""),
        Member(6,  "Gede Agus Wira Darma Putra",              "2026-PPLG-006", "Bendahara 1", "Bendahara 1 kas kelas XII PPLG · Android & Web Full-Stack Developer", "17 Tahun", "6285600487433", ""),
        Member(7,  "I Gede Abi Wirya Dinata",                 "2026-PPLG-007", "Anggota", "Siswa XII PPLG", "17 Tahun", "", ""),
        Member(8,  "I Gede Darma Suptiawan",                  "2026-PPLG-008", "Anggota", "Siswa XII PPLG", "17 Tahun", "", ""),
        Member(9,  "I Komang Raditya Putra",                  "2026-PPLG-009", "Anggota", "Siswa XII PPLG", "17 Tahun", "", ""),
        Member(10, "I Komang Riski Setiawan",                 "2026-PPLG-010", "Anggota", "Siswa XII PPLG", "17 Tahun", "", ""),
        Member(11, "I Nyoman Gede Arta Wiguna",               "2026-PPLG-011", "Anggota", "Siswa XII PPLG", "17 Tahun", "", ""),
        Member(12, "I Putu Dika Laksmana Putra",              "2026-PPLG-012", "Anggota", "Siswa XII PPLG", "17 Tahun", "", ""),
        Member(13, "I Putu Ditya Artha Wijaya",               "2026-PPLG-013", "Anggota", "Siswa XII PPLG", "17 Tahun", "", ""),
        Member(14, "I Putu Pande Andika",                     "2026-PPLG-014", "Anggota", "Manajemen Keuangan & Data Analyst PPLG", "16 Tahun", "", ""),
        Member(15, "I Putu Suyoga Mahendra",                  "2026-PPLG-015", "Anggota", "Siswa XII PPLG", "17 Tahun", "", ""),
        Member(16, "I Wayan Bagus Putrawan",                  "2026-PPLG-016", "Anggota", "Siswa XII PPLG", "17 Tahun", "", ""),
        Member(17, "I Wayan Pasek Kevin Ariadi",              "2026-PPLG-017", "Anggota", "Siswa XII PPLG", "17 Tahun", "", ""),
        Member(18, "Kadek Yuda Prasetya",                     "2026-PPLG-018", "Anggota", "Siswa XII PPLG", "17 Tahun", "", ""),
        Member(19, "Kadek Yuni Callista Putri Dewi",          "2026-PPLG-019", "Anggota", "Siswa XII PPLG", "17 Tahun", "", ""),
        Member(20, "Komang Diah Putri Pratiwi",               "2026-PPLG-020", "Anggota", "Siswa XII PPLG", "17 Tahun", "", ""),
        Member(21, "Luh Ria Mirasih",                         "2026-PPLG-021", "Bendahara 2", "Bendahara 2 kas kelas XII PPLG", "17 Tahun", "", ""),
        Member(22, "Ni Kadek Adelia Cahya Kencana Putri",     "2026-PPLG-022", "Anggota", "UI/UX Designer XII PPLG", "16 Tahun", "", ""),
        Member(23, "Ni Kadek Lina Antika Dewi",               "2026-PPLG-023", "Anggota", "Siswa XII PPLG", "17 Tahun", "", ""),
        Member(24, "Ni Komang Kirana Paramita Ardanari",      "2026-PPLG-024", "Anggota", "Siswa XII PPLG", "17 Tahun", "", ""),
        Member(25, "Ni Komang Septiarini",                    "2026-PPLG-025", "Anggota", "Siswa XII PPLG", "17 Tahun", "", ""),
        Member(26, "Ni Luh Putu Kesya Astri Melani",          "2026-PPLG-026", "Anggota", "Siswa XII PPLG", "17 Tahun", "", ""),
        Member(27, "Ni Putu Cahaya Lestari Dewi",             "2026-PPLG-027", "Sekretaris 1", "Sekretaris 1 XII PPLG", "17 Tahun", "", ""),
        Member(28, "Ni Putu Intan Lestari Darmayanti",        "2026-PPLG-028", "Sekretaris 2", "Sekretaris 2 XII PPLG", "17 Tahun", "", ""),
        Member(29, "Okta Pradipta Attala Dzaki",              "2026-PPLG-029", "Wakil Ketua", "Wakil Ketua Kelas XII PPLG", "17 Tahun", "", ""),
        Member(30, "Putu Bayu Satria Wangsa Bukian",          "2026-PPLG-030", "Anggota", "Front-end Enthusiast XII PPLG", "17 Tahun", "", ""),
        Member(31, "Putu Nanda Lindia Maharani",              "2026-PPLG-031", "Anggota", "Siswa XII PPLG", "17 Tahun", "", ""),
        Member(32, "Putu Putri Cahyani",                      "2026-PPLG-032", "Anggota", "Siswa XII PPLG", "17 Tahun", "", ""),
        Member(33, "Raditya Rondi",                           "2026-PPLG-033", "Anggota", "Siswa XII PPLG", "17 Tahun", "", "")
    )

    // 37 minggu kas (tiap Senin). "M01 (05 Jan)" = minggu ke-1 tanggal 05 Jan. Juni tidak ada karena libur.
    // Tabel pembayaran memakai teks ini sebagai kunci kolom, jadi penulisannya harus persis sama di semua tempat.
    // 37 minggu kas sesuai buku kas XII PPLG (Jan–Mei & Jul–Okt 2026, Juni libur)
    // CARI-DATA-AWAL CARI-NOMINAL-KAS | Daftar minggu kas (setiap Senin). Tambah minggu baru di akhir dengan format "M38 (02 Nov)";
    //   singkatan bulan harus sesuai monthMap (Jan..Des, Agustus = Ags).
    private val datesList = mutableListOf(
        "M01 (05 Jan)", "M02 (12 Jan)", "M03 (19 Jan)", "M04 (26 Jan)",
        "M05 (02 Feb)", "M06 (09 Feb)", "M07 (16 Feb)", "M08 (23 Feb)",
        "M09 (02 Mar)", "M10 (09 Mar)", "M11 (16 Mar)", "M12 (23 Mar)",
        "M13 (06 Apr)", "M14 (13 Apr)", "M15 (20 Apr)", "M16 (27 Apr)",
        "M17 (04 Mei)", "M18 (11 Mei)", "M19 (18 Mei)", "M20 (25 Mei)",
        "M21 (06 Jul)", "M22 (13 Jul)", "M23 (20 Jul)", "M24 (27 Jul)",
        "M25 (03 Ags)", "M26 (10 Ags)", "M27 (17 Ags)", "M28 (24 Ags)",
        "M29 (31 Ags)", "M30 (07 Sep)", "M31 (14 Sep)", "M32 (21 Sep)",
        "M33 (28 Sep)", "M34 (05 Okt)", "M35 (12 Okt)", "M36 (19 Okt)",
        "M37 (26 Okt)"
    )

    // Centang awal tiap siswa dari buku kas asli. Satu string 37 karakter per siswa, urut sama dengan datesList.
    // "1" = lunas, "0" = belum. Contoh siswa 5: semuanya 1 = lunas penuh semua minggu.
    // Status bayar per anggota (urut sesuai datesList): 1 = lunas, 0 = belum. Sumber: buku kas XII PPLG per 20 Nov 2026
    // CARI-DATA-AWAL CARI-PEMBAYARAN | Status bayar awal dari buku kas asli: satu string per siswa, 1 = lunas, 0 = belum, urut sesuai datesList.
    private val seedStatus = mapOf(
        1 to "1111111111111111111111111111111110000",
        2 to "1111111111111111111100000000000000000",
        3 to "1111111111111111111111111111100000000",
        4 to "1111111111111111111111111111100000000",
        5 to "1111111111111111111111111111111111111",
        6 to "1111111111111111111111111111100000000",
        7 to "1111111111111111111111110000000000000",
        8 to "1111111111111111111111111000000000000",
        9 to "1111111111111111111111111000000000000",
        10 to "1111111111111111111111000000000000000",
        11 to "1111111111111111111111111000000000000",
        12 to "1111111111111111111111111111000000000",
        13 to "1111111111111111111111000000000000000",
        14 to "1111111111111111111111110000000000000",
        15 to "1111111111111111111111111100000000000",
        16 to "1111111111111111111111111111111111111",
        17 to "1111111111111111111111111111111111111",
        18 to "1111111111111111111111100000000000000",
        19 to "1111111111111111111111111111000000000",
        20 to "1111111111111111111111111100000000000",
        21 to "1111111111111111111111111111111111111",
        22 to "1111111111111111111111110000000000000",
        23 to "1111111111111111111111111111100000000",
        24 to "1111111111111111111111111111111111111",
        25 to "1111111111111111111111111111110000000",
        26 to "1111111111111111111111111111111110000",
        27 to "1111111111111111111111111111100000000",
        28 to "1111111111111111111111111111000000000",
        29 to "1111111111111111111111111111111111111",
        30 to "1111111111111111111111000000000000000",
        31 to "1111111111111111111111111111000000000",
        32 to "1111111111111111111111111100000000000",
        33 to "1111111111111111111111110000000000000"
    )

    // 24 pengeluaran asli buku kas (belanja kelas, Saraswati, Ogoh-ogoh, dll). Jumlahnya Rp3.496.000.
    // Format tanggal yyyy-MM-dd. Kategori: Perlengkapan, Kegiatan, Akademik, Lainnya.
    // CARI-DATA-AWAL CARI-PENGELUARAN | 24 pengeluaran asli (id, keterangan, nominal, tanggal yyyy-MM-dd, kategori).
    private val expensesList = mutableListOf(
        Expense(30, "Selasa Literasi", 59000, "2026-11-10", "Akademik"),
        Expense(29, "Saraswati", 995000, "2026-10-30", "Kegiatan"),
        Expense(28, "Literasi Bahasa Inggris", 47000, "2026-09-30", "Akademik"),
        Expense(23, "Kamis Literasi", 36000, "2026-09-10", "Akademik"),
        Expense(22, "MAP PLASTIK BIG", 17000, "2026-07-26", "Perlengkapan"),
        Expense(21, "SNOWMAN BOARD MARKER BLACK", 9000, "2026-07-26", "Perlengkapan"),
        Expense(20, "Jagung, Sayur, Kacang", 40000, "2026-05-01", "Kegiatan"),
        Expense(16, "Jahit Bendera", 35000, "2026-05-01", "Kegiatan"),
        Expense(15, "Property Drama", 200000, "2026-05-01", "Kegiatan"),
        Expense(14, "Tirai & Jahit", 255000, "2026-04-01", "Kegiatan"),
        Expense(13, "Latihan Masak", 93000, "2026-04-01", "Kegiatan"),
        Expense(12, "Saraswati", 440000, "2026-04-01", "Kegiatan"),
        Expense(11, "Property Drama", 300000, "2026-03-01", "Kegiatan"),
        Expense(10, "Ogoh - Ogoh", 140000, "2026-03-01", "Kegiatan"),
        Expense(9, "Literasi Pagi", 30000, "2026-03-01", "Akademik"),
        Expense(19, "Dompet kas", 6500, "2026-02-01", "Perlengkapan"),
        Expense(18, "Pulpen kas", 4000, "2026-02-01", "Perlengkapan"),
        Expense(17, "Tugas Agama", 62000, "2026-02-01", "Akademik"),
        Expense(8, "Kanvas", 67000, "2026-02-01", "Akademik"),
        Expense(7, "Ogoh - Ogoh", 100000, "2026-02-01", "Kegiatan"),
        Expense(6, "Ogoh - Ogoh", 200000, "2026-02-01", "Kegiatan"),
        Expense(5, "Uang Keluar", 50000, "2026-02-01", "Lainnya"),
        Expense(2, "Buku Kas", 10500, "2026-02-01", "Perlengkapan"),
        Expense(1, "Ogoh - Ogoh", 300000, "2026-02-01", "Kegiatan")
    )

    // Donasi = pemasukan di luar kas mingguan. Total Rp220.000 (Bu Jallu 200.000 dan Ni Komang Septiarini 20.000).
    // CARI-DONASI | Donasi dihitung sebagai pemasukan di luar kas mingguan.
    private val donationsList = mutableListOf(
        Donation(1, "Bu Jallu", 200000, "2026-02-01"),
        Donation(2, "Ni Komang Septiarini", 20000, "2026-07-21")
    )

    // TABEL PEMBAYARAN INTI: paymentRecords["3"]["M05 (02 Feb)"] = true artinya siswa id 3 sudah bayar minggu M05.
    // Saldo, denda, status lunas, dan grafik semua dihitung dari tabel ini.
    private val paymentRecords = mutableMapOf<String, MutableMap<String, Boolean>>()

    // Dispensasi: siswa yang sedang BI atau PKL tidak wajib bayar pada rentang tanggal tertentu. Minggu itu tidak dihitung wajib dan tidak didenda.
    // Siswa yang sedang BI/PKL tidak wajib bayar kas pada rentang tanggal tersebut
    private data class Exemption(val memberId: Int, val from: LocalDate, val until: LocalDate, val reason: String)
    // CARI-DISPENSASI | Siswa yang bebas kas pada rentang tanggal tertentu (mis. BI/PKL).
    //   Tambah: Exemption(idSiswa, LocalDate dari, LocalDate sampai, "alasan").
    private val exemptions = listOf(Exemption(2, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 9, 20), "BI/PKL"))

    // true bila minggu `date` jatuh dalam rentang dispensasi siswa itu. Dipakai tampilan (label Dispensasi) dan rumus denda.
    // CARI-DISPENSASI | Dipakai layar Kas & rumus denda untuk melewati minggu dispensasi.
    /** true jika anggota dibebaskan dari kas pada minggu [date] (mis. sedang BI/PKL). */
    fun isExempt(memberId: Int, date: String): Boolean {
        val d = parseDateLabel(date) ?: return false
        return exemptions.any { it.memberId == memberId && !d.isBefore(it.from) && !d.isAfter(it.until) }
    }

    // Payment history for new payment input feature
    private val paymentHistoryList = mutableListOf<PaymentRecord>()

    // CARI-NOMINAL-KAS CARI-SALDO | kasAmount = iuran per minggu, previousBalance = saldo awal, realBalance = uang fisik di peti kas.
    // Pengaturan aktif: iuran Rp5.000, saldo awal 0, saldo fisik Rp1.389.000, kelas XII PPLG.
    private var configData = Config(
        kasAmount       = 5000,
        previousBalance = 0,
        realBalance     = 1389000,
        className       = "XII PPLG",
        year            = "2026/2027"
    )

    // Dijalankan sekali saat repository dibuat: bila tidak ada data tersimpan, isi tabel pembayaran dari DATA AWAL.
    init {
        if (!restore()) seedPayments()
    }

    // Mengubah string seedStatus ("1101...") tiap siswa menjadi tabel centang per minggu.
    // CARI-DATA-AWAL | Mengubah seedStatus menjadi tabel pembayaran saat aplikasi pertama kali dipasang.
    private fun seedPayments() {
        membersList.forEach { member ->
            val status = seedStatus[member.id].orEmpty()
            paymentRecords[member.id.toString()] = datesList.mapIndexed { i, date -> date to (status.getOrNull(i) == '1') }
                .toMap().toMutableMap()
        }
    }

    // --- SESSION MANAGEMENT ---
    suspend fun getRole(): String? = currentRole
    // true bila ada sesi login aktif (dipakai untuk melewati layar login).
    suspend fun hasToken(): Boolean = isLoggedIn
    // id siswa yang sedang login (null bila admin).
    suspend fun getCurrentMemberId(): Int? = currentMemberId

    // Hitung (minggu lunas, minggu wajib) satu siswa. Minggu dispensasi tidak dihitung wajib. Dipakai kartu "Status Kas Anda".
    // Menghitung (minggu lunas, minggu wajib) satu siswa, minggu dispensasi tidak dihitung wajib.
    suspend fun getPaymentStatusForMember(memberId: Int): Result<Pair<Int, Int>> = withContext(Dispatchers.IO) {
        val memberPayments = paymentRecords[memberId.toString()] ?: emptyMap()
        val dueWeeks = datesList.filterNot { isExempt(memberId, it) }
        val totalWeeks = dueWeeks.size
        val paidWeeks = dueWeeks.count { date -> memberPayments[date] == true }
        Result.success(Pair(paidWeeks, totalWeeks))
    }

    // LOGIN. Dua jalur:
    //   role "admin" : email harus admin@gmail.com DAN sha256(password) sama dengan ADMIN_PASSWORD_HASH
    //   role "user"  : siswa cukup memilih nama (memberId). Tidak ada password untuk siswa.
    // Hasilnya Result.success atau Result.failure(pesan). Pesan gagal ditampilkan di LoginScreen.
    // CARI-LOGIN | role "admin" = email + password; role "user" = siswa cukup memilih nama.
    // TANYA-GURU: Bedanya admin dan siswa? Admin login email + password (hash SHA-256), siswa cukup pilih nama.
    // Admin bisa centang pembayaran, kelola pengeluaran, dan melihat rekap denda. Siswa hanya melihat miliknya.
    suspend fun login(role: String, email: String? = null, password: String? = null, memberId: Int? = null, @Suppress("UNUSED_PARAMETER") name: String? = null): Result<LoginResponse> = withContext(Dispatchers.IO) {
        if (role == "admin") {
            if (email?.trim()?.lowercase() == ADMIN_EMAIL && password != null && sha256(password) == ADMIN_PASSWORD_HASH) {
                currentRole = "admin"
                isLoggedIn = true
                adminEmail = email
                Result.success(LoginResponse(token = "admin_token", role = "admin"))
            } else {
                Result.failure(Exception("Email atau password salah"))
            }
        } else {
            // User login via member selection
            if (memberId != null && membersList.any { it.id == memberId }) {
                currentRole = "user"
                currentMemberId = memberId
                isLoggedIn = true
                Result.success(LoginResponse(token = "user_token_$memberId", role = "user"))
            } else {
                Result.failure(Exception("Pilih anggota terlebih dahulu"))
            }
        }
    }

    // Menyimpan siswa yang dipilih di layar login.
    suspend fun setMemberId(id: Int) = withContext(Dispatchers.IO) {
        currentMemberId = id
    }

    // Keluar: menghapus peran dan id siswa dari sesi, lalu menyimpan ke HP.
    suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        currentRole = null
        currentMemberId = null
        isLoggedIn = false
        adminEmail = ""
        Result.success(Unit)
    }

    // Profil yang login: admin dibuat virtual ("Administrator"), siswa diambil dari membersList.
    // --- PROFILE ---
    suspend fun getCurrentUser(): Result<UserProfile> = withContext(Dispatchers.IO) {
        if (currentRole == "admin") {
            Result.success(UserProfile(
                name = "Administrator",
                role = "Admin",
                email = adminEmail,
                nis = "-",
                phone = "-",
                bio = "Administrator AppCash XII PPLG",
                umur = "-"
            ))
        } else {
            val member = membersList.find { it.id == currentMemberId }
            if (member != null) {
                Result.success(UserProfile(
                    name = member.name,
                    role = member.role,
                    email = "",
                    nis = member.nis,
                    phone = member.phone,
                    bio = member.bio,
                    umur = member.umur
                ))
            } else {
                Result.failure(Exception("User not found"))
            }
        }
    }

    // CARI-PROFIL | Menyimpan perubahan nama, no. HP, dan bio siswa yang sedang login.
    suspend fun updateProfile(name: String, phone: String, bio: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (currentRole == "admin") {
            // Admin profile is virtual, no member to update
            Result.success(Unit)
        } else {
            val idx = membersList.indexOfFirst { it.id == currentMemberId }
            if (idx >= 0) {
                membersList[idx] = membersList[idx].copy(name = name.trim(), phone = phone.trim(), bio = bio.trim())
                persist()
                Result.success(Unit)
            } else {
                Result.failure(Exception("User not found"))
            }
        }
    }

    // --- DENDA (PENALTY) CALCULATION ---
    // 5% per month based on device date vs payment due date

    // Kamus singkatan bulan di label minggu ("Ags" = Agustus) ke angka bulan, untuk mengubah label jadi tanggal.
    private val monthMap = mapOf(
        "Jan" to 1, "Feb" to 2, "Mar" to 3, "Apr" to 4,
        "Mei" to 5, "Jun" to 6, "Jul" to 7, "Ags" to 8,
        "Sep" to 9, "Okt" to 10, "Nov" to 11, "Des" to 12
    )

    // Mengubah label "M05 (02 Feb)" jadi tanggal 2 Februari 2026 (LocalDate). Tahun dikunci 2026. Hasil null bila format salah.
    /**
     * Parse date label like "M01 (05 Jan)" → LocalDate(2026, 1, 5)
     */
    private fun parseDateLabel(label: String): LocalDate? {
        val regex = Regex("""\((\d{2})\s+(\w+)\)""")
        val match = regex.find(label) ?: return null
        val day = match.groupValues[1].toIntOrNull() ?: return null
        val monthStr = match.groupValues[2]
        val month = monthMap[monthStr] ?: return null
        return try { LocalDate.of(2026, month, day) } catch (e: Exception) { null }
    }

    // RUMUS DENDA (baca pelan):
    //   untuk SETIAP minggu yang belum dicentang dan bukan dispensasi:
    //     monthsLate = jumlah BULAN PENUH antara tanggal minggu itu dan hari ini (ChronoUnit.MONTHS)
    //     bila monthsLate > 0 : denda minggu itu = kas (5.000) x 5 / 100 x monthsLate = Rp250 x monthsLate
    //   total = jumlah denda semua minggu tersebut.
    // Contoh: 3 minggu belum bayar yang telat 2 bulan penuh -> 3 x 250 x 2 = Rp1.500.
    // Telat kurang dari 1 bulan belum didenda. Bila total 0 hasilnya null (tidak punya denda).
    // CARI-DENDA | Rumus: kas x 5% x n untuk setiap minggu belum lunas; n = jumlah bulan penuh sejak tanggal kas.
    //   Minggu dispensasi dan keterlambatan < 1 bulan tidak didenda.
    /**
     * Rumus denda: denda per minggu = kas x 5% x bulan_terlambat.
     * Contoh: kas Rp5.000, telat 2 bulan → 5.000 x 5% x 2 = Rp500.
     */
    // TANYA-GURU: Bagaimana denda dihitung? Kas Rp5.000 x 5% = Rp250 x n bulan penuh terlambat, per minggu yang belum lunas.
    // Minggu dispensasi (BI/PKL) dilewati. Contoh: 3 minggu telat 2 bulan = 3 x 250 x 2 = Rp1.500.
    private fun calculateDenda(member: Member, today: LocalDate = LocalDate.now()): DendaInfo? {
        val memberPayments = paymentRecords[member.id.toString()] ?: emptyMap()
        var total = 0
        val unpaidMonths = mutableSetOf<Int>()
        datesList.forEach { date ->
            if (memberPayments[date] == true || isExempt(member.id, date)) return@forEach
            val due = parseDateLabel(date) ?: return@forEach
            val monthsLate = ChronoUnit.MONTHS.between(due, today).toInt()
            if (monthsLate > 0) {
                total += configData.kasAmount * DENDA_RATE_PERCENT * monthsLate / 100
                unpaidMonths.add(due.monthValue)
            }
        }
        if (total <= 0) return null
        return DendaInfo(
            memberId = member.id,
            memberName = member.name,
            unpaidMonths = unpaidMonths.size,
            dendaPercentage = unpaidMonths.size * DENDA_RATE_PERCENT.toDouble(),
            dendaAmount = total
        )
    }

    // Denda hanya untuk minggu-minggu yang DICENTANG saat membayar (rumus sama dengan calculateDenda tetapi per minggu pilihan).
    // Dipakai dialog Bayar Kas untuk menampilkan total yang harus dibayar.
    // CARI-DENDA CARI-PEMBAYARAN | Denda hanya untuk minggu yang dicentang saat membayar (dipakai dialog Bayar Kas).
    /** Denda untuk minggu-minggu tertentu saja (dipakai saat membayar sebagian tunggakan). */
    fun dendaForWeeks(dates: Collection<String>, today: LocalDate = LocalDate.now()): Int =
        dates.sumOf { date ->
            val due = parseDateLabel(date) ?: return@sumOf 0
            configData.kasAmount * DENDA_RATE_PERCENT * ChronoUnit.MONTHS.between(due, today).toInt().coerceAtLeast(0) / 100
        }

    // Rekap denda semua siswa. Hanya ditampilkan di Dashboard akun admin.
    suspend fun getDendaForAllMembers(): Result<List<DendaInfo>> = withContext(Dispatchers.IO) {
        Result.success(membersList.mapNotNull { calculateDenda(it) })
    }

    // Denda satu siswa, dipakai di Profil dan kartu Status Kas Anda.
    suspend fun getDendaForMember(memberId: Int): Result<DendaInfo?> = withContext(Dispatchers.IO) {
        val member = membersList.find { it.id == memberId }
            ?: return@withContext Result.failure(Exception("Member not found"))
        Result.success(calculateDenda(member))
    }

    // DASHBOARD: dua hal dihitung di sini.
    // 1) SALDO:
    //      totalKasIncome = (jumlah semua centang lunas) x Rp5.000
    //      saldo = saldo awal + totalKasIncome + total donasi - total pengeluaran = Rp1.389.000
    // 2) DATA GRAFIK per bulan: tiap bulan punya dua angka, [0] pemasukan (kas + donasi) dan [1] pengeluaran.
    //      kas dikelompokkan lewat tanggal minggu (parseDateLabel), donasi dan pengeluaran lewat tanggalnya.
    //      sortedMapOf menjaga urutan bulan dari Januari. Bulan tanpa transaksi (Juni) tidak muncul.
    //      Hasilnya dikirim sebagai weeklyLabels, weeklyKasIncome, weeklyExpenses ke WeeklyKasLineChart.
    // latestPaid = jumlah siswa yang lunas di minggu terakhir yang tanggalnya sudah lewat.
    // --- DASHBOARD ---
    // CARI-SALDO CARI-GRAFIK | Saldo = saldo awal + (jumlah centang lunas x kas) + donasi - total pengeluaran.
    //   Juga menyiapkan data grafik per bulan (pemasukan vs pengeluaran).
    // TANYA-GURU: Dari mana saldo? saldo awal + (centang lunas x Rp5.000) + donasi - pengeluaran. Hasil data asli Rp1.389.000.
    suspend fun getDashboard(): Result<Dashboard> = withContext(Dispatchers.IO) {
        var totalPaymentsCount = 0
        datesList.forEach { date -> totalPaymentsCount += paymentRecords.values.count { it[date] == true } }
        val totalKasIncome = totalPaymentsCount * configData.kasAmount
        val totalDonation = donationsList.sumOf { it.amount }
        val totalExpenseAmount = expensesList.sumOf { it.amount }
        val netBalance = configData.previousBalance + totalKasIncome + totalDonation - totalExpenseAmount

        // Grafik per bulan: pemasukan = kas + donasi, pengeluaran = sesuai tanggal transaksi
        val months = sortedMapOf<YearMonth, IntArray>()
        // bucket = ambil "keranjang" dua angka milik bulan dari tanggal d (dibuat bila belum ada).
        // Kelompokkan pemasukan dan pengeluaran per bulan untuk grafik Dashboard.
        fun bucket(d: LocalDate?) = d?.let { months.getOrPut(YearMonth.from(it)) { IntArray(2) } }
        datesList.forEach { date ->
            bucket(parseDateLabel(date))?.let { it[0] += paymentRecords.values.count { r -> r[date] == true } * configData.kasAmount }
        }
        donationsList.forEach { d -> bucket(runCatching { LocalDate.parse(d.date) }.getOrNull())?.let { it[0] += d.amount } }
        expensesList.forEach { e -> bucket(runCatching { LocalDate.parse(e.date) }.getOrNull())?.let { it[1] += e.amount } }
        val shortMonth = listOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Ags", "Sep", "Okt", "Nov", "Des")

        val latestDate = datesList.lastOrNull { d -> parseDateLabel(d)?.let { !it.isAfter(LocalDate.now()) } ?: false } ?: datesList.lastOrNull() ?: ""
        val latestPaidCount = paymentRecords.values.count { it[latestDate] == true }

        Result.success(
            Dashboard(
                totalBalance      = netBalance,
                previousBalance   = configData.previousBalance,
                kasAmount         = configData.kasAmount,
                totalKasIncome    = totalKasIncome,
                totalPayments     = totalPaymentsCount,
                totalPaymentDates = datesList.size,
                totalMembers      = membersList.size,
                totalExpenseAmount= totalExpenseAmount,
                totalExpenseCount = expensesList.size,
                latestPaid        = latestPaidCount,
                weeklyLabels      = months.keys.map { shortMonth[it.monthValue - 1] },
                weeklyKasIncome   = months.values.map { it[0] },
                weeklyExpenses    = months.values.map { it[1] },
                totalDonationAmount = totalDonation,
                realBalance       = configData.realBalance
            )
        )
    }

    // Daftar donasi (ikut dihitung sebagai pemasukan).
    suspend fun getDonations(): Result<List<Donation>> = withContext(Dispatchers.IO) { Result.success(donationsList.toList()) }

    // Daftar 33 siswa untuk layar Anggota dan pilihan nama di Login.
    suspend fun getMembers(): Result<List<Member>> = withContext(Dispatchers.IO) { Result.success(membersList.toList()) }

    // Tabel centang lunas semua siswa per minggu (salinan, supaya layar tidak mengubah data asli).
    suspend fun getPayments(): Result<PaymentsWrapper> = withContext(Dispatchers.IO) {
        // Deep copy: UI tidak boleh mengubah map internal repository secara langsung
        Result.success(PaymentsWrapper(dates = datesList.toList(), records = paymentRecords.mapValues { it.value.toMap() }))
    }

    // Menyimpan perubahan centang. Dipakai admin (edit minggu, tambah pembayaran) dan siswa (konfirmasi setelah kirim WA).
    // persist() membuat semua layar memuat ulang sehingga saldo ikut berubah.
    // CARI-PEMBAYARAN | Menyimpan centang lunas per siswa per minggu.
    suspend fun savePayments(wrapper: PaymentsWrapper): Result<Unit> = withContext(Dispatchers.IO) {
        wrapper.records.forEach { (memberId, datesMap) ->
            val existing = paymentRecords[memberId] ?: mutableMapOf()
            datesMap.forEach { (date, paid) -> existing[date] = paid }
            paymentRecords[memberId] = existing
        }
        persist()
        Result.success(Unit)
    }

    // Mengubah satu centang (siswa, minggu) menjadi lunas atau belum, lalu menyimpan.
    suspend fun updatePaymentStatus(memberId: Int, date: String, paid: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        val memberMap = paymentRecords[memberId.toString()] ?: mutableMapOf()
        memberMap[date] = paid
        paymentRecords[memberId.toString()] = memberMap
        persist()
        Result.success(Unit)
    }

    // Daftar pengeluaran untuk layar Pengeluaran Kas.
    suspend fun getExpenses(): Result<List<Expense>> = withContext(Dispatchers.IO) { Result.success(expensesList.toList()) }

    // Tambah pengeluaran. Validasi: keterangan tidak boleh kosong dan nominal harus > 0. Id baru = id terbesar + 1.
    // Ditaruh di urutan paling atas (add(0, ...)), lalu saldo otomatis turun.
    // CARI-PENGELUARAN CARI-VALIDASI | Tambah pengeluaran; keterangan wajib diisi dan nominal > 0.
    suspend fun addExpense(req: ExpenseRequest): Result<IdResponse> = withContext(Dispatchers.IO) {
        if (req.description.isBlank()) return@withContext Result.failure(Exception("Keterangan wajib diisi"))
        if (req.amount <= 0) return@withContext Result.failure(Exception("Nominal harus lebih dari 0"))
        val newId = (expensesList.maxOfOrNull { it.id } ?: 0) + 1
        expensesList.add(0, Expense(newId, req.description.trim(), req.amount, req.date, req.category))
        persist()
        Result.success(IdResponse(newId))
    }

    // Hapus pengeluaran (admin). Karena saldo dihitung ulang dari daftar, saldo otomatis naik lagi.
    // Menghapus satu pengeluaran (khusus admin), saldo otomatis naik lagi.
    suspend fun deleteExpense(id: Int): Result<Unit> = withContext(Dispatchers.IO) {
        expensesList.removeAll { it.id == id }
        persist()
        Result.success(Unit)
    }

    // CARI-PENGELUARAN | Edit pengeluaran yang sudah ada.
    suspend fun updateExpense(id: Int, description: String, amount: Int, date: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (description.isBlank()) return@withContext Result.failure(Exception("Keterangan wajib diisi"))
        if (amount <= 0) return@withContext Result.failure(Exception("Nominal harus lebih dari 0"))
        val idx = expensesList.indexOfFirst { it.id == id }
        if (idx >= 0) {
            val old = expensesList[idx]
            expensesList[idx] = old.copy(description = description.trim(), amount = amount, date = date)
            persist()
            Result.success(Unit)
        } else {
            Result.failure(Exception("Expense not found"))
        }
    }

    // CARI-ANGGOTA | Edit data siswa (khusus admin).
    suspend fun updateMember(id: Int, name: String, nis: String, role: String, bio: String, phone: String): Result<Unit> = withContext(Dispatchers.IO) {
        val idx = membersList.indexOfFirst { it.id == id }
        if (idx >= 0) {
            membersList[idx] = membersList[idx].copy(name = name.trim(), nis = nis.trim(), role = role, bio = bio.trim(), phone = phone.trim())
            persist()
            Result.success(Unit)
        } else {
            Result.failure(Exception("Member not found"))
        }
    }

    // Pengaturan kas: nominal per minggu, saldo awal, saldo riil, nama kelas.
    suspend fun getConfig(): Result<Config> = withContext(Dispatchers.IO) { Result.success(configData) }

    // Mengubah pengaturan kas (belum ada layarnya, nilai diatur tim di kode).
    suspend fun updateConfig(req: ConfigRequest): Result<Unit> = withContext(Dispatchers.IO) {
        configData = configData.copy(
            kasAmount       = req.kasAmount ?: configData.kasAmount,
            previousBalance = req.previousBalance ?: configData.previousBalance,
            realBalance     = req.realBalance ?: configData.realBalance,
            className       = req.className ?: configData.className,
            year            = req.year ?: configData.year
        )
        persist()
        Result.success(Unit)
    }

    // Untuk PaymentInputScreen yang belum dipasang di menu. Riwayat ini TIDAK ikut menghitung saldo (saldo hanya dari tabel centang).
    // --- Payment Input Feature ---
    // CARI-TIDAK-DIPAKAI | Untuk PaymentInputScreen yang belum dipasang di navigasi; tidak ikut dihitung di saldo.
    suspend fun addPaymentRecord(memberId: Int, amount: Int, date: String, note: String): Result<PaymentRecord> = withContext(Dispatchers.IO) {
        val member = membersList.find { it.id == memberId } ?: return@withContext Result.failure(Exception("Member not found"))
        if (amount <= 0) return@withContext Result.failure(Exception("Nominal harus lebih dari 0"))
        val newId = (paymentHistoryList.maxOfOrNull { it.id } ?: 0) + 1
        val record = PaymentRecord(id = newId, memberId = memberId, memberName = member.name, amount = amount, date = date, note = note)
        paymentHistoryList.add(0, record)
        persist()
        Result.success(record)
    }

    // Riwayat input pembayaran manual (layar belum dipasang di menu).
    suspend fun getPaymentHistory(): Result<List<PaymentRecord>> = withContext(Dispatchers.IO) {
        Result.success(paymentHistoryList.toList())
    }
}
