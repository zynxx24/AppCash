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
import java.time.temporal.ChronoUnit

class AppCashRepository(private val context: Context) {

    companion object {
        const val DENDA_RATE_PERCENT = 5
    }

    // --- SESSION STATE ---
    private var currentRole: String? = null
    private var currentMemberId: Int? = null
    private var isLoggedIn: Boolean = false
    private var adminEmail: String = ""

    // --- ADMIN CREDENTIALS ---
    // Password disimpan sebagai hash SHA-256 (bukan plaintext). Nilai = sha256("admin123")
    private val ADMIN_EMAIL = "admin@gmail.com"
    private val ADMIN_PASSWORD_HASH = "240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9"

    private fun sha256(text: String): String =
        MessageDigest.getInstance("SHA-256").digest(text.toByteArray())
            .joinToString("") { "%02x".format(it) }

    // --- PERSISTENCE (SharedPreferences + Gson) ---
    // Data tetap tersimpan walau aplikasi ditutup / layar diputar.
    private val prefs = context.getSharedPreferences("appcash_db", Context.MODE_PRIVATE)
    private val gson = Gson()

    private data class Snapshot(
        val members: List<Member>,
        val payments: Map<String, Map<String, Boolean>>,
        val expenses: List<Expense>,
        val history: List<PaymentRecord>,
        val config: Config
    )

    // Sinyal perubahan data: layar memuat ulang otomatis setiap kali nilainya naik
    private val _version = MutableStateFlow(0)
    val version: StateFlow<Int> = _version.asStateFlow()
    fun refresh() { _version.value++ }

    private fun persist() {
        _version.value++
        val snap = Snapshot(membersList, paymentRecords, expensesList, paymentHistoryList, configData)
        prefs.edit().putString("snapshot", gson.toJson(snap)).apply()
    }

    private fun restore(): Boolean {
        val json = prefs.getString("snapshot", null) ?: return false
        return try {
            val snap = gson.fromJson<Snapshot>(json, object : TypeToken<Snapshot>() {}.type) ?: return false
            membersList.clear(); membersList.addAll(snap.members)
            paymentRecords.clear()
            snap.payments.forEach { (k, v) -> paymentRecords[k] = v.toMutableMap() }
            expensesList.clear(); expensesList.addAll(snap.expenses)
            paymentHistoryList.clear(); paymentHistoryList.addAll(snap.history)
            configData = snap.config
            true
        } catch (e: Exception) {
            false // data korup → pakai data awal
        }
    }

    // --- IN-MEMORY STANDALONE DATABASE (XII PPLG) ---
    private val membersList = mutableListOf(
        Member(1,  "Boyke Vilano Hamonangan Sihite",          "2026-PPLG-001", "Anggota",      "Siswa XII PPLG", "17 Tahun", "", ""),
        Member(2,  "Bintang Leonita Christya Renata",         "2026-PPLG-002", "Anggota",      "Siswa XII PPLG", "17 Tahun", "", ""),
        Member(3,  "Carolina Timuthy Janggur",                "2026-PPLG-003", "Anggota",      "Siswa XII PPLG", "17 Tahun", "", ""),
        Member(4,  "Dewa Gede Dalem Oka Adnyana Sandi",      "2026-PPLG-004", "Anggota",      "Siswa XII PPLG", "17 Tahun", "", ""),
        Member(5,  "Galistan Ramadhan Kurnia Taunaes",        "2026-PPLG-005", "Anggota",      "Siswa XII PPLG", "17 Tahun", "", ""),
        Member(6,  "Gede Agus Wira Darma Putra",              "2026-PPLG-006", "Ketua Kelas",  "Android & Web Full-Stack Developer, pemimpin kelas XII PPLG yang visioner", "17 Tahun", "6285600487433", ""),
        Member(7,  "I Gede Abi Wirya Dinata",                 "2026-PPLG-007", "Anggota",      "Siswa XII PPLG", "17 Tahun", "", ""),
        Member(8,  "I Gede Darma Suptiawan",                  "2026-PPLG-008", "Anggota",      "Siswa XII PPLG", "17 Tahun", "", ""),
        Member(9,  "I Komang Raditya Putra",                  "2026-PPLG-009", "Anggota",      "Siswa XII PPLG", "17 Tahun", "", ""),
        Member(10, "I Komang Riski Setiawan",                 "2026-PPLG-010", "Anggota",      "Siswa XII PPLG", "17 Tahun", "", ""),
        Member(11, "I Nyoman Gede Arta Wiguna",               "2026-PPLG-011", "Anggota",      "Siswa XII PPLG", "17 Tahun", "", ""),
        Member(12, "I Putu Dika Laksmana Putra",              "2026-PPLG-012", "Anggota",      "Siswa XII PPLG", "17 Tahun", "", ""),
        Member(13, "I Putu Ditya Artha Wijaya",               "2026-PPLG-013", "Anggota",      "Siswa XII PPLG", "17 Tahun", "", ""),
        Member(14, "I Putu Pande Andika",                     "2026-PPLG-014", "Bendahara 2",  "Manajemen Keuangan & Data Analyst PPLG", "16 Tahun", "", ""),
        Member(15, "I Putu Suyoga Mahendra",                  "2026-PPLG-015", "Anggota",      "Siswa XII PPLG", "17 Tahun", "", ""),
        Member(16, "I Wayan Bagus Putrawan",                  "2026-PPLG-016", "Anggota",      "Siswa XII PPLG", "17 Tahun", "", ""),
        Member(17, "I Wayan Pasek Kevin Ariadi",              "2026-PPLG-017", "Anggota",      "Siswa XII PPLG", "17 Tahun", "", ""),
        Member(18, "Kadek Yuda Prasetya",                     "2026-PPLG-018", "Anggota",      "Siswa XII PPLG", "17 Tahun", "", ""),
        Member(19, "Kadek Yuni Callista Putri Dewi",          "2026-PPLG-019", "Anggota",      "Siswa XII PPLG", "17 Tahun", "", ""),
        Member(20, "Komang Diah Putri Pratiwi",               "2026-PPLG-020", "Anggota",      "Siswa XII PPLG", "17 Tahun", "", ""),
        Member(21, "Luh Ria Mirasih",                         "2026-PPLG-021", "Anggota",      "Siswa XII PPLG", "17 Tahun", "", ""),
        Member(22, "Ni Kadek Adelia Cahya Kencana Putri",     "2026-PPLG-022", "Bendahara 1",  "Pengelola Kas Kelas XII PPLG & UI/UX Designer", "16 Tahun", "", ""),
        Member(23, "Ni Kadek Lina Antika Dewi",               "2026-PPLG-023", "Anggota",      "Siswa XII PPLG", "17 Tahun", "", ""),
        Member(24, "Ni Komang Kirana Paramita Ardanari",      "2026-PPLG-024", "Anggota",      "Siswa XII PPLG", "17 Tahun", "", ""),
        Member(25, "Ni Komang Septiarini",                    "2026-PPLG-025", "Anggota",      "Siswa XII PPLG", "17 Tahun", "", ""),
        Member(26, "Ni Luh Putu Kesya Astri Melani",          "2026-PPLG-026", "Anggota",      "Siswa XII PPLG", "17 Tahun", "", ""),
        Member(27, "Ni Putu Cahaya Lestari Dewi",             "2026-PPLG-027", "Anggota",      "Siswa XII PPLG", "17 Tahun", "", ""),
        Member(28, "Ni Putu Intan Lestari Darmayanti",        "2026-PPLG-028", "Anggota",      "Siswa XII PPLG", "17 Tahun", "", ""),
        Member(29, "Okta Pradipta Attala Dzaki",              "2026-PPLG-029", "Anggota",      "Siswa XII PPLG", "17 Tahun", "", ""),
        Member(30, "Putu Bayu Satria Wangsa Bukian",          "2026-PPLG-030", "Wakil Ketua",  "Front-end Enthusiast & Koordinator Kegiatan Kelas", "17 Tahun", "", ""),
        Member(31, "Putu Nanda Lindia Maharani",              "2026-PPLG-031", "Anggota",      "Siswa XII PPLG", "17 Tahun", "", ""),
        Member(32, "Putu Putri Cahyani",                      "2026-PPLG-032", "Anggota",      "Siswa XII PPLG", "17 Tahun", "", ""),
        Member(33, "Raditya Rondi",                           "2026-PPLG-033", "Anggota",      "Siswa XII PPLG", "17 Tahun", "", "")
    )

    // 20 Mingguan Kas (Januari - Mei 2026)
    private val datesList = mutableListOf(
        "M01 (05 Jan)", "M02 (12 Jan)", "M03 (19 Jan)", "M04 (26 Jan)",
        "M05 (02 Feb)", "M06 (09 Feb)", "M07 (16 Feb)", "M08 (23 Feb)",
        "M09 (02 Mar)", "M10 (09 Mar)", "M11 (16 Mar)", "M12 (23 Mar)",
        "M13 (30 Mar)", "M14 (06 Apr)", "M15 (13 Apr)", "M16 (20 Apr)",
        "M17 (27 Apr)", "M18 (04 Mei)", "M19 (11 Mei)", "M20 (18 Mei)"
    )

    // Monthly groupings for denda calculation (4 weeks per month approx)
    private val monthlyGroups = listOf(
        "Januari"  to listOf("M01 (05 Jan)", "M02 (12 Jan)", "M03 (19 Jan)", "M04 (26 Jan)"),
        "Februari" to listOf("M05 (02 Feb)", "M06 (09 Feb)", "M07 (16 Feb)", "M08 (23 Feb)"),
        "Maret"    to listOf("M09 (02 Mar)", "M10 (09 Mar)", "M11 (16 Mar)", "M12 (23 Mar)"),
        "April"    to listOf("M13 (30 Mar)", "M14 (06 Apr)", "M15 (13 Apr)", "M16 (20 Apr)"),
        "Mei"      to listOf("M17 (27 Apr)", "M18 (04 Mei)", "M19 (11 Mei)", "M20 (18 Mei)")
    )

    private val paymentRecords = mutableMapOf<String, MutableMap<String, Boolean>>()

    private val expensesList = mutableListOf(
        Expense(1, "Beli Spidol Boardmarker (3 pcs) & Penghapus Board", 45000, "2026-01-10", "Perlengkapan"),
        Expense(2, "Fotocopy Modul & Bahan Ajar Praktik RPL",            75000, "2026-01-22", "Akademik"),
        Expense(3, "Beli Alat Kebersihan (Sapu, Pel, Trash Bag Kelas)",  120000,"2026-02-05", "Kebersihan"),
        Expense(4, "Konsumsi Rapat Panitia Wisata XII PPLG",             160000,"2026-02-18", "Kegiatan"),
        Expense(5, "Print & Jilid Buklet Tugas Kelompok PPLG",           55000, "2026-03-02", "Akademik"),
        Expense(6, "Beli Kertas HVS A4 (2 Rim) & Map Dokumen",           110000,"2026-03-20", "Perlengkapan"),
        Expense(7, "Konsumsi Classmeeting & Lomba E-Sport PPLG",         200000,"2026-04-05", "Kegiatan"),
        Expense(8, "Banner & Dekorasi Stand Pameran Karya PPLG",         180000,"2026-04-15", "Kegiatan"),
        Expense(9, "Pulsa Internet Presentasi & Workshop RPL",            90000, "2026-05-02", "Akademik"),
        Expense(10,"Konsumsi Perayaan Kelulusan Ujian Akhir XII PPLG",   250000,"2026-05-10", "Kegiatan")
    )

    // Payment history for new payment input feature
    private val paymentHistoryList = mutableListOf<PaymentRecord>()

    private var configData = Config(
        kasAmount       = 5000,
        previousBalance = 500000,
        realBalance     = 3365000,
        className       = "XII PPLG",
        year            = "2026/2027"
    )

    init {
        if (!restore()) seedPayments()
    }

    private fun seedPayments() {
        membersList.forEach { member ->
            val memberMap = mutableMapOf<String, Boolean>()
            datesList.forEachIndexed { index, date ->
                val isPaid = when {
                    index < 16 -> true
                    index < 18 -> member.id % 2 == 1 || member.id <= 12
                    else       -> member.id in listOf(1, 2, 3, 4, 5, 6, 7, 9, 11, 14, 22, 30)
                }
                memberMap[date] = isPaid
            }
            paymentRecords[member.id.toString()] = memberMap
        }
    }

    // --- SESSION MANAGEMENT ---
    suspend fun getRole(): String? = currentRole
    suspend fun hasToken(): Boolean = isLoggedIn
    suspend fun getCurrentMemberId(): Int? = currentMemberId

    suspend fun getPaymentStatusForMember(memberId: Int): Result<Pair<Int, Int>> = withContext(Dispatchers.IO) {
        val memberPayments = paymentRecords[memberId.toString()] ?: emptyMap()
        val totalWeeks = datesList.size
        val paidWeeks = datesList.count { date -> memberPayments[date] == true }
        Result.success(Pair(paidWeeks, totalWeeks))
    }

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

    suspend fun setMemberId(id: Int) = withContext(Dispatchers.IO) {
        currentMemberId = id
    }

    suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        currentRole = null
        currentMemberId = null
        isLoggedIn = false
        adminEmail = ""
        Result.success(Unit)
    }

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

    private val monthMap = mapOf(
        "Jan" to 1, "Feb" to 2, "Mar" to 3, "Apr" to 4,
        "Mei" to 5, "Jun" to 6, "Jul" to 7, "Ags" to 8,
        "Sep" to 9, "Okt" to 10, "Nov" to 11, "Des" to 12
    )

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

    /**
     * Rumus denda: denda per minggu = kas x 5% x bulan_terlambat.
     * Contoh: kas Rp5.000, telat 2 bulan → 5.000 x 5% x 2 = Rp500.
     */
    private fun calculateDenda(member: Member, today: LocalDate = LocalDate.now()): DendaInfo? {
        val memberPayments = paymentRecords[member.id.toString()] ?: emptyMap()
        var total = 0
        val unpaidMonths = mutableSetOf<Int>()
        datesList.forEach { date ->
            if (memberPayments[date] == true) return@forEach
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

    suspend fun getDendaForAllMembers(): Result<List<DendaInfo>> = withContext(Dispatchers.IO) {
        Result.success(membersList.mapNotNull { calculateDenda(it) })
    }

    suspend fun getDendaForMember(memberId: Int): Result<DendaInfo?> = withContext(Dispatchers.IO) {
        val member = membersList.find { it.id == memberId }
            ?: return@withContext Result.failure(Exception("Member not found"))
        Result.success(calculateDenda(member))
    }

    // --- DASHBOARD ---
    suspend fun getDashboard(): Result<Dashboard> = withContext(Dispatchers.IO) {
        var totalPaymentsCount = 0
        var totalKasIncome = 0
        val weeklyIncomeList = mutableListOf<Int>()
        val weeklyExpensesList = mutableListOf<Int>()
        val weeklyLabelsList = mutableListOf<String>()

        datesList.forEachIndexed { index, date ->
            var weekPaid = 0
            paymentRecords.values.forEach { m -> if (m[date] == true) { weekPaid++; totalPaymentsCount++ } }
            val weekIncome = weekPaid * configData.kasAmount
            totalKasIncome += weekIncome

            // Grafik per 2 minggu: pemasukan = jumlah 2 minggu, pengeluaran = sesuai tanggal transaksi
            if (index % 2 == 1) {
                val prevDate = datesList[index - 1]
                val prevPaid = paymentRecords.values.count { it[prevDate] == true }
                weeklyLabelsList.add("M${index + 1}")
                weeklyIncomeList.add(weekIncome + prevPaid * configData.kasAmount)

                val start = parseDateLabel(prevDate)
                val end = datesList.getOrNull(index + 1)?.let { parseDateLabel(it) } ?: LocalDate.MAX
                weeklyExpensesList.add(expensesList.filter { e ->
                    val d = runCatching { LocalDate.parse(e.date) }.getOrNull() ?: return@filter false
                    // Pengeluaran sebelum M01 ikut dihitung di bar pertama
                    (index == 1 || (start != null && !d.isBefore(start))) && d.isBefore(end)
                }.sumOf { it.amount })
            }
        }

        val totalExpenseAmount = expensesList.sumOf { it.amount }
        val netBalance = configData.previousBalance + totalKasIncome - totalExpenseAmount
        val latestDate = datesList.lastOrNull() ?: ""
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
                weeklyLabels      = weeklyLabelsList,
                weeklyKasIncome   = weeklyIncomeList,
                weeklyExpenses    = weeklyExpensesList
            )
        )
    }

    suspend fun getMembers(): Result<List<Member>> = withContext(Dispatchers.IO) { Result.success(membersList.toList()) }

    suspend fun getPayments(): Result<PaymentsWrapper> = withContext(Dispatchers.IO) {
        // Deep copy: UI tidak boleh mengubah map internal repository secara langsung
        Result.success(PaymentsWrapper(dates = datesList.toList(), records = paymentRecords.mapValues { it.value.toMap() }))
    }

    suspend fun savePayments(wrapper: PaymentsWrapper): Result<Unit> = withContext(Dispatchers.IO) {
        wrapper.records.forEach { (memberId, datesMap) ->
            val existing = paymentRecords[memberId] ?: mutableMapOf()
            datesMap.forEach { (date, paid) -> existing[date] = paid }
            paymentRecords[memberId] = existing
        }
        persist()
        Result.success(Unit)
    }

    suspend fun updatePaymentStatus(memberId: Int, date: String, paid: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        val memberMap = paymentRecords[memberId.toString()] ?: mutableMapOf()
        memberMap[date] = paid
        paymentRecords[memberId.toString()] = memberMap
        persist()
        Result.success(Unit)
    }

    suspend fun getExpenses(): Result<List<Expense>> = withContext(Dispatchers.IO) { Result.success(expensesList.toList()) }

    suspend fun addExpense(req: ExpenseRequest): Result<IdResponse> = withContext(Dispatchers.IO) {
        if (req.description.isBlank()) return@withContext Result.failure(Exception("Keterangan wajib diisi"))
        if (req.amount <= 0) return@withContext Result.failure(Exception("Nominal harus lebih dari 0"))
        val newId = (expensesList.maxOfOrNull { it.id } ?: 0) + 1
        expensesList.add(0, Expense(newId, req.description.trim(), req.amount, req.date, req.category))
        persist()
        Result.success(IdResponse(newId))
    }

    suspend fun deleteExpense(id: Int): Result<Unit> = withContext(Dispatchers.IO) {
        expensesList.removeAll { it.id == id }
        persist()
        Result.success(Unit)
    }

    suspend fun updateExpense(id: Int, description: String, amount: Int, date: String): Result<Unit> = withContext(Dispatchers.IO) {
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

    suspend fun getConfig(): Result<Config> = withContext(Dispatchers.IO) { Result.success(configData) }

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

    // --- Payment Input Feature ---
    suspend fun addPaymentRecord(memberId: Int, amount: Int, date: String, note: String): Result<PaymentRecord> = withContext(Dispatchers.IO) {
        val member = membersList.find { it.id == memberId } ?: return@withContext Result.failure(Exception("Member not found"))
        if (amount <= 0) return@withContext Result.failure(Exception("Nominal harus lebih dari 0"))
        val newId = (paymentHistoryList.maxOfOrNull { it.id } ?: 0) + 1
        val record = PaymentRecord(id = newId, memberId = memberId, memberName = member.name, amount = amount, date = date, note = note)
        paymentHistoryList.add(0, record)
        persist()
        Result.success(record)
    }

    suspend fun getPaymentHistory(): Result<List<PaymentRecord>> = withContext(Dispatchers.IO) {
        Result.success(paymentHistoryList.toList())
    }
}
