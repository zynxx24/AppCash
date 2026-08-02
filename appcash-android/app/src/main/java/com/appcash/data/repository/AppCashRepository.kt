package com.appcash.data.repository

import android.content.Context
import com.appcash.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppCashRepository(private val context: Context) {

    // --- IN-MEMORY STANDALONE DATABASE (XII PPLG) ---
    private val membersList = mutableListOf(
        Member(1,  "Gede Agus Wira Darma Putra",     "2026-PPLG-006", "Ketua Kelas",  "Android & Web Full-Stack Developer, pemimpin kelas XII PPLG yang visioner", "17 Tahun", "6285600487433", "https://i.pravatar.cc/150?img=11"),
        Member(2,  "I Putu Pande Andika",            "2026-PPLG-014", "Wakil Ketua", "Front-end Enthusiast & Koordinator Kegiatan Kelas", "17 Tahun", "6281234567802", "https://i.pravatar.cc/150?img=45"),
        Member(3,  "Ni Kadek Adelia Kencana Putri",  "2026-PPLG-022", "Bendahara 1", "Pengelola Kas Kelas XII PPLG & UI/UX Designer", "16 Tahun", "6281234567803", "https://i.pravatar.cc/150?img=12"),
        Member(4,  "Putu Bayu Satria Wangsa Bukian", "2026-PPLG-030", "Bendahara 2", "Manajemen Keuangan & Data Analyst PPLG", "16 Tahun", "6281234567804", "https://i.pravatar.cc/150?img=9"),
        Member(5,  "Putu Ari Kusuma",                "2026-PPLG-004", "Sekretaris 1","Technical Writer & Documenter Kelompok PPLG", "17 Tahun", "6281234567805", "https://i.pravatar.cc/150?img=33"),
        Member(6,  "Ni Made Trisna Dewi",            "2026-PPLG-005", "Sekretaris 2","Graphic Designer & Illustrator Digital Kelas", "16 Tahun", "6281234567806", "https://i.pravatar.cc/150?img=44"),
        Member(7,  "Komang Adi Pratama",             "2026-PPLG-006", "Anggota",     "Backend Developer & Database Architect", "17 Tahun", "6281234567807", "https://i.pravatar.cc/150?img=53"),
        Member(8,  "Gede Surya Mahendra",            "2026-PPLG-007", "Anggota",     "Mobile Developer Android & iOS Enthusiast", "17 Tahun", "6281234567808", "https://i.pravatar.cc/150?img=57"),
        Member(9,  "Ni Putu Ayu Lestari",            "2026-PPLG-008", "Anggota",     "Frontend React & Next.js Developer", "16 Tahun", "6281234567809", "https://i.pravatar.cc/150?img=32"),
        Member(10, "Made Agus Darmasaputra",         "2026-PPLG-009", "Anggota",     "DevOps & System Administrator PPLG", "17 Tahun", "6281234567810", "https://i.pravatar.cc/150?img=60"),
        Member(11, "Kadek Rani Puspita",             "2026-PPLG-010", "Anggota",     "QA Engineer & Automated Testing Specialist", "16 Tahun", "6281234567811", "https://i.pravatar.cc/150?img=26"),
        Member(12, "Gede Eka Wiryawan",              "2026-PPLG-011", "Anggota",     "Game Developer & Unity 3D Programmer", "17 Tahun", "6281234567812", "https://i.pravatar.cc/150?img=68"),
        Member(13, "Ni Ketut Maya Sari",             "2026-PPLG-012", "Anggota",     "Cyber Security & Network Engineering", "16 Tahun", "6281234567813", "https://i.pravatar.cc/150?img=47"),
        Member(14, "Wayan Bagas Saputra",            "2026-PPLG-013", "Anggota",     "IoT & Embedded System Developer", "17 Tahun", "6281234567814", "https://i.pravatar.cc/150?img=15"),
        Member(15, "Ni Wayan Eka Puspawati",         "2026-PPLG-014", "Anggota",     "Content Creator & Social Media PPLG", "16 Tahun", "6281234567815", "https://i.pravatar.cc/150?img=25"),
        Member(16, "Putu Dewa Nugraha",              "2026-PPLG-015", "Anggota",     "Cloud Computing & API Developer", "17 Tahun", "6281234567816", "https://i.pravatar.cc/150?img=65"),
        Member(17, "Kadek Yogi Wahyudi",             "2026-PPLG-016", "Anggota",     "Machine Learning & AI Enthusiast", "17 Tahun", "6281234567817", "https://i.pravatar.cc/150?img=52"),
        Member(18, "Ni Made Putri Cahyani",          "2026-PPLG-017", "Anggota",     "Web Designer & Figma Expert", "16 Tahun", "6281234567818", "https://i.pravatar.cc/150?img=36"),
        Member(19, "Gede Arya Pramana",              "2026-PPLG-018", "Anggota",     "Database Administrator & SQL Expert", "17 Tahun", "6281234567819", "https://i.pravatar.cc/150?img=63"),
        Member(20, "Ni Luh Komang Artini",           "2026-PPLG-019", "Anggota",     "Fullstack PHP & Laravel Developer", "16 Tahun", "6281234567820", "https://i.pravatar.cc/150?img=29")
    )

    // 20 Mingguan Kas (Januari - Mei 2026)
    private val datesList = mutableListOf(
        "M01 (05 Jan)", "M02 (12 Jan)", "M03 (19 Jan)", "M04 (26 Jan)",
        "M05 (02 Feb)", "M06 (09 Feb)", "M07 (16 Feb)", "M08 (23 Feb)",
        "M09 (02 Mar)", "M10 (09 Mar)", "M11 (16 Mar)", "M12 (23 Mar)",
        "M13 (30 Mar)", "M14 (06 Apr)", "M15 (13 Apr)", "M16 (20 Apr)",
        "M17 (27 Apr)", "M18 (04 Mei)", "M19 (11 Mei)", "M20 (18 Mei)"
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
                    else       -> member.id in listOf(1, 2, 3, 4, 5, 7, 9, 11)
                }
                memberMap[date] = isPaid
            }
            paymentRecords[member.id.toString()] = memberMap
        }
    }

    suspend fun getRole(): String? = "admin"
    suspend fun hasToken(): Boolean = true

    suspend fun login(@Suppress("UNUSED_PARAMETER") req: LoginRequest): Result<LoginResponse> = withContext(Dispatchers.IO) {
        Result.success(LoginResponse(token = "dummy_token", role = "admin"))
    }

    suspend fun login(role: String, @Suppress("UNUSED_PARAMETER") username: String? = null, @Suppress("UNUSED_PARAMETER") password: String? = null, @Suppress("UNUSED_PARAMETER") memberId: Int? = null, @Suppress("UNUSED_PARAMETER") name: String? = null): Result<LoginResponse> = withContext(Dispatchers.IO) {
        Result.success(LoginResponse(token = "dummy_token", role = role))
    }

    suspend fun setMemberId(@Suppress("UNUSED_PARAMETER") id: Int) = withContext(Dispatchers.IO) {}

    suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        Result.success(Unit)
    }

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
}
