package com.appcash.data.repository

import android.content.Context
import com.appcash.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class AppCashRepository(private val context: Context) {

    // --- SESSION STATE ---
    private var currentRole: String? = null
    private var currentMemberId: Int? = null
    private var isLoggedIn: Boolean = false
    private var adminEmail: String = ""

    // --- ADMIN CREDENTIALS ---
    private val ADMIN_EMAIL = "admin@gmail.com"
    private val ADMIN_PASSWORD = "admin123"

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

    suspend fun login(@Suppress("UNUSED_PARAMETER") req: LoginRequest): Result<LoginResponse> = withContext(Dispatchers.IO) {
        Result.success(LoginResponse(token = "dummy_token", role = "admin"))
    }

    suspend fun login(role: String, email: String? = null, password: String? = null, memberId: Int? = null, @Suppress("UNUSED_PARAMETER") name: String? = null): Result<LoginResponse> = withContext(Dispatchers.IO) {
        if (role == "admin") {
            if (email == ADMIN_EMAIL && password == ADMIN_PASSWORD) {
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
                membersList[idx] = membersList[idx].copy(name = name, phone = phone, bio = bio)
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

    suspend fun getDendaForAllMembers(): Result<List<DendaInfo>> = withContext(Dispatchers.IO) {
        val dendaList = mutableListOf<DendaInfo>()
        val kasPerWeek = configData.kasAmount
        val today = LocalDate.now()

        membersList.forEach { member ->
            val memberPayments = paymentRecords[member.id.toString()] ?: emptyMap()
            var totalDendaAmount = 0
            var unpaidMonthsCount = 0
            val unpaidMonthsSet = mutableSetOf<Int>()

            datesList.forEach { date ->
                if (memberPayments[date] != true) {
                    val dueDate = parseDateLabel(date)
                    if (dueDate != null && today.isAfter(dueDate)) {
                        val monthsLate = ChronoUnit.MONTHS.between(dueDate, today).toInt().coerceAtLeast(0)
                        if (monthsLate > 0) {
                            val dendaForWeek = (kasPerWeek * 5.0 * monthsLate / 100).toInt()
                            totalDendaAmount += dendaForWeek
                            unpaidMonthsSet.add(dueDate.monthValue)
                        }
                    }
                }
            }
            unpaidMonthsCount = unpaidMonthsSet.size

            if (totalDendaAmount > 0) {
                dendaList.add(DendaInfo(
                    memberId = member.id,
                    memberName = member.name,
                    unpaidMonths = unpaidMonthsCount,
                    dendaPercentage = unpaidMonthsCount * 5.0,
                    dendaAmount = totalDendaAmount
                ))
            }
        }
        Result.success(dendaList)
    }

    suspend fun getDendaForMember(memberId: Int): Result<DendaInfo?> = withContext(Dispatchers.IO) {
        val member = membersList.find { it.id == memberId }
            ?: return@withContext Result.failure(Exception("Member not found"))
        val memberPayments = paymentRecords[memberId.toString()] ?: emptyMap()
        val kasPerWeek = configData.kasAmount
        val today = LocalDate.now()

        var totalDendaAmount = 0
        val unpaidMonthsSet = mutableSetOf<Int>()

        datesList.forEach { date ->
            if (memberPayments[date] != true) {
                val dueDate = parseDateLabel(date)
                if (dueDate != null && today.isAfter(dueDate)) {
                    val monthsLate = ChronoUnit.MONTHS.between(dueDate, today).toInt().coerceAtLeast(0)
                    if (monthsLate > 0) {
                        val dendaForWeek = (kasPerWeek * 5.0 * monthsLate / 100).toInt()
                        totalDendaAmount += dendaForWeek
                        unpaidMonthsSet.add(dueDate.monthValue)
                    }
                }
            }
        }

        if (totalDendaAmount > 0) {
            Result.success(DendaInfo(
                memberId = member.id,
                memberName = member.name,
                unpaidMonths = unpaidMonthsSet.size,
                dendaPercentage = unpaidMonthsSet.size * 5.0,
                dendaAmount = totalDendaAmount
            ))
        } else {
            Result.success(null)
        }
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

            if (index % 2 == 1) {
                weeklyLabelsList.add("M${index + 1}")
                weeklyIncomeList.add(weekIncome * 2)
                val exp = when (index) {
                    1 -> 45000; 3 -> 195000; 7 -> 215000; 15 -> 380000; 19 -> 340000; else -> 0
                }
                weeklyExpensesList.add(exp)
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
        Result.success(PaymentsWrapper(dates = datesList.toList(), records = paymentRecords.toMap()))
    }

    suspend fun savePayments(wrapper: PaymentsWrapper): Result<Unit> = withContext(Dispatchers.IO) {
        wrapper.records.forEach { (memberId, datesMap) ->
            val existing = paymentRecords[memberId] ?: mutableMapOf()
            datesMap.forEach { (date, paid) -> existing[date] = paid }
            paymentRecords[memberId] = existing
        }
        Result.success(Unit)
    }

    suspend fun updatePaymentStatus(memberId: Int, date: String, paid: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        val memberMap = paymentRecords[memberId.toString()] ?: mutableMapOf()
        memberMap[date] = paid
        paymentRecords[memberId.toString()] = memberMap
        Result.success(Unit)
    }

    suspend fun getExpenses(): Result<List<Expense>> = withContext(Dispatchers.IO) { Result.success(expensesList.toList()) }

    suspend fun addExpense(req: ExpenseRequest): Result<IdResponse> = withContext(Dispatchers.IO) {
        val newId = (expensesList.maxOfOrNull { it.id } ?: 0) + 1
        expensesList.add(0, Expense(newId, req.description, req.amount, req.date, req.category))
        Result.success(IdResponse(newId))
    }

    suspend fun deleteExpense(id: Int): Result<Unit> = withContext(Dispatchers.IO) {
        expensesList.removeAll { it.id == id }
        Result.success(Unit)
    }

    suspend fun updateExpense(id: Int, description: String, amount: Int, date: String): Result<Unit> = withContext(Dispatchers.IO) {
        val idx = expensesList.indexOfFirst { it.id == id }
        if (idx >= 0) {
            val old = expensesList[idx]
            expensesList[idx] = old.copy(description = description, amount = amount, date = date)
            Result.success(Unit)
        } else {
            Result.failure(Exception("Expense not found"))
        }
    }

    suspend fun updateMember(id: Int, name: String, nis: String, role: String, bio: String, phone: String): Result<Unit> = withContext(Dispatchers.IO) {
        val idx = membersList.indexOfFirst { it.id == id }
        if (idx >= 0) {
            membersList[idx] = membersList[idx].copy(name = name, nis = nis, role = role, bio = bio, phone = phone)
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
            className       = req.className ?: configData.className,
            year            = req.year ?: configData.year
        )
        Result.success(Unit)
    }

    // --- Payment Input Feature ---
    suspend fun addPaymentRecord(memberId: Int, amount: Int, date: String, note: String): Result<PaymentRecord> = withContext(Dispatchers.IO) {
        val member = membersList.find { it.id == memberId } ?: return@withContext Result.failure(Exception("Member not found"))
        val newId = (paymentHistoryList.maxOfOrNull { it.id } ?: 0) + 1
        val record = PaymentRecord(id = newId, memberId = memberId, memberName = member.name, amount = amount, date = date, note = note)
        paymentHistoryList.add(0, record)
        Result.success(record)
    }

    suspend fun getPaymentHistory(): Result<List<PaymentRecord>> = withContext(Dispatchers.IO) {
        Result.success(paymentHistoryList.toList())
    }
}
