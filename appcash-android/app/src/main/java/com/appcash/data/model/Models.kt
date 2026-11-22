// ==========================================================================
// PETA FILE · AppCash (Kelompok 6) · Models.kt
// Isi file : Bentuk data (model) yang dipakai repository dan layar.
// Penanda  : CARI-SALDO, CARI-ANGGOTA, CARI-PENGELUARAN, CARI-DONASI, CARI-NOMINAL-KAS, CARI-DENDA
// Cara cari: VS Code Ctrl+Shift+F (Android Studio Ctrl+Shift+F) lalu ketik
//            "CARI-" untuk semua penanda, atau salah satu nama di baris Penanda.
// ==========================================================================
//  BACA INI DULU
//  File ini hanya "cetakan data" (tidak ada tampilan, tidak ada hitungan). Seperti kolom di buku kas:
//    Member = 1 siswa            Expense = 1 pengeluaran        Donation = 1 donasi
//    Config = pengaturan kas (iuran Rp5.000, saldo fisik Rp1.389.000)
//    Dashboard = paket angka siap tampil untuk layar Dashboard (saldo, pemasukan, data grafik)
//    DendaInfo = hasil hitung denda satu siswa
//  Tanda @SerializedName("nama_di_json") hanya mengatur nama field saat disimpan sebagai JSON (Gson).
//  Catatan: Login/Levy/ConfigRequest dkk adalah sisa rancangan versi online (server), belum dipakai.
// » Alamat folder file ini (package). Folder "model" = tempat bentuk-bentuk data.
package com.appcash.data.model

// » Memanggil @SerializedName dari Gson. Gson = alat yang mengubah objek Kotlin menjadi teks JSON (untuk
// » disimpan di HP) dan sebaliknya. @SerializedName("x") berarti "di JSON, field ini diberi nama x".
import com.google.gson.annotations.SerializedName

// » data class = cetakan data. LoginRequest = data yang dikirim saat login versi online (SISA rancangan,
// » tidak dipakai di versi offline).
data class LoginRequest(
    // » role = jenis akun: "admin" atau "user".
    val role: String,
    // » email = email admin. Tanda "?" artinya boleh kosong (null). "= null" = nilai awalnya kosong.
    val email: String? = null,
    // » password = kata sandi admin yang diketik.
    val password: String? = null,
    // » member_id = nomor siswa yang login sebagai user. Di kode ditulis memberId (gaya Kotlin), di JSON ditulis
    // » member_id.
    @SerializedName("member_id") val memberId: Int? = null,
    // » name = nama siswa (untuk login siswa).
    val name: String? = null
)

// » LoginResponse = jawaban server setelah login (rancangan online, tidak dipakai).
data class LoginResponse(
    // » token = "kunci" tanda sudah login.
    val token: String,
    // » role = peran akun yang berhasil login.
    val role: String
)

// » IdResponse = jawaban server berisi satu nomor id (mis. id pengeluaran baru). Rancangan online.
data class IdResponse(
    // » id = nomor yang dikembalikan.
    val id: Int
)

// Paket angka untuk layar Dashboard. Yang dipakai kartu saldo: totalBalance (saldo buku), realBalance (uang fisik),
// totalKasIncome + totalDonationAmount (pemasukan), totalExpenseAmount (pengeluaran).
// Yang dipakai GRAFIK: weeklyLabels (nama bulan), weeklyKasIncome (pemasukan per bulan), weeklyExpenses (pengeluaran per bulan).
// Walau namanya "weekly", isinya sekarang per BULAN.
// CARI-SALDO | Ringkasan angka untuk Dashboard dan kartu saldo.
// » Dashboard = paket angka siap tampil untuk layar Dashboard. Dibuat oleh getDashboard() di repository,
// » lalu dibaca DashboardScreen.
data class Dashboard(
    // » Saldo buku kas sekarang (saldo awal + pemasukan - pengeluaran). Ini angka besar di kartu saldo.
    @SerializedName("total_balance") val totalBalance: Int,
    // » Saldo awal sebelum pencatatan (saldo bawaan).
    @SerializedName("previous_balance") val previousBalance: Int,
    // » Nominal iuran per minggu (Rp5.000).
    @SerializedName("kas_amount") val kasAmount: Int,
    // » Total uang iuran yang sudah masuk dari semua centang LUNAS.
    @SerializedName("total_kas_income") val totalKasIncome: Int,
    // » Jumlah centang lunas seluruhnya (berapa kali orang sudah bayar).
    @SerializedName("total_payments") val totalPayments: Int,
    // » Jumlah tanggal/minggu iuran (37 minggu).
    @SerializedName("total_payment_dates") val totalPaymentDates: Int,
    // » Jumlah siswa (33).
    @SerializedName("total_members") val totalMembers: Int,
    // » Total rupiah semua pengeluaran.
    @SerializedName("total_expense_amount") val totalExpenseAmount: Int,
    // » Banyaknya catatan pengeluaran.
    @SerializedName("total_expense_count") val totalExpenseCount: Int,
    // » Jumlah siswa yang sudah bayar pada minggu terakhir.
    @SerializedName("latest_paid") val latestPaid: Int,
    // » Nama bulan untuk sumbu bawah grafik (mis. "Jan", "Feb"). emptyList() = daftar kosong sebagai nilai awal.
    val weeklyLabels: List<String> = emptyList(),
    // » Pemasukan iuran tiap bulan (satu angka per bulan). Dipakai garis hijau grafik.
    val weeklyKasIncome: List<Int> = emptyList(),
    // » Pengeluaran tiap bulan. Dipakai garis merah grafik.
    val weeklyExpenses: List<Int> = emptyList(),
    // » Total donasi yang masuk (ditambahkan ke pemasukan).
    val totalDonationAmount: Int = 0,
    // » Saldo UANG FISIK di peti kas (Rp1.389.000). Dibandingkan dengan saldo buku untuk mengecek selisih.
    val realBalance: Int = 0
)

// Satu siswa. id 1 sampai 33. Foto diambil dari file res/drawable/student_<id>. role: Anggota, Ketua Kelas, Bendahara 1, dst.
// CARI-ANGGOTA | Data satu siswa.
// » Member = data satu siswa. Dibuat dari membersList di repository.
data class Member(
    // » Nomor siswa, 1 sampai 33. Dipakai juga sebagai nama foto (student_<id>).
    val id: Int,
    // » Nama lengkap siswa.
    val name: String,
    // » NIS (nomor induk siswa). Awalnya kosong "".
    val nis: String = "",
    // » Jabatan: "Anggota", "Ketua Kelas", "Bendahara 1", dll. Awalnya "Anggota".
    val role: String = "Anggota",
    // » Bio/deskripsi singkat siswa.
    val bio: String = "",
    // » Umur dalam teks, awalnya "17 Tahun".
    val umur: String = "17 Tahun",
    // » Nomor WhatsApp siswa (untuk kirim pengingat).
    val phone: String = "",
    // » Alamat foto profil (kosong = pakai foto bawaan).
    val avatarUrl: String = ""
)

// CARI-PENGELUARAN | Data satu pengeluaran (tanggal format yyyy-MM-dd).
// » Expense = data satu pengeluaran kas.
data class Expense(
    // » Nomor unik pengeluaran.
    val id: Int,
    // » Keterangan, contoh "Beli spidol".
    val description: String,
    // » Jumlah uang keluar dalam rupiah (angka bulat Int).
    val amount: Int,
    // » Tanggal bentuk teks "yyyy-MM-dd" (tahun-bulan-tanggal), contoh 2026-03-09.
    val date: String,
    // » Kategori pengeluaran, awalnya "Perlengkapan".
    val category: String = "Perlengkapan"
)

// CARI-DONASI | Data satu donasi.
// » Donation = data satu donasi yang masuk.
data class Donation(
    // » Nomor unik donasi.
    val id: Int,
    // » Nama pemberi donasi.
    val name: String,
    // » Jumlah rupiah donasi.
    val amount: Int,
    // » Tanggal donasi (yyyy-MM-dd).
    val date: String,
    // » Catatan tambahan; "-" jika tidak ada.
    val note: String = "-"
)

// » Levy = data iuran/tagihan khusus (rancangan, sisa fitur lama, tidak dipakai di layar).
data class Levy(
    // » Nomor tagihan.
    val id: Int,
    // » Judul tagihan.
    val title: String,
    // » Nominal tagihan.
    val amount: Int,
    // » Tanggal mulai tagihan.
    @SerializedName("start_date") val startDate: String,
    // » Batas akhir (deadline) pembayaran.
    val deadline: String,
    // » Daftar id siswa yang sudah membayar.
    @SerializedName("paid_members") val paidMembers: List<Int> = emptyList(),
    // » Apakah uang sudah disetor/dipindahkan.
    val transferred: Boolean = false
)

// Pengaturan kas. kasAmount = iuran per minggu (5000). realBalance = uang fisik di peti kas (1389000), dipakai untuk mencocokkan dengan saldo buku di Dashboard.
// CARI-NOMINAL-KAS | Nilai bawaan konfigurasi kas (nominal, saldo awal, saldo fisik).
// » Config = pengaturan kas yang bisa diubah bendahara.
data class Config(
    // » Iuran per minggu: bawaan 5000.
    @SerializedName("kas_amount") val kasAmount: Int = 5000,
    // » Saldo awal sebelum pencatatan: bawaan 0.
    @SerializedName("previous_balance") val previousBalance: Int = 0,
    // » Saldo uang fisik: bawaan 1.389.000.
    @SerializedName("real_balance") val realBalance: Int = 1389000,
    // » Nama kelas: bawaan "XII PPLG".
    @SerializedName("class_name") val className: String = "XII PPLG",
    // » Tahun ajaran: bawaan "2026/2027".
    val year: String = "2026/2027"
)

// » PaymentsWrapper = bungkus tabel pembayaran: dates = daftar minggu, records = tabel centang.
data class PaymentsWrapper(
    // » Daftar label minggu (M01 sampai M37).
    val dates: List<String> = emptyList(),
    // » Tabel centang bertingkat: id siswa -> (minggu -> lunas true/false).
    val records: Map<String, Map<String, Boolean>> = emptyMap()
)

// » ExpenseRequest = data kiriman saat menambah pengeluaran (rancangan online).
data class ExpenseRequest(
    // » Keterangan pengeluaran.
    val description: String,
    // » Jumlah rupiah.
    val amount: Int,
    // » Tanggal.
    val date: String,
    // » Kategori.
    val category: String = "Perlengkapan"
)

// » LevyRequest = data kiriman untuk membuat tagihan (rancangan online, tidak dipakai).
data class LevyRequest(
    // » Judul.
    val title: String,
    // » Nominal.
    val amount: Int,
    // » Tanggal mulai.
    @SerializedName("start_date") val startDate: String,
    // » Deadline.
    val deadline: String,
    // » Daftar siswa yang sudah bayar.
    @SerializedName("paid_members") val paidMembers: List<Int> = emptyList(),
    // » Sudah disetor atau belum.
    val transferred: Boolean = false
)

// » LevyPaymentRequest = kiriman untuk menandai satu siswa sudah bayar tagihan (tidak dipakai).
data class LevyPaymentRequest(
    // » Id siswa yang membayar.
    @SerializedName("member_id") val memberId: Int
)

// » ConfigRequest = kiriman untuk mengubah pengaturan; semua boleh kosong (null) = tidak diubah.
data class ConfigRequest(
    // » Iuran per minggu baru.
    @SerializedName("kas_amount") val kasAmount: Int? = null,
    // » Saldo awal baru.
    @SerializedName("previous_balance") val previousBalance: Int? = null,
    // » Saldo fisik baru.
    @SerializedName("real_balance") val realBalance: Int? = null,
    // » Nama kelas baru.
    @SerializedName("class_name") val className: String? = null,
    // » Tahun ajaran baru.
    val year: String? = null
)

// » PaymentRecord = catatan satu pembayaran (rancangan online, tidak dipakai).
data class PaymentRecord(
    // » Nomor catatan.
    val id: Int,
    // » Id siswa yang bayar.
    @SerializedName("member_id") val memberId: Int,
    // » Nama siswa.
    @SerializedName("member_name") val memberName: String,
    // » Jumlah.
    val amount: Int,
    // » Tanggal.
    val date: String,
    // » Catatan.
    val note: String = "",
    // » Jenis pembayaran, awalnya "Kas".
    val type: String = "Kas"
)

// » UserProfile = data profil akun yang sedang login (dipakai layar Profil).
data class UserProfile(
    // » Nama pengguna.
    val name: String,
    // » Peran: admin atau user.
    val role: String,
    // » Email.
    val email: String = "",
    // » NIS.
    val nis: String = "",
    // » Nomor telepon.
    val phone: String = "",
    // » Bio.
    val bio: String = "",
    // » Umur.
    val umur: String = "17 Tahun",
    // » Alamat foto.
    val avatarUrl: String = ""
)

// Hasil denda satu siswa: unpaidMonths = berapa bulan berbeda yang punya minggu telat, dendaAmount = total rupiah denda.
// CARI-DENDA | Hasil hitung denda per siswa.
// » DendaInfo = hasil hitung denda untuk SATU siswa (dibuat calculateDenda di repository).
data class DendaInfo(
    // » Id siswa.
    val memberId: Int,
    // » Nama siswa.
    val memberName: String,
    // » Berapa bulan berbeda yang memiliki minggu telat.
    val unpaidMonths: Int,
    // » Persentase denda total, contoh 5.0 atau 10.0 (Double = angka desimal).
    val dendaPercentage: Double,
    // » Total denda dalam rupiah.
    val dendaAmount: Int
)
