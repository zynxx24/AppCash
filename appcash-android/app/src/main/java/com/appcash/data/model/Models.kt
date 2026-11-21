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
package com.appcash.data.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val role: String,
    val email: String? = null,
    val password: String? = null,
    @SerializedName("member_id") val memberId: Int? = null,
    val name: String? = null
)

data class LoginResponse(
    val token: String,
    val role: String
)

data class IdResponse(
    val id: Int
)

// Paket angka untuk layar Dashboard. Yang dipakai kartu saldo: totalBalance (saldo buku), realBalance (uang fisik),
// totalKasIncome + totalDonationAmount (pemasukan), totalExpenseAmount (pengeluaran).
// Yang dipakai GRAFIK: weeklyLabels (nama bulan), weeklyKasIncome (pemasukan per bulan), weeklyExpenses (pengeluaran per bulan).
// Walau namanya "weekly", isinya sekarang per BULAN.
// CARI-SALDO | Ringkasan angka untuk Dashboard dan kartu saldo.
data class Dashboard(
    @SerializedName("total_balance") val totalBalance: Int,
    @SerializedName("previous_balance") val previousBalance: Int,
    @SerializedName("kas_amount") val kasAmount: Int,
    @SerializedName("total_kas_income") val totalKasIncome: Int,
    @SerializedName("total_payments") val totalPayments: Int,
    @SerializedName("total_payment_dates") val totalPaymentDates: Int,
    @SerializedName("total_members") val totalMembers: Int,
    @SerializedName("total_expense_amount") val totalExpenseAmount: Int,
    @SerializedName("total_expense_count") val totalExpenseCount: Int,
    @SerializedName("latest_paid") val latestPaid: Int,
    val weeklyLabels: List<String> = emptyList(),
    val weeklyKasIncome: List<Int> = emptyList(),
    val weeklyExpenses: List<Int> = emptyList(),
    val totalDonationAmount: Int = 0,
    val realBalance: Int = 0
)

// Satu siswa. id 1 sampai 33. Foto diambil dari file res/drawable/student_<id>. role: Anggota, Ketua Kelas, Bendahara 1, dst.
// CARI-ANGGOTA | Data satu siswa.
data class Member(
    val id: Int,
    val name: String,
    val nis: String = "",
    val role: String = "Anggota",
    val bio: String = "",
    val umur: String = "17 Tahun",
    val phone: String = "",
    val avatarUrl: String = ""
)

// CARI-PENGELUARAN | Data satu pengeluaran (tanggal format yyyy-MM-dd).
data class Expense(
    val id: Int,
    val description: String,
    val amount: Int,
    val date: String,
    val category: String = "Perlengkapan"
)

// CARI-DONASI | Data satu donasi.
data class Donation(
    val id: Int,
    val name: String,
    val amount: Int,
    val date: String,
    val note: String = "-"
)

data class Levy(
    val id: Int,
    val title: String,
    val amount: Int,
    @SerializedName("start_date") val startDate: String,
    val deadline: String,
    @SerializedName("paid_members") val paidMembers: List<Int> = emptyList(),
    val transferred: Boolean = false
)

// Pengaturan kas. kasAmount = iuran per minggu (5000). realBalance = uang fisik di peti kas (1389000), dipakai untuk mencocokkan dengan saldo buku di Dashboard.
// CARI-NOMINAL-KAS | Nilai bawaan konfigurasi kas (nominal, saldo awal, saldo fisik).
data class Config(
    @SerializedName("kas_amount") val kasAmount: Int = 5000,
    @SerializedName("previous_balance") val previousBalance: Int = 0,
    @SerializedName("real_balance") val realBalance: Int = 1389000,
    @SerializedName("class_name") val className: String = "XII PPLG",
    val year: String = "2026/2027"
)

data class PaymentsWrapper(
    val dates: List<String> = emptyList(),
    val records: Map<String, Map<String, Boolean>> = emptyMap()
)

data class ExpenseRequest(
    val description: String,
    val amount: Int,
    val date: String,
    val category: String = "Perlengkapan"
)

data class LevyRequest(
    val title: String,
    val amount: Int,
    @SerializedName("start_date") val startDate: String,
    val deadline: String,
    @SerializedName("paid_members") val paidMembers: List<Int> = emptyList(),
    val transferred: Boolean = false
)

data class LevyPaymentRequest(
    @SerializedName("member_id") val memberId: Int
)

data class ConfigRequest(
    @SerializedName("kas_amount") val kasAmount: Int? = null,
    @SerializedName("previous_balance") val previousBalance: Int? = null,
    @SerializedName("real_balance") val realBalance: Int? = null,
    @SerializedName("class_name") val className: String? = null,
    val year: String? = null
)

data class PaymentRecord(
    val id: Int,
    @SerializedName("member_id") val memberId: Int,
    @SerializedName("member_name") val memberName: String,
    val amount: Int,
    val date: String,
    val note: String = "",
    val type: String = "Kas"
)

data class UserProfile(
    val name: String,
    val role: String,
    val email: String = "",
    val nis: String = "",
    val phone: String = "",
    val bio: String = "",
    val umur: String = "17 Tahun",
    val avatarUrl: String = ""
)

// Hasil denda satu siswa: unpaidMonths = berapa bulan berbeda yang punya minggu telat, dendaAmount = total rupiah denda.
// CARI-DENDA | Hasil hitung denda per siswa.
data class DendaInfo(
    val memberId: Int,
    val memberName: String,
    val unpaidMonths: Int,
    val dendaPercentage: Double,
    val dendaAmount: Int
)
