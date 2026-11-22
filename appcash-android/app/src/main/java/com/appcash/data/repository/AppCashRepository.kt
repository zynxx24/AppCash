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
// » Alamat folder file ini. Folder "repository" = tempat menyimpan dan mengolah data.
package com.appcash.data.repository

// » Context = pegangan ke lingkungan aplikasi; dibutuhkan untuk membuka penyimpanan HP (SharedPreferences).
import android.content.Context
// » Memanggil semua bentuk data (Member, Expense, Donation, Config, dll) dari Models.kt.
import com.appcash.data.model.*
// » Gson = penerjemah objek <-> teks JSON.
import com.google.gson.Gson
// » TypeToken = alat bantu agar Gson tahu tipe data lengkap saat membaca JSON (mis. daftar berisi Member).
import com.google.gson.reflect.TypeToken
// » MessageDigest = alat bawaan Java untuk membuat hash (kode acak) dari teks; dipakai untuk password.
import java.security.MessageDigest
// » Dispatchers = pilihan "jalur kerja"; Dispatchers.IO = jalur khusus pekerjaan data.
import kotlinx.coroutines.Dispatchers
// » MutableStateFlow = wadah angka/nilai yang bisa diamati layar; kalau nilainya berubah, layar yang
// » mengamati otomatis tahu.
import kotlinx.coroutines.flow.MutableStateFlow
// » StateFlow = versi hanya-baca dari wadah tadi (untuk dibaca layar).
import kotlinx.coroutines.flow.StateFlow
// » asStateFlow = mengubah MutableStateFlow menjadi StateFlow hanya-baca.
import kotlinx.coroutines.flow.asStateFlow
// » withContext = berpindah jalur kerja (ke IO) untuk satu blok pekerjaan lalu kembali.
import kotlinx.coroutines.withContext
// » LocalDate = tipe tanggal (tahun-bulan-tanggal) tanpa jam.
import java.time.LocalDate
// » YearMonth = tipe "bulan dalam tahun" (mis. Maret 2026).
import java.time.YearMonth
// » ChronoUnit = satuan waktu untuk menghitung selisih (mis. berapa bulan antara dua tanggal).
import java.time.temporal.ChronoUnit

// Satu-satunya tempat data. Dibuat SEKALI di AppCashApplication supaya data dan sesi tidak hilang saat layar diputar.
// CARI-PUSAT-DATA | Semua layar mengambil dan mengubah data lewat kelas ini (tanpa server).
// » class AppCashRepository = "OTAK" aplikasi. context = pegangan aplikasi (private = hanya dipakai di dalam
// » kelas ini).
class AppCashRepository(private val context: Context) {

    // » companion object = bagian milik kelas (bukan milik tiap objek), isinya konstanta.
    companion object {
        // CARI-DENDA | Persentase denda per bulan keterlambatan (5 = 5%). Ubah angka ini untuk mengganti tarif denda.
        // Tarif denda 5 persen per bulan keterlambatan. Mau ganti tarif: ubah angka ini saja.
        // » const val = nilai tetap yang tidak bisa diubah saat aplikasi berjalan. 5 = tarif denda 5 persen per
        // » bulan keterlambatan.
        const val DENDA_RATE_PERCENT = 5
        // Versi data: dinaikkan saat data awal diganti agar data demo lama tidak dipakai lagi
        // CARI-SIMPAN-DATA | Nama kunci penyimpanan. Naikkan versinya (mis. _v3) bila data awal diganti, supaya HP memuat data baru.
        // » Kunci/label tempat menyimpan data di penyimpanan HP. Kalau data awal diganti, ubah nama kunci ini (mis.
        // » _v4) agar HP membaca data baru, bukan data lama.
        private const val SNAPSHOT_KEY = "snapshot_v3_pengurus"
    }

    // --- SESSION STATE ---
    // Sesi login yang sedang berjalan (hanya di memori): peran (admin/user), id siswa yang login, status login, email admin.
    // » var = nilai yang bisa berubah. currentRole = peran yang sedang login ("admin" atau "user"). Tanda "?" =
    // » boleh kosong (belum login).
    private var currentRole: String? = null
    // » currentMemberId = id siswa yang sedang login (kosong kalau admin atau belum login).
    private var currentMemberId: Int? = null
    // » isLoggedIn = true kalau sudah login.
    private var isLoggedIn: Boolean = false
    // » adminEmail = email admin yang sedang login.
    private var adminEmail: String = ""

    // --- ADMIN CREDENTIALS ---
    // Password disimpan sebagai hash SHA-256 (bukan plaintext). Nilai = sha256("admin123")
    // CARI-LOGIN | Akun admin: admin@gmail.com / admin123. Password disimpan sebagai hash SHA-256 di ADMIN_PASSWORD_HASH;
    //   kalau password diganti, isi hash baru (sha256 dari password baru).
    // » Email admin yang valid: admin@gmail.com.
    private val ADMIN_EMAIL = "admin@gmail.com"
    // » Hash SHA-256 dari password admin. Password asli tidak ditulis di kode; yang ditulis hanya hasil acaknya
    // » (64 karakter).
    private val ADMIN_PASSWORD_HASH = "240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9"

    // Mengacak teks jadi 64 karakter hex yang tidak bisa dibalik. Password yang diketik diacak lalu DIBANDINGKAN dengan ADMIN_PASSWORD_HASH,
    // jadi password asli ("admin123") tidak pernah tersimpan di kode.
    // Mengubah teks (password) menjadi kode hash SHA-256 supaya password asli tidak tersimpan.
    // » Fungsi sha256: mengubah teks jadi hash 64 karakter. Dipakai membandingkan password yang diketik dengan
    // » ADMIN_PASSWORD_HASH.
    private fun sha256(text: String): String =
        // » Membuat alat hash SHA-256, lalu menghitung hash dari byte-byte teks (text.toByteArray()).
        MessageDigest.getInstance("SHA-256").digest(text.toByteArray())
            // » Mengubah tiap byte menjadi 2 karakter hex (%02x) lalu menyambung semuanya jadi satu teks panjang.
            .joinToString("") { "%02x".format(it) }

    // prefs = laci penyimpanan aplikasi di HP bernama "appcash_db". gson = penerjemah objek <-> teks JSON.
    // --- PERSISTENCE (SharedPreferences + Gson) ---
    // Data tetap tersimpan walau aplikasi ditutup / layar diputar.
    // » prefs = "laci" penyimpanan di HP bernama "appcash_db". MODE_PRIVATE = hanya aplikasi ini yang bisa
    // » membukanya.
    private val prefs = context.getSharedPreferences("appcash_db", Context.MODE_PRIVATE)
    // » gson = penerjemah objek <-> JSON.
    private val gson = Gson()

    // "Foto" seluruh data (anggota, centang bayar, pengeluaran, riwayat, config, donasi) yang disimpan sebagai satu teks JSON.
    // » Snapshot = "foto" seluruh data yang disimpan sebagai satu teks JSON. private = hanya dipakai di file
    // » ini.
    private data class Snapshot(
        // » Daftar semua siswa.
        val members: List<Member>,
        // » Tabel centang pembayaran: id siswa -> (minggu -> lunas?).
        val payments: Map<String, Map<String, Boolean>>,
        // » Daftar semua pengeluaran.
        val expenses: List<Expense>,
        // » Riwayat pembayaran (fitur input pembayaran).
        val history: List<PaymentRecord>,
        // » Pengaturan kas (iuran, saldo, dll).
        val config: Config,
        // » Daftar donasi. Boleh kosong (null) karena data lama tidak punya donasi.
        val donations: List<Donation>? = null
    )

    // Nomor versi data. Setiap persist() menaikkannya. Layar memantau angka ini (repository.version.collectAsState) dan
    // menjalankan LaunchedEffect ulang untuk memuat data baru. Inilah cara layar otomatis ikut berubah setelah ada perubahan.
    // Sinyal perubahan data: layar memuat ulang otomatis setiap kali nilainya naik
    // » _version = nomor versi data, mulai dari 0. Setiap data berubah, angka ini naik. Private karena hanya
    // » repository boleh mengubahnya.
    private val _version = MutableStateFlow(0)
    // » version = versi hanya-baca yang diamati layar. Begitu naik, layar memuat ulang data otomatis.
    val version: StateFlow<Int> = _version.asStateFlow()
    // Menaikkan nomor versi data supaya semua layar yang mengamati ikut memuat ulang.
    // » refresh() = menaikkan nomor versi secara manual supaya layar memuat ulang (value++ berarti tambah 1).
    fun refresh() { _version.value++ }

    // Dipanggil setiap data berubah: naikkan versi (layar memuat ulang), bungkus semua data jadi Snapshot, simpan JSON ke HP.
    // CARI-SIMPAN-DATA | Menyimpan semua data ke HP (Gson -> SharedPreferences). Dipanggil setiap ada perubahan.
    // TANYA-GURU: Data disimpan di mana? Di HP (SharedPreferences, format JSON lewat Gson). Belum online, Firebase masih rancangan.
    // » persist() = SIMPAN. Dipanggil tiap kali ada perubahan data.
    private fun persist() {
        // » Naikkan nomor versi agar layar memuat ulang data terbaru.
        _version.value++
        // » Bungkus semua daftar data menjadi satu objek Snapshot.
        val snap = Snapshot(membersList, paymentRecords, expensesList, paymentHistoryList, configData, donationsList)
        // » Ubah Snapshot jadi teks JSON (gson.toJson), simpan di laci HP dengan kunci SNAPSHOT_KEY (putString),
        // » lalu terapkan (apply).
        prefs.edit().putString(SNAPSHOT_KEY, gson.toJson(snap)).apply()
    }

    // Dipanggil saat aplikasi dibuka. Bila ada JSON tersimpan -> isi ulang semua daftar. Bila tidak ada atau rusak -> false,
    // lalu init memakai DATA AWAL (seedPayments). Kunci SNAPSHOT_KEY dinaikkan bila data awal diganti.
    // CARI-SIMPAN-DATA | Memuat data tersimpan saat aplikasi dibuka; bila kosong/rusak dipakai data awal.
    // » restore() = MUAT. Mengisi ulang semua data dari HP. Hasil true = berhasil, false = tidak ada data
    // » tersimpan.
    private fun restore(): Boolean {
        // » Ambil teks JSON dari laci HP. Kalau tidak ada (null), langsung keluar dengan false ("?: return false").
        val json = prefs.getString(SNAPSHOT_KEY, null) ?: return false
        // » try = coba proses di bawah; kalau error loncat ke catch.
        return try {
            // » Ubah JSON kembali jadi objek Snapshot. Kalau hasilnya kosong, keluar dengan false.
            val snap = gson.fromJson<Snapshot>(json, object : TypeToken<Snapshot>() {}.type) ?: return false
            // » Kosongkan daftar siswa lalu isi dengan siswa dari Snapshot.
            membersList.clear(); membersList.addAll(snap.members)
            // » Kosongkan tabel centang lama.
            paymentRecords.clear()
            // » Salin tabel centang dari Snapshot satu per satu (toMutableMap = bisa diubah).
            snap.payments.forEach { (k, v) -> paymentRecords[k] = v.toMutableMap() }
            // » Kosongkan daftar pengeluaran lalu isi dari Snapshot.
            expensesList.clear(); expensesList.addAll(snap.expenses)
            // » Kosongkan riwayat lalu isi dari Snapshot.
            paymentHistoryList.clear(); paymentHistoryList.addAll(snap.history)
            // » Ganti pengaturan dengan yang tersimpan.
            configData = snap.config
            // » Kalau ada donasi tersimpan, kosongkan daftar donasi lalu isi dengan itu.
            snap.donations?.let { donationsList.clear(); donationsList.addAll(it) }
            // » Semua sukses: kembalikan true.
            true
        // » catch = kalau ada error saat membaca JSON (misal data rusak)...
        } catch (e: Exception) {
            // » ...kembalikan false, nanti dipakai data awal.
            false // data korup → pakai data awal
        }
    }

    // DATA AWAL SISWA: 33 anak XII PPLG. Urutan kolom Member: id, nama, NIS, jabatan, bio, umur, nomor WA, foto.
    // --- IN-MEMORY STANDALONE DATABASE (XII PPLG) ---
    // CARI-ANGGOTA CARI-DATA-AWAL | Daftar 33 siswa XII PPLG: id, nama, NIS, jabatan, bio, umur, no. WA.
    //   Foto siswa diambil dari res/drawable/student_<id>.
    // » membersList = daftar 33 siswa XII PPLG. mutableListOf = daftar yang bisa diubah. Urutan isi Member: id,
    // » nama, NIS, jabatan, bio, umur, nomor WhatsApp, alamat foto.
    private val membersList = mutableListOf(
        // » Siswa nomor 1, jabatan Anggota.
        Member(1,  "Boyke Vilano Hamonangan Sihite",          "2026-PPLG-001", "Anggota", "Siswa XII PPLG", "17 Tahun", "", ""),
        Member(2,  "Bintang Leonita Christya Renata",         "2026-PPLG-002", "Anggota", "Siswa XII PPLG", "17 Tahun", "", ""),
        Member(3,  "Carolina Timuthy Janggur",                "2026-PPLG-003", "Anggota", "Siswa XII PPLG", "17 Tahun", "", ""),
        Member(4,  "Dewa Gede Dalem Oka Adnyana Sandi",      "2026-PPLG-004", "Anggota", "Siswa XII PPLG", "17 Tahun", "", ""),
        // » Siswa nomor 5 (Galistan): Ketua Kelas.
        Member(5,  "Galistan Ramadhan Kurnia Taunaes",        "2026-PPLG-005", "Ketua Kelas", "Ketua Kelas XII PPLG", "17 Tahun", "", ""),
        // » Siswa nomor 6 (Gede Agus Wira Darma Putra): Bendahara 1, nomor WA 6285600487433 dipakai untuk pengingat
        // » bayar.
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
        // » Siswa nomor 21 (Luh Ria Mirasih): Bendahara 2.
        Member(21, "Luh Ria Mirasih",                         "2026-PPLG-021", "Bendahara 2", "Bendahara 2 kas kelas XII PPLG", "17 Tahun", "", ""),
        Member(22, "Ni Kadek Adelia Cahya Kencana Putri",     "2026-PPLG-022", "Anggota", "UI/UX Designer XII PPLG", "16 Tahun", "", ""),
        Member(23, "Ni Kadek Lina Antika Dewi",               "2026-PPLG-023", "Anggota", "Siswa XII PPLG", "17 Tahun", "", ""),
        Member(24, "Ni Komang Kirana Paramita Ardanari",      "2026-PPLG-024", "Anggota", "Siswa XII PPLG", "17 Tahun", "", ""),
        Member(25, "Ni Komang Septiarini",                    "2026-PPLG-025", "Anggota", "Siswa XII PPLG", "17 Tahun", "", ""),
        Member(26, "Ni Luh Putu Kesya Astri Melani",          "2026-PPLG-026", "Anggota", "Siswa XII PPLG", "17 Tahun", "", ""),
        // » Siswa nomor 27: Sekretaris 1.
        Member(27, "Ni Putu Cahaya Lestari Dewi",             "2026-PPLG-027", "Sekretaris 1", "Sekretaris 1 XII PPLG", "17 Tahun", "", ""),
        // » Siswa nomor 28: Sekretaris 2.
        Member(28, "Ni Putu Intan Lestari Darmayanti",        "2026-PPLG-028", "Sekretaris 2", "Sekretaris 2 XII PPLG", "17 Tahun", "", ""),
        // » Siswa nomor 29 (Okta Pradipta): Wakil Ketua.
        Member(29, "Okta Pradipta Attala Dzaki",              "2026-PPLG-029", "Wakil Ketua", "Wakil Ketua Kelas XII PPLG", "17 Tahun", "", ""),
        Member(30, "Putu Bayu Satria Wangsa Bukian",          "2026-PPLG-030", "Anggota", "Front-end Enthusiast XII PPLG", "17 Tahun", "", ""),
        Member(31, "Putu Nanda Lindia Maharani",              "2026-PPLG-031", "Anggota", "Siswa XII PPLG", "17 Tahun", "", ""),
        Member(32, "Putu Putri Cahyani",                      "2026-PPLG-032", "Anggota", "Siswa XII PPLG", "17 Tahun", "", ""),
        // » Siswa nomor 33, terakhir.
        Member(33, "Raditya Rondi",                           "2026-PPLG-033", "Anggota", "Siswa XII PPLG", "17 Tahun", "", "")
    // » Tutup daftar siswa.
    )

    // 37 minggu kas (tiap Senin). "M01 (05 Jan)" = minggu ke-1 tanggal 05 Jan. Juni tidak ada karena libur.
    // Tabel pembayaran memakai teks ini sebagai kunci kolom, jadi penulisannya harus persis sama di semua tempat.
    // 37 minggu kas sesuai buku kas XII PPLG (Jan–Mei & Jul–Okt 2026, Juni libur)
    // CARI-DATA-AWAL CARI-NOMINAL-KAS | Daftar minggu kas (setiap Senin). Tambah minggu baru di akhir dengan format "M38 (02 Nov)";
    //   singkatan bulan harus sesuai monthMap (Jan..Des, Agustus = Ags).
    // » datesList = 37 minggu kas (tiap Senin), teks "M01 (05 Jan)" = minggu ke-1 tanggal 05 Jan. Dipakai
    // » sebagai KUNCI kolom tabel pembayaran, jadi penulisan harus persis sama di semua tempat.
    private val datesList = mutableListOf(
        // » Januari: M01 sampai M04.
        "M01 (05 Jan)", "M02 (12 Jan)", "M03 (19 Jan)", "M04 (26 Jan)",
        // » Februari: M05 sampai M08.
        "M05 (02 Feb)", "M06 (09 Feb)", "M07 (16 Feb)", "M08 (23 Feb)",
        // » Maret: M09 sampai M12.
        "M09 (02 Mar)", "M10 (09 Mar)", "M11 (16 Mar)", "M12 (23 Mar)",
        // » April: M13 sampai M16.
        "M13 (06 Apr)", "M14 (13 Apr)", "M15 (20 Apr)", "M16 (27 Apr)",
        // » Mei: M17 sampai M20.
        "M17 (04 Mei)", "M18 (11 Mei)", "M19 (18 Mei)", "M20 (25 Mei)",
        // » Juli: M21 sampai M24 (Juni libur, tidak ada kas).
        "M21 (06 Jul)", "M22 (13 Jul)", "M23 (20 Jul)", "M24 (27 Jul)",
        // » Agustus: M25 sampai M28 ("Ags" = Agustus).
        "M25 (03 Ags)", "M26 (10 Ags)", "M27 (17 Ags)", "M28 (24 Ags)",
        // » M29 (31 Agustus), lalu September M30 sampai M32.
        "M29 (31 Ags)", "M30 (07 Sep)", "M31 (14 Sep)", "M32 (21 Sep)",
        // » M33 (28 Sep), lalu Oktober M34 sampai M36.
        "M33 (28 Sep)", "M34 (05 Okt)", "M35 (12 Okt)", "M36 (19 Okt)",
        // » M37 (26 Okt) = minggu terakhir.
        "M37 (26 Okt)"
    )

    // Centang awal tiap siswa dari buku kas asli. Satu string 37 karakter per siswa, urut sama dengan datesList.
    // "1" = lunas, "0" = belum. Contoh siswa 5: semuanya 1 = lunas penuh semua minggu.
    // Status bayar per anggota (urut sesuai datesList): 1 = lunas, 0 = belum. Sumber: buku kas XII PPLG per 20 Nov 2026
    // CARI-DATA-AWAL CARI-PEMBAYARAN | Status bayar awal dari buku kas asli: satu string per siswa, 1 = lunas, 0 = belum, urut sesuai datesList.
    // » seedStatus = centang awal tiap siswa dari buku kas asli. "id siswa to string". Satu string berisi 37
    // » karakter, urut sama dengan datesList. "1" = LUNAS, "0" = belum bayar.
    private val seedStatus = mapOf(
        // » Siswa 1: 33 minggu lunas pertama, 4 minggu terakhir belum.
        1 to "1111111111111111111111111111111110000",
        // » Siswa 2: hanya 20 minggu lunas, lalu 0 (minggu Juli-Sep dibebaskan BI/PKL).
        2 to "1111111111111111111100000000000000000",
        3 to "1111111111111111111111111111100000000",
        4 to "1111111111111111111111111111100000000",
        // » Siswa 5 (Ketua Kelas): semua 37 minggu lunas.
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
        // » Siswa 16: semua 37 minggu lunas.
        16 to "1111111111111111111111111111111111111",
        // » Siswa 17: semua 37 minggu lunas.
        17 to "1111111111111111111111111111111111111",
        18 to "1111111111111111111111100000000000000",
        19 to "1111111111111111111111111111000000000",
        20 to "1111111111111111111111111100000000000",
        // » Siswa 21 (Bendahara 2): semua 37 minggu lunas.
        21 to "1111111111111111111111111111111111111",
        22 to "1111111111111111111111110000000000000",
        23 to "1111111111111111111111111111100000000",
        // » Siswa 24: semua 37 minggu lunas.
        24 to "1111111111111111111111111111111111111",
        25 to "1111111111111111111111111111110000000",
        26 to "1111111111111111111111111111111110000",
        27 to "1111111111111111111111111111100000000",
        28 to "1111111111111111111111111111000000000",
        // » Siswa 29 (Wakil Ketua): semua 37 minggu lunas.
        29 to "1111111111111111111111111111111111111",
        30 to "1111111111111111111111000000000000000",
        31 to "1111111111111111111111111111000000000",
        32 to "1111111111111111111111111100000000000",
        // » Siswa 33: lunas sampai minggu ke-25, sisanya belum.
        33 to "1111111111111111111111110000000000000"
    // » Tutup tabel seedStatus.
    )

    // 24 pengeluaran asli buku kas (belanja kelas, Saraswati, Ogoh-ogoh, dll). Jumlahnya Rp3.496.000.
    // Format tanggal yyyy-MM-dd. Kategori: Perlengkapan, Kegiatan, Akademik, Lainnya.
    // CARI-DATA-AWAL CARI-PENGELUARAN | 24 pengeluaran asli (id, keterangan, nominal, tanggal yyyy-MM-dd, kategori).
    // » expensesList = 24 pengeluaran asli buku kas. Urutan Expense: id, keterangan, jumlah rupiah, tanggal
    // » (yyyy-MM-dd), kategori.
    private val expensesList = mutableListOf(
        // » Id 30: Selasa Literasi, Rp59.000, 10 Nov 2026, kategori Akademik.
        Expense(30, "Selasa Literasi", 59000, "2026-11-10", "Akademik"),
        // » Id 29: Saraswati, Rp995.000 (pengeluaran terbesar), 30 Okt 2026, kategori Kegiatan.
        Expense(29, "Saraswati", 995000, "2026-10-30", "Kegiatan"),
        // » Id 28: Literasi Bahasa Inggris Rp47.000.
        Expense(28, "Literasi Bahasa Inggris", 47000, "2026-09-30", "Akademik"),
        // » Id 23: Kamis Literasi Rp36.000.
        Expense(23, "Kamis Literasi", 36000, "2026-09-10", "Akademik"),
        // » Id 22: Map plastik Rp17.000, kategori Perlengkapan.
        Expense(22, "MAP PLASTIK BIG", 17000, "2026-07-26", "Perlengkapan"),
        // » Id 21: Spidol (marker) Rp9.000.
        Expense(21, "SNOWMAN BOARD MARKER BLACK", 9000, "2026-07-26", "Perlengkapan"),
        // » Id 20: Jagung, sayur, kacang Rp40.000, kegiatan.
        Expense(20, "Jagung, Sayur, Kacang", 40000, "2026-05-01", "Kegiatan"),
        // » Id 16: Jahit bendera Rp35.000.
        Expense(16, "Jahit Bendera", 35000, "2026-05-01", "Kegiatan"),
        // » Id 15: Property drama Rp200.000.
        Expense(15, "Property Drama", 200000, "2026-05-01", "Kegiatan"),
        // » Id 14: Tirai & jahit Rp255.000.
        Expense(14, "Tirai & Jahit", 255000, "2026-04-01", "Kegiatan"),
        // » Id 13: Latihan masak Rp93.000.
        Expense(13, "Latihan Masak", 93000, "2026-04-01", "Kegiatan"),
        // » Id 12: Saraswati Rp440.000 (April).
        Expense(12, "Saraswati", 440000, "2026-04-01", "Kegiatan"),
        // » Id 11: Property drama Rp300.000.
        Expense(11, "Property Drama", 300000, "2026-03-01", "Kegiatan"),
        // » Id 10: Ogoh-ogoh Rp140.000.
        Expense(10, "Ogoh - Ogoh", 140000, "2026-03-01", "Kegiatan"),
        // » Id 9: Literasi pagi Rp30.000.
        Expense(9, "Literasi Pagi", 30000, "2026-03-01", "Akademik"),
        // » Id 19: Dompet kas Rp6.500.
        Expense(19, "Dompet kas", 6500, "2026-02-01", "Perlengkapan"),
        // » Id 18: Pulpen kas Rp4.000.
        Expense(18, "Pulpen kas", 4000, "2026-02-01", "Perlengkapan"),
        // » Id 17: Tugas agama Rp62.000.
        Expense(17, "Tugas Agama", 62000, "2026-02-01", "Akademik"),
        // » Id 8: Kanvas Rp67.000.
        Expense(8, "Kanvas", 67000, "2026-02-01", "Akademik"),
        // » Id 7: Ogoh-ogoh Rp100.000.
        Expense(7, "Ogoh - Ogoh", 100000, "2026-02-01", "Kegiatan"),
        // » Id 6: Ogoh-ogoh Rp200.000.
        Expense(6, "Ogoh - Ogoh", 200000, "2026-02-01", "Kegiatan"),
        // » Id 5: Uang keluar Rp50.000, kategori Lainnya.
        Expense(5, "Uang Keluar", 50000, "2026-02-01", "Lainnya"),
        // » Id 2: Buku kas Rp10.500.
        Expense(2, "Buku Kas", 10500, "2026-02-01", "Perlengkapan"),
        // » Id 1: Ogoh-ogoh Rp300.000 (pengeluaran pertama), tanpa koma karena item terakhir.
        Expense(1, "Ogoh - Ogoh", 300000, "2026-02-01", "Kegiatan")
    // » Tutup daftar pengeluaran. Jumlah semuanya Rp3.496.000.
    )

    // Donasi = pemasukan di luar kas mingguan. Total Rp220.000 (Bu Jallu 200.000 dan Ni Komang Septiarini 20.000).
    // CARI-DONASI | Donasi dihitung sebagai pemasukan di luar kas mingguan.
    // » donationsList = daftar donasi (pemasukan di luar iuran mingguan).
    private val donationsList = mutableListOf(
        // » Donasi #1: Bu Jallu Rp200.000 pada 1 Feb 2026.
        Donation(1, "Bu Jallu", 200000, "2026-02-01"),
        // » Donasi #2: Ni Komang Septiarini Rp20.000 pada 21 Jul 2026. Total donasi Rp220.000.
        Donation(2, "Ni Komang Septiarini", 20000, "2026-07-21")
    // » Tutup daftar donasi.
    )

    // TABEL PEMBAYARAN INTI: paymentRecords["3"]["M05 (02 Feb)"] = true artinya siswa id 3 sudah bayar minggu M05.
    // Saldo, denda, status lunas, dan grafik semua dihitung dari tabel ini.
    // » paymentRecords = TABEL PEMBAYARAN INTI: kunci luar = id siswa (teks), kunci dalam = label minggu, nilai
    // » true = LUNAS. Awalnya kosong; diisi seedPayments() atau restore().
    private val paymentRecords = mutableMapOf<String, MutableMap<String, Boolean>>()

    // Dispensasi: siswa yang sedang BI atau PKL tidak wajib bayar pada rentang tanggal tertentu. Minggu itu tidak dihitung wajib dan tidak didenda.
    // Siswa yang sedang BI/PKL tidak wajib bayar kas pada rentang tanggal tersebut
    // » Exemption = cetakan data dispensasi: id siswa, tanggal mulai, tanggal selesai, alasan.
    private data class Exemption(val memberId: Int, val from: LocalDate, val until: LocalDate, val reason: String)
    // CARI-DISPENSASI | Siswa yang bebas kas pada rentang tanggal tertentu (mis. BI/PKL).
    //   Tambah: Exemption(idSiswa, LocalDate dari, LocalDate sampai, "alasan").
    // » Daftar dispensasi aktif: siswa 2 bebas kas 1 Juli sampai 20 September 2026 karena BI/PKL.
    private val exemptions = listOf(Exemption(2, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 9, 20), "BI/PKL"))

    // true bila minggu `date` jatuh dalam rentang dispensasi siswa itu. Dipakai tampilan (label Dispensasi) dan rumus denda.
    // CARI-DISPENSASI | Dipakai layar Kas & rumus denda untuk melewati minggu dispensasi.
    /** true jika anggota dibebaskan dari kas pada minggu [date] (mis. sedang BI/PKL). */
    // » isExempt = cek apakah siswa dibebaskan dari kas di minggu tertentu. Hasil true/false.
    fun isExempt(memberId: Int, date: String): Boolean {
        // » Ubah teks minggu menjadi tanggal. Kalau gagal diubah (null), anggap tidak dibebaskan (false).
        val d = parseDateLabel(date) ?: return false
        // » Cari di daftar dispensasi: ada yang id siswanya sama DAN tanggal minggu tidak sebelum "from" dan tidak
        // » sesudah "until".
        return exemptions.any { it.memberId == memberId && !d.isBefore(it.from) && !d.isAfter(it.until) }
    }

    // Payment history for new payment input feature
    // » paymentHistoryList = daftar riwayat input pembayaran. Awalnya kosong.
    private val paymentHistoryList = mutableListOf<PaymentRecord>()

    // CARI-NOMINAL-KAS CARI-SALDO | kasAmount = iuran per minggu, previousBalance = saldo awal, realBalance = uang fisik di peti kas.
    // Pengaturan aktif: iuran Rp5.000, saldo awal 0, saldo fisik Rp1.389.000, kelas XII PPLG.
    // » configData = pengaturan kas yang sedang berlaku (bisa diubah di profil admin).
    private var configData = Config(
        // » Iuran per minggu Rp5.000.
        kasAmount       = 5000,
        // » Saldo awal 0.
        previousBalance = 0,
        // » Saldo uang fisik Rp1.389.000 (dicocokkan dengan buku kas).
        realBalance     = 1389000,
        // » Nama kelas XII PPLG.
        className       = "XII PPLG",
        // » Tahun ajaran 2026/2027.
        year            = "2026/2027"
    )

    // Dijalankan sekali saat repository dibuat: bila tidak ada data tersimpan, isi tabel pembayaran dari DATA AWAL.
    // » init = blok yang dijalankan otomatis SEKALI saat repository dibuat.
    init {
        // » Coba muat data dari HP; kalau tidak ada/rusak (restore false), isi dari data awal (seedPayments).
        if (!restore()) seedPayments()
    }

    // Mengubah string seedStatus ("1101...") tiap siswa menjadi tabel centang per minggu.
    // CARI-DATA-AWAL | Mengubah seedStatus menjadi tabel pembayaran saat aplikasi pertama kali dipasang.
    // » seedPayments() = membuat tabel centang awal dari seedStatus.
    private fun seedPayments() {
        // » Untuk tiap siswa di daftar...
        membersList.forEach { member ->
            // » ...ambil string 37 karakter miliknya (kalau tidak ada, teks kosong).
            val status = seedStatus[member.id].orEmpty()
            // » Untuk tiap minggu (i = urutan), pasangkan label minggu dengan true bila karakter ke-i adalah '1'.
            // » Hasilnya disimpan di paymentRecords dengan kunci id siswa.
            paymentRecords[member.id.toString()] = datesList.mapIndexed { i, date -> date to (status.getOrNull(i) == '1') }
                // » Ubah pasangan jadi tabel (toMap), lalu jadikan bisa diubah (toMutableMap).
                .toMap().toMutableMap()
        }
    }

    // --- SESSION MANAGEMENT ---
    // » Mengambil peran yang sedang login (admin/user). "suspend" = boleh dipanggil dari tugas belakang layar.
    suspend fun getRole(): String? = currentRole
    // true bila ada sesi login aktif (dipakai untuk melewati layar login).
    // » hasToken() = apakah ada sesi login aktif. Dipakai AppRoot untuk memutuskan menampilkan login atau tidak.
    suspend fun hasToken(): Boolean = isLoggedIn
    // id siswa yang sedang login (null bila admin).
    // » Mengambil id siswa yang sedang login (null kalau admin).
    suspend fun getCurrentMemberId(): Int? = currentMemberId

    // Hitung (minggu lunas, minggu wajib) satu siswa. Minggu dispensasi tidak dihitung wajib. Dipakai kartu "Status Kas Anda".
    // Menghitung (minggu lunas, minggu wajib) satu siswa, minggu dispensasi tidak dihitung wajib.
    // » Menghitung (jumlah minggu lunas, jumlah minggu wajib) satu siswa. Result = bungkus hasil sukses/gagal.
    // » withContext(Dispatchers.IO) = dikerjakan di jalur data.
    suspend fun getPaymentStatusForMember(memberId: Int): Result<Pair<Int, Int>> = withContext(Dispatchers.IO) {
        // » Ambil tabel centang milik siswa itu; kalau belum ada, tabel kosong.
        val memberPayments = paymentRecords[memberId.toString()] ?: emptyMap()
        // » dueWeeks = minggu yang WAJIB dibayar: semua minggu kecuali minggu dispensasi.
        val dueWeeks = datesList.filterNot { isExempt(memberId, it) }
        // » totalWeeks = jumlah minggu wajib.
        val totalWeeks = dueWeeks.size
        // » paidWeeks = jumlah minggu wajib yang statusnya true (lunas).
        val paidWeeks = dueWeeks.count { date -> memberPayments[date] == true }
        // » Kembalikan sukses dengan pasangan (lunas, wajib).
        Result.success(Pair(paidWeeks, totalWeeks))
    }

    // LOGIN. Dua jalur:
    //   role "admin" : email harus admin@gmail.com DAN sha256(password) sama dengan ADMIN_PASSWORD_HASH
    //   role "user"  : siswa cukup memilih nama (memberId). Tidak ada password untuk siswa.
    // Hasilnya Result.success atau Result.failure(pesan). Pesan gagal ditampilkan di LoginScreen.
    // CARI-LOGIN | role "admin" = email + password; role "user" = siswa cukup memilih nama.
    // TANYA-GURU: Bedanya admin dan siswa? Admin login email + password (hash SHA-256), siswa cukup pilih nama.
    // Admin bisa centang pembayaran, kelola pengeluaran, dan melihat rekap denda. Siswa hanya melihat miliknya.
    // » login() = proses masuk. Parameter: role ("admin"/"user"), email & password (untuk admin), memberId
    // » (untuk siswa). Hasilnya Result: sukses membawa LoginResponse, gagal membawa pesan error.
    suspend fun login(role: String, email: String? = null, password: String? = null, memberId: Int? = null, @Suppress("UNUSED_PARAMETER") name: String? = null): Result<LoginResponse> = withContext(Dispatchers.IO) {
        // » Jalur 1: kalau yang login adalah ADMIN...
        if (role == "admin") {
            // » Cek 3 syarat sekaligus: email (dirapikan: trim spasi, lowercase) sama dengan admin@gmail.com, password
            // » tidak kosong, dan hash password yang diketik sama dengan hash yang tersimpan.
            if (email?.trim()?.lowercase() == ADMIN_EMAIL && password != null && sha256(password) == ADMIN_PASSWORD_HASH) {
                // » Benar: catat peran sesi sebagai admin.
                currentRole = "admin"
                // » Tandai sudah login.
                isLoggedIn = true
                // » Simpan email admin.
                adminEmail = email
                // » Kembalikan SUKSES dengan token admin.
                Result.success(LoginResponse(token = "admin_token", role = "admin"))
            // » Kalau salah satu syarat gagal...
            } else {
                // » ...kembalikan GAGAL dengan pesan "Email atau password salah" (ditampilkan di layar login).
                Result.failure(Exception("Email atau password salah"))
            }
        // » Jalur 2: kalau yang login adalah SISWA (user)...
        } else {
            // User login via member selection
            // » Siswa dipilih lewat nama (memberId); cek id itu memang ada di daftar siswa.
            if (memberId != null && membersList.any { it.id == memberId }) {
                // » Catat peran sesi sebagai user.
                currentRole = "user"
                // » Simpan id siswa yang login.
                currentMemberId = memberId
                // » Tandai sudah login.
                isLoggedIn = true
                // » Kembalikan SUKSES dengan token berisi id siswa.
                Result.success(LoginResponse(token = "user_token_$memberId", role = "user"))
            // » Kalau siswa belum dipilih/tidak ada...
            } else {
                // » ...kembalikan GAGAL "Pilih anggota terlebih dahulu".
                Result.failure(Exception("Pilih anggota terlebih dahulu"))
            }
        }
    }

    // Menyimpan siswa yang dipilih di layar login.
    // » setMemberId = menyimpan siswa yang dipilih di layar login ke sesi.
    suspend fun setMemberId(id: Int) = withContext(Dispatchers.IO) {
        // » Simpan id siswa ke currentMemberId.
        currentMemberId = id
    }

    // Keluar: menghapus peran dan id siswa dari sesi, lalu menyimpan ke HP.
    // » logout() = keluar dari akun.
    suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        // » Kosongkan peran.
        currentRole = null
        // » Kosongkan id siswa.
        currentMemberId = null
        // » Tandai belum login.
        isLoggedIn = false
        // » Kosongkan email admin.
        adminEmail = ""
        // » Kembalikan sukses tanpa isi (Unit).
        Result.success(Unit)
    }

    // Profil yang login: admin dibuat virtual ("Administrator"), siswa diambil dari membersList.
    // --- PROFILE ---
    // » getCurrentUser() = mengambil data profil akun yang sedang login untuk layar Profil.
    suspend fun getCurrentUser(): Result<UserProfile> = withContext(Dispatchers.IO) {
        // » Kalau yang login admin...
        if (currentRole == "admin") {
            // » ...buat profil "virtual" (tidak ada di daftar siswa).
            Result.success(UserProfile(
                // » Nama: Administrator.
                name = "Administrator",
                // » Peran ditulis "Admin".
                role = "Admin",
                // » Email admin yang login.
                email = adminEmail,
                // » NIS: "-" (tidak ada).
                nis = "-",
                // » Telepon: "-".
                phone = "-",
                // » Bio admin.
                bio = "Administrator AppCash XII PPLG",
                // » Umur: "-".
                umur = "-"
            ))
        // » Kalau yang login siswa...
        } else {
            // » Cari data siswa dengan id yang sedang login.
            val member = membersList.find { it.id == currentMemberId }
            // » Kalau ketemu...
            if (member != null) {
                // » ...buat profil dari data siswa itu.
                Result.success(UserProfile(
                    // » Nama siswa.
                    name = member.name,
                    // » Jabatan siswa.
                    role = member.role,
                    // » Email kosong (siswa tidak punya email).
                    email = "",
                    // » NIS siswa.
                    nis = member.nis,
                    // » Nomor telepon siswa.
                    phone = member.phone,
                    // » Bio siswa.
                    bio = member.bio,
                    // » Umur siswa.
                    umur = member.umur
                ))
            // » Kalau tidak ketemu...
            } else {
                // » ...kembalikan gagal "User not found".
                Result.failure(Exception("User not found"))
            }
        }
    }

    // CARI-PROFIL | Menyimpan perubahan nama, no. HP, dan bio siswa yang sedang login.
    // » updateProfile() = menyimpan perubahan nama, nomor HP, dan bio siswa yang sedang login.
    suspend fun updateProfile(name: String, phone: String, bio: String): Result<Unit> = withContext(Dispatchers.IO) {
        // » Kalau yang login admin...
        if (currentRole == "admin") {
            // Admin profile is virtual, no member to update
            // » ...tidak ada yang disimpan (profil admin hanya virtual), cukup kembalikan sukses.
            Result.success(Unit)
        // » Kalau siswa...
        } else {
            // » Cari urutan (indeks) siswa itu di daftar; -1 bila tidak ada.
            val idx = membersList.indexOfFirst { it.id == currentMemberId }
            // » Kalau ketemu...
            if (idx >= 0) {
                // » Ganti data siswa dengan salinan baru (copy) berisi nama, telepon, bio baru; trim() membuang spasi di
                // » awal/akhir.
                membersList[idx] = membersList[idx].copy(name = name.trim(), phone = phone.trim(), bio = bio.trim())
                // » Simpan ke HP dan beritahu layar agar memuat ulang.
                persist()
                // » Kembalikan sukses.
                Result.success(Unit)
            // » Kalau tidak ketemu...
            } else {
                // » ...kembalikan gagal.
                Result.failure(Exception("User not found"))
            }
        }
    }

    // --- DENDA (PENALTY) CALCULATION ---
    // 5% per month based on device date vs payment due date

    // Kamus singkatan bulan di label minggu ("Ags" = Agustus) ke angka bulan, untuk mengubah label jadi tanggal.
    // » monthMap = kamus singkatan bulan -> angka bulan, untuk mengubah label minggu menjadi tanggal.
    private val monthMap = mapOf(
        // » Januari 1, Februari 2, Maret 3, April 4.
        "Jan" to 1, "Feb" to 2, "Mar" to 3, "Apr" to 4,
        // » Mei 5, Juni 6, Juli 7, Agustus ditulis "Ags" = 8.
        "Mei" to 5, "Jun" to 6, "Jul" to 7, "Ags" to 8,
        // » September 9, Oktober 10, November 11, Desember 12.
        "Sep" to 9, "Okt" to 10, "Nov" to 11, "Des" to 12
    )

    // Mengubah label "M05 (02 Feb)" jadi tanggal 2 Februari 2026 (LocalDate). Tahun dikunci 2026. Hasil null bila format salah.
    /**
     * Parse date label like "M01 (05 Jan)" → LocalDate(2026, 1, 5)
     */
    // » parseDateLabel() = mengubah label minggu "M05 (02 Feb)" menjadi tanggal. Hasil null kalau format salah.
    private fun parseDateLabel(label: String): LocalDate? {
        // » Regex = pola pencarian teks: cari tanda kurung berisi 2 angka (hari), spasi, lalu kata (bulan).
        val regex = Regex("""\((\d{2})\s+(\w+)\)""")
        // » Cari pola itu di label. Kalau tidak ketemu, kembalikan null.
        val match = regex.find(label) ?: return null
        // » Ambil angka hari (kelompok 1) lalu ubah ke Int; kalau gagal, null.
        val day = match.groupValues[1].toIntOrNull() ?: return null
        // » Ambil singkatan bulan (kelompok 2), contoh "Feb".
        val monthStr = match.groupValues[2]
        // » Ubah singkatan bulan jadi angka lewat monthMap; kalau tidak dikenal, null.
        val month = monthMap[monthStr] ?: return null
        // » Buat tanggal dengan tahun 2026 (tahun dikunci). Kalau tanggalnya tidak valid (mis. 31 Feb), kembalikan
        // » null.
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
    // » calculateDenda() = rumus DENDA satu siswa. today = tanggal HP hari ini. Hasil DendaInfo, atau null kalau
    // » tidak punya denda.
    private fun calculateDenda(member: Member, today: LocalDate = LocalDate.now()): DendaInfo? {
        // » Ambil tabel centang siswa; kalau belum ada, kosong.
        val memberPayments = paymentRecords[member.id.toString()] ?: emptyMap()
        // » total = jumlah denda dalam rupiah, mulai 0.
        var total = 0
        // » unpaidMonths = kumpulan nomor bulan yang punya minggu telat (Set = tanpa duplikat).
        val unpaidMonths = mutableSetOf<Int>()
        // » Periksa SETIAP minggu satu per satu.
        datesList.forEach { date ->
            // » Kalau minggu itu sudah lunas ATAU siswa dibebaskan (dispensasi), lewati minggu ini.
            if (memberPayments[date] == true || isExempt(member.id, date)) return@forEach
            // » Ubah label minggu menjadi tanggal jatuh tempo; kalau gagal, lewati.
            val due = parseDateLabel(date) ?: return@forEach
            // » monthsLate = jumlah BULAN PENUH antara tanggal jatuh tempo sampai hari ini.
            val monthsLate = ChronoUnit.MONTHS.between(due, today).toInt()
            // » Kalau sudah telat minimal 1 bulan penuh...
            if (monthsLate > 0) {
                // » ...tambah denda minggu itu: iuran (5000) x 5 x bulanTelat / 100. Contoh telat 2 bulan = 5000 x 5 x 2 /
                // » 100 = Rp500.
                total += configData.kasAmount * DENDA_RATE_PERCENT * monthsLate / 100
                // » Catat bulan minggu ini ke daftar bulan telat.
                unpaidMonths.add(due.monthValue)
            }
        }
        // » Kalau total denda 0, kembalikan null (tidak punya denda).
        if (total <= 0) return null
        // » Sebaliknya, buat hasil DendaInfo:
        return DendaInfo(
            // » id siswa.
            memberId = member.id,
            // » nama siswa.
            memberName = member.name,
            // » jumlah bulan berbeda yang telat.
            unpaidMonths = unpaidMonths.size,
            // » persentase = jumlah bulan x 5 (tampil di layar).
            dendaPercentage = unpaidMonths.size * DENDA_RATE_PERCENT.toDouble(),
            // » total rupiah denda.
            dendaAmount = total
        )
    }

    // Denda hanya untuk minggu-minggu yang DICENTANG saat membayar (rumus sama dengan calculateDenda tetapi per minggu pilihan).
    // Dipakai dialog Bayar Kas untuk menampilkan total yang harus dibayar.
    // CARI-DENDA CARI-PEMBAYARAN | Denda hanya untuk minggu yang dicentang saat membayar (dipakai dialog Bayar Kas).
    /** Denda untuk minggu-minggu tertentu saja (dipakai saat membayar sebagian tunggakan). */
    // » dendaForWeeks() = denda hanya untuk minggu-minggu yang dipilih (dipakai dialog Bayar Kas). Hasil Int
    // » rupiah.
    fun dendaForWeeks(dates: Collection<String>, today: LocalDate = LocalDate.now()): Int =
        // » Jumlahkan (sumOf) nilai denda untuk setiap label minggu yang dipilih.
        dates.sumOf { date ->
            // » Ubah label ke tanggal; kalau gagal, nilai minggu itu 0.
            val due = parseDateLabel(date) ?: return@sumOf 0
            // » Rumus sama: iuran x 5 x bulanTelat / 100. coerceAtLeast(0) = jika belum telat (negatif) dianggap 0.
            configData.kasAmount * DENDA_RATE_PERCENT * ChronoUnit.MONTHS.between(due, today).toInt().coerceAtLeast(0) / 100
        }

    // Rekap denda semua siswa. Hanya ditampilkan di Dashboard akun admin.
    // » getDendaForAllMembers() = rekap denda semua siswa. Dipakai Dashboard admin.
    suspend fun getDendaForAllMembers(): Result<List<DendaInfo>> = withContext(Dispatchers.IO) {
        // » Hitung denda tiap siswa (calculateDenda), buang yang null (mapNotNull), kembalikan daftar sisanya.
        Result.success(membersList.mapNotNull { calculateDenda(it) })
    }

    // Denda satu siswa, dipakai di Profil dan kartu Status Kas Anda.
    // » getDendaForMember() = denda satu siswa tertentu (untuk Profil dan kartu Status Kas Anda).
    suspend fun getDendaForMember(memberId: Int): Result<DendaInfo?> = withContext(Dispatchers.IO) {
        // » Cari siswa dengan id itu.
        val member = membersList.find { it.id == memberId }
            // » Kalau tidak ketemu, kembalikan gagal "Member not found".
            ?: return@withContext Result.failure(Exception("Member not found"))
        // » Kembalikan hasil perhitungan denda siswa itu (bisa null = tidak ada denda).
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
    // » getDashboard() = menyiapkan SEMUA angka Dashboard: saldo dan data grafik.
    suspend fun getDashboard(): Result<Dashboard> = withContext(Dispatchers.IO) {
        // » totalPaymentsCount = jumlah seluruh centang LUNAS, mulai dari 0.
        var totalPaymentsCount = 0
        // » Untuk tiap minggu, hitung berapa siswa lunas (count it[date]==true), tambahkan ke total.
        datesList.forEach { date -> totalPaymentsCount += paymentRecords.values.count { it[date] == true } }
        // » Pemasukan iuran = jumlah centang lunas x iuran per minggu (Rp5.000).
        val totalKasIncome = totalPaymentsCount * configData.kasAmount
        // » Total donasi = jumlahkan nominal semua donasi.
        val totalDonation = donationsList.sumOf { it.amount }
        // » Total pengeluaran = jumlahkan nominal semua pengeluaran.
        val totalExpenseAmount = expensesList.sumOf { it.amount }
        // » SALDO BUKU = saldo awal + pemasukan iuran + donasi - pengeluaran.
        val netBalance = configData.previousBalance + totalKasIncome + totalDonation - totalExpenseAmount

        // Grafik per bulan: pemasukan = kas + donasi, pengeluaran = sesuai tanggal transaksi
        // » months = "keranjang" per bulan yang urut dari Januari. Setiap bulan berisi 2 angka: [0] pemasukan, [1]
        // » pengeluaran.
        val months = sortedMapOf<YearMonth, IntArray>()
        // bucket = ambil "keranjang" dua angka milik bulan dari tanggal d (dibuat bila belum ada).
        // Kelompokkan pemasukan dan pengeluaran per bulan untuk grafik Dashboard.
        // » bucket(d) = fungsi kecil: ambil keranjang bulan dari tanggal d; kalau belum ada, buat baru berisi dua
        // » angka 0 (IntArray(2)). Hasil null kalau tanggal null.
        fun bucket(d: LocalDate?) = d?.let { months.getOrPut(YearMonth.from(it)) { IntArray(2) } }
        // » Untuk tiap minggu kas...
        datesList.forEach { date ->
            // » ...tambahkan ke keranjang bulannya (angka [0] pemasukan): jumlah siswa lunas minggu itu x iuran.
            bucket(parseDateLabel(date))?.let { it[0] += paymentRecords.values.count { r -> r[date] == true } * configData.kasAmount }
        }
        // » Untuk tiap donasi: ubah tanggalnya (runCatching = aman bila gagal), tambahkan nominalnya ke pemasukan
        // » bulan itu.
        donationsList.forEach { d -> bucket(runCatching { LocalDate.parse(d.date) }.getOrNull())?.let { it[0] += d.amount } }
        // » Untuk tiap pengeluaran: tambahkan nominalnya ke angka [1] (pengeluaran) bulan itu.
        expensesList.forEach { e -> bucket(runCatching { LocalDate.parse(e.date) }.getOrNull())?.let { it[1] += e.amount } }
        // » Daftar singkatan nama bulan untuk label sumbu grafik.
        val shortMonth = listOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Ags", "Sep", "Okt", "Nov", "Des")

        // » latestDate = minggu terakhir yang tanggalnya sudah lewat/sama dengan hari ini; kalau tidak ada, pakai
        // » minggu terakhir daftar, atau teks kosong.
        val latestDate = datesList.lastOrNull { d -> parseDateLabel(d)?.let { !it.isAfter(LocalDate.now()) } ?: false } ?: datesList.lastOrNull() ?: ""
        // » latestPaidCount = berapa siswa yang lunas di minggu terakhir itu.
        val latestPaidCount = paymentRecords.values.count { it[latestDate] == true }

        // » Bungkus semua hasil jadi sukses.
        Result.success(
            // » Buat objek Dashboard dengan isian:
            Dashboard(
                // » saldo buku (netBalance).
                totalBalance      = netBalance,
                // » saldo awal dari pengaturan.
                previousBalance   = configData.previousBalance,
                // » iuran per minggu dari pengaturan.
                kasAmount         = configData.kasAmount,
                // » total pemasukan iuran.
                totalKasIncome    = totalKasIncome,
                // » jumlah centang lunas.
                totalPayments     = totalPaymentsCount,
                // » jumlah minggu (37).
                totalPaymentDates = datesList.size,
                // » jumlah siswa (33).
                totalMembers      = membersList.size,
                // » total pengeluaran.
                totalExpenseAmount= totalExpenseAmount,
                // » banyaknya catatan pengeluaran.
                totalExpenseCount = expensesList.size,
                // » jumlah siswa lunas minggu terakhir.
                latestPaid        = latestPaidCount,
                // » label grafik: nama bulan singkat dari tiap bulan yang punya transaksi.
                weeklyLabels      = months.keys.map { shortMonth[it.monthValue - 1] },
                // » garis pemasukan: angka [0] tiap bulan.
                weeklyKasIncome   = months.values.map { it[0] },
                // » garis pengeluaran: angka [1] tiap bulan.
                weeklyExpenses    = months.values.map { it[1] },
                // » total donasi.
                totalDonationAmount = totalDonation,
                // » saldo uang fisik dari pengaturan.
                realBalance       = configData.realBalance
            )
        )
    }

    // Daftar donasi (ikut dihitung sebagai pemasukan).
    // » getDonations() = daftar donasi (salinan toList agar aman).
    suspend fun getDonations(): Result<List<Donation>> = withContext(Dispatchers.IO) { Result.success(donationsList.toList()) }

    // Daftar 33 siswa untuk layar Anggota dan pilihan nama di Login.
    // » getMembers() = daftar 33 siswa (salinan) untuk layar Anggota dan pilihan nama di Login.
    suspend fun getMembers(): Result<List<Member>> = withContext(Dispatchers.IO) { Result.success(membersList.toList()) }

    // Tabel centang lunas semua siswa per minggu (salinan, supaya layar tidak mengubah data asli).
    // » getPayments() = tabel centang semua siswa untuk layar Kas.
    suspend fun getPayments(): Result<PaymentsWrapper> = withContext(Dispatchers.IO) {
        // Deep copy: UI tidak boleh mengubah map internal repository secara langsung
        // » Kembalikan salinan daftar minggu dan salinan tabel centang (mapValues + toMap) supaya layar tidak
        // » mengubah data asli.
        Result.success(PaymentsWrapper(dates = datesList.toList(), records = paymentRecords.mapValues { it.value.toMap() }))
    }

    // Menyimpan perubahan centang. Dipakai admin (edit minggu, tambah pembayaran) dan siswa (konfirmasi setelah kirim WA).
    // persist() membuat semua layar memuat ulang sehingga saldo ikut berubah.
    // CARI-PEMBAYARAN | Menyimpan centang lunas per siswa per minggu.
    // » savePayments() = simpan perubahan centang. Dipakai admin (edit tabel) dan siswa (setelah kirim WA).
    suspend fun savePayments(wrapper: PaymentsWrapper): Result<Unit> = withContext(Dispatchers.IO) {
        // » Untuk tiap siswa (memberId) beserta peta minggunya (datesMap)...
        wrapper.records.forEach { (memberId, datesMap) ->
            // » ...ambil tabel centang lama siswa itu (atau buat baru bila belum ada).
            val existing = paymentRecords[memberId] ?: mutableMapOf()
            // » ...timpa tiap minggu dengan nilai baru (paid = true/false).
            datesMap.forEach { (date, paid) -> existing[date] = paid }
            // » ...simpan kembali ke tabel utama.
            paymentRecords[memberId] = existing
        }
        // » persist() = simpan ke HP dan naikkan versi supaya semua layar memuat ulang (saldo ikut berubah).
        persist()
        // » Kembalikan sukses.
        Result.success(Unit)
    }

    // Mengubah satu centang (siswa, minggu) menjadi lunas atau belum, lalu menyimpan.
    // » updatePaymentStatus() = ubah SATU centang: siswa memberId, minggu date, lunas atau belum (paid).
    suspend fun updatePaymentStatus(memberId: Int, date: String, paid: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        // » Ambil tabel centang siswa itu (atau buat baru).
        val memberMap = paymentRecords[memberId.toString()] ?: mutableMapOf()
        // » Ubah centang minggu itu menjadi paid.
        memberMap[date] = paid
        // » Simpan tabel itu kembali.
        paymentRecords[memberId.toString()] = memberMap
        // » Simpan ke HP & refresh layar.
        persist()
        // » Kembalikan sukses.
        Result.success(Unit)
    }

    // Daftar pengeluaran untuk layar Pengeluaran Kas.
    // » getExpenses() = daftar pengeluaran untuk layar Pengeluaran Kas (salinan).
    suspend fun getExpenses(): Result<List<Expense>> = withContext(Dispatchers.IO) { Result.success(expensesList.toList()) }

    // Tambah pengeluaran. Validasi: keterangan tidak boleh kosong dan nominal harus > 0. Id baru = id terbesar + 1.
    // Ditaruh di urutan paling atas (add(0, ...)), lalu saldo otomatis turun.
    // CARI-PENGELUARAN CARI-VALIDASI | Tambah pengeluaran; keterangan wajib diisi dan nominal > 0.
    // » addExpense() = tambah pengeluaran baru dari ExpenseRequest. Hasilnya id baru.
    suspend fun addExpense(req: ExpenseRequest): Result<IdResponse> = withContext(Dispatchers.IO) {
        // » Validasi: kalau keterangan kosong, GAGAL "Keterangan wajib diisi".
        if (req.description.isBlank()) return@withContext Result.failure(Exception("Keterangan wajib diisi"))
        // » Validasi: kalau nominal 0 atau minus, GAGAL "Nominal harus lebih dari 0".
        if (req.amount <= 0) return@withContext Result.failure(Exception("Nominal harus lebih dari 0"))
        // » Id baru = id terbesar yang ada + 1 (kalau daftar kosong mulai dari 0+1).
        val newId = (expensesList.maxOfOrNull { it.id } ?: 0) + 1
        // » Taruh pengeluaran baru di URUTAN PALING ATAS (add(0, ...)). Keterangan dirapikan dengan trim().
        expensesList.add(0, Expense(newId, req.description.trim(), req.amount, req.date, req.category))
        // » Simpan ke HP; saldo otomatis berkurang karena dihitung ulang.
        persist()
        // » Kembalikan sukses dengan id baru.
        Result.success(IdResponse(newId))
    }

    // Hapus pengeluaran (admin). Karena saldo dihitung ulang dari daftar, saldo otomatis naik lagi.
    // Menghapus satu pengeluaran (khusus admin), saldo otomatis naik lagi.
    // » deleteExpense() = hapus satu pengeluaran berdasarkan id (khusus admin).
    suspend fun deleteExpense(id: Int): Result<Unit> = withContext(Dispatchers.IO) {
        // » Hapus semua pengeluaran yang id-nya sama.
        expensesList.removeAll { it.id == id }
        // » Simpan ke HP; saldo otomatis naik lagi.
        persist()
        // » Kembalikan sukses.
        Result.success(Unit)
    }

    // CARI-PENGELUARAN | Edit pengeluaran yang sudah ada.
    // » updateExpense() = edit pengeluaran yang sudah ada (keterangan, nominal, tanggal).
    suspend fun updateExpense(id: Int, description: String, amount: Int, date: String): Result<Unit> = withContext(Dispatchers.IO) {
        // » Validasi keterangan tidak boleh kosong.
        if (description.isBlank()) return@withContext Result.failure(Exception("Keterangan wajib diisi"))
        // » Validasi nominal harus lebih dari 0.
        if (amount <= 0) return@withContext Result.failure(Exception("Nominal harus lebih dari 0"))
        // » Cari urutan pengeluaran dengan id itu; -1 bila tidak ada.
        val idx = expensesList.indexOfFirst { it.id == id }
        // » Kalau ketemu...
        if (idx >= 0) {
            // » ...ambil data lamanya.
            val old = expensesList[idx]
            // » ...ganti dengan salinan yang sudah diubah (keterangan, nominal, tanggal); kategori tetap.
            expensesList[idx] = old.copy(description = description.trim(), amount = amount, date = date)
            // » Simpan ke HP.
            persist()
            // » Kembalikan sukses.
            Result.success(Unit)
        // » Kalau tidak ketemu...
        } else {
            // » ...kembalikan gagal "Expense not found".
            Result.failure(Exception("Expense not found"))
        }
    }

    // CARI-ANGGOTA | Edit data siswa (khusus admin).
    // » updateMember() = edit data satu siswa (khusus admin): nama, NIS, jabatan, bio, nomor HP.
    suspend fun updateMember(id: Int, name: String, nis: String, role: String, bio: String, phone: String): Result<Unit> = withContext(Dispatchers.IO) {
        // » Cari urutan siswa dengan id itu.
        val idx = membersList.indexOfFirst { it.id == id }
        // » Kalau ketemu...
        if (idx >= 0) {
            // » ...ganti dengan salinan berisi data baru (spasi di awal/akhir dibuang).
            membersList[idx] = membersList[idx].copy(name = name.trim(), nis = nis.trim(), role = role, bio = bio.trim(), phone = phone.trim())
            // » Simpan ke HP.
            persist()
            // » Kembalikan sukses.
            Result.success(Unit)
        // » Kalau tidak ketemu...
        } else {
            // » ...gagal "Member not found".
            Result.failure(Exception("Member not found"))
        }
    }

    // Pengaturan kas: nominal per minggu, saldo awal, saldo riil, nama kelas.
    // » getConfig() = mengambil pengaturan kas yang berlaku.
    suspend fun getConfig(): Result<Config> = withContext(Dispatchers.IO) { Result.success(configData) }

    // Mengubah pengaturan kas (belum ada layarnya, nilai diatur tim di kode).
    // » updateConfig() = mengubah pengaturan kas. Nilai yang null (tidak dikirim) tetap memakai nilai lama
    // » (operator ?:).
    suspend fun updateConfig(req: ConfigRequest): Result<Unit> = withContext(Dispatchers.IO) {
        // » Ganti configData dengan salinan yang sudah diperbarui:
        configData = configData.copy(
            // » iuran per minggu.
            kasAmount       = req.kasAmount ?: configData.kasAmount,
            // » saldo awal.
            previousBalance = req.previousBalance ?: configData.previousBalance,
            // » saldo uang fisik.
            realBalance     = req.realBalance ?: configData.realBalance,
            // » nama kelas.
            className       = req.className ?: configData.className,
            // » tahun ajaran.
            year            = req.year ?: configData.year
        )
        // » Simpan ke HP.
        persist()
        // » Kembalikan sukses.
        Result.success(Unit)
    }

    // Untuk PaymentInputScreen yang belum dipasang di menu. Riwayat ini TIDAK ikut menghitung saldo (saldo hanya dari tabel centang).
    // --- Payment Input Feature ---
    // CARI-TIDAK-DIPAKAI | Untuk PaymentInputScreen yang belum dipasang di navigasi; tidak ikut dihitung di saldo.
    // » addPaymentRecord() = menambah riwayat input pembayaran. (Layar PaymentInputScreen belum dipasang;
    // » riwayat ini TIDAK ikut menghitung saldo.)
    suspend fun addPaymentRecord(memberId: Int, amount: Int, date: String, note: String): Result<PaymentRecord> = withContext(Dispatchers.IO) {
        // » Cari siswa; kalau tidak ketemu, gagal "Member not found".
        val member = membersList.find { it.id == memberId } ?: return@withContext Result.failure(Exception("Member not found"))
        // » Validasi nominal harus > 0.
        if (amount <= 0) return@withContext Result.failure(Exception("Nominal harus lebih dari 0"))
        // » Id baru = id riwayat terbesar + 1.
        val newId = (paymentHistoryList.maxOfOrNull { it.id } ?: 0) + 1
        // » Buat objek PaymentRecord dari data yang diisi.
        val record = PaymentRecord(id = newId, memberId = memberId, memberName = member.name, amount = amount, date = date, note = note)
        // » Taruh di urutan paling atas.
        paymentHistoryList.add(0, record)
        // » Simpan ke HP.
        persist()
        // » Kembalikan sukses beserta catatannya.
        Result.success(record)
    }

    // Riwayat input pembayaran manual (layar belum dipasang di menu).
    // » getPaymentHistory() = daftar riwayat input pembayaran manual.
    suspend fun getPaymentHistory(): Result<List<PaymentRecord>> = withContext(Dispatchers.IO) {
        // » Kembalikan salinan daftar.
        Result.success(paymentHistoryList.toList())
    }
// » Penutup class AppCashRepository: akhir dari "otak" aplikasi.
}
