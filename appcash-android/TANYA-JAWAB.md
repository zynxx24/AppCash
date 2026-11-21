# Tanya Jawab Kode AppCash – Kelompok 6

Persiapan menjawab pertanyaan guru. Setiap jawaban diberi letak kodenya (file:baris). Di Android Studio tekan **Ctrl+Shift+N** lalu ketik nama file, atau **Ctrl+Shift+F** lalu ketik `TANYA-GURU`.

### 1. Aplikasi ini dibuat dengan apa?

Kotlin dengan Jetpack Compose (Material 3) di Android Studio, build memakai Gradle. Minimal Android 8.0 (API 26).

Letak: `app/build.gradle.kts:12`

### 2. Kode pertama yang jalan saat aplikasi dibuka?

`MainActivity.onCreate` mengambil repository dari `AppCashApplication`, lalu menampilkan layar login atau menu utama.

Letak: `app/src/main/java/com/appcash/ui/MainActivity.kt:68`

### 3. Bagaimana login admin dan siswa?

Admin memakai email dan password yang dicocokkan dengan hash SHA-256. Siswa cukup memilih nama.

Letak: `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:336`

### 4. Kenapa password admin tidak terlihat di kode?

Yang disimpan hanya hash SHA-256 (fungsi `sha256`), bukan password asli.

Letak: `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:73`

### 5. Data disimpan di mana? Hilang tidak kalau aplikasi ditutup?

Disimpan di HP memakai SharedPreferences (JSON lewat Gson) setiap ada perubahan, jadi tidak hilang. Dimuat lagi oleh `restore()`.

Letak: `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:104`

### 6. Kenapa belum memakai Firebase atau server?

Versi sekarang offline supaya bisa dipakai tanpa sinyal. Struktur Firestore sudah dirancang di SRS Bab 9, dan `ApiService.kt` adalah rancangan API yang belum dipakai.

Letak: `app/src/main/java/com/appcash/network/ApiService.kt:21`

### 7. Rumus saldo?

Saldo = saldo awal + (jumlah centang lunas x Rp5.000) + donasi - total pengeluaran. Data asli: Rp1.389.000.

Letak: `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:517`

### 8. Rumus denda?

Rp5.000 x 5% = Rp250 dikali jumlah bulan penuh terlambat, untuk setiap minggu yang belum dibayar. Terlambat kurang dari sebulan belum didenda.

Letak: `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:459`

### 9. Kalau tarif denda mau diubah?

Ubah konstanta persentase denda di companion object repository.

Letak: `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:48`

### 10. Kalau nominal kas mau diubah?

Nilai `kasAmount` di konfigurasi awal (Config).

Letak: `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:287`

### 11. Dispensasi BI/PKL diatur di mana?

Daftar siswa dan rentang tanggal bebas kas, dicek lewat `isExempt`. Minggu itu tidak ditagih dan tidak didenda.

Letak: `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:279`

### 12. Bagaimana admin mencatat pembayaran?

Menu Kas, Tambah Pembayaran, pilih siswa, centang minggu, Konfirmasi. Disimpan lewat `savePayments` / `updatePaymentStatus`.

Letak: `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:576`

### 13. Bagaimana siswa membayar?

Menu Kas, Bayar Kas, pilih BNI atau QRIS, centang minggu, kirim bukti ke WA admin.

Letak: `app/src/main/java/com/appcash/ui/screens/PaymentsScreen.kt:869`

### 14. Bagaimana mencatat pengeluaran dan validasinya?

Admin mengisi deskripsi, jumlah, tanggal. Keterangan wajib dan nominal harus lebih dari 0.

Letak: `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:601`

### 15. Grafik di Dashboard datanya dari mana?

Pemasukan dan pengeluaran dikelompokkan per bulan oleh fungsi `bucket` di `getDashboard`.

Letak: `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:529`

### 16. Kenapa siswa tidak melihat rekap denda teman?

Rekap denda bernama hanya untuk admin, supaya yang belum bayar tidak dipermalukan.

Letak: `app/src/main/java/com/appcash/ui/screens/DashboardScreen.kt:438`

### 17. Bagaimana tombol WhatsApp bekerja?

Membuka link wa.me dengan nomor dan pesan terisi lewat Intent ACTION_VIEW.

Letak: `app/src/main/java/com/appcash/ui/screens/MembersScreen.kt:288`

### 18. Data 33 siswa dan kas awal dari mana?

Disalin dari buku kas asli XII PPLG per 20 November 2026 (seedStatus).

Letak: `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:191`

### 19. Apa itu Repository di aplikasi ini?

Satu kelas pusat data. Semua layar membaca dan mengubah data lewat `AppCashRepository`, jadi rumus tidak tersebar.

Letak: `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:4`

### 20. Tabel ERD ada di kode mana?

Data class di Models.kt: Member, Expense, Donation, Config, PaymentsWrapper, dan lainnya.

Letak: `app/src/main/java/com/appcash/data/model/Models.kt:20`

### 21. Apa kekurangan aplikasi?

Transfer dicek manual, pengingat otomatis dan pengajuan dispensasi dari siswa ditunda, data belum online. Lihat SRS 5.2.

Letak: `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:4`

### 22. Grafiknya di mana dan jenis grafik apa?

Satu grafik di Dashboard: grafik garis dua warna (hijau pemasukan, merah pengeluaran) per bulan, fungsi `WeeklyKasLineChart`. Digambar manual pakai Canvas, tanpa library.

Letak: `app/src/main/java/com/appcash/ui/screens/DashboardScreen.kt:614`

### 23. Data grafik dihitung di mana?

`getDashboard` di repository: pemasukan per bulan = centang lunas x Rp5.000 + donasi, pengeluaran per bulan = jumlah pengeluaran bulan itu. Nama fungsinya weekly tetapi isinya per bulan.

Letak: `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:525`

### 24. Kenapa bulan Juni tidak ada di grafik?

Juni libur, tidak ada minggu kas, donasi, maupun pengeluaran, jadi tidak ada keranjang bulannya.

Letak: `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:529`

### 25. Ada grafik batang?

`MiniBarChart` ada di PaymentsScreen.kt tetapi tidak dipanggil dari layar mana pun. Yang tampil hanya grafik garis di Dashboard.

Letak: `app/src/main/java/com/appcash/ui/screens/PaymentsScreen.kt:1281`

### 26. Kartu saldo besar ada di mana?

Bagian Hero Balance Card di DashboardScreen. Angka dari `d.totalBalance`, dihitung di getDashboard.

Letak: `app/src/main/java/com/appcash/ui/screens/DashboardScreen.kt:174`

### 27. Layar bisa berubah otomatis dari mana?

`repository.version` naik setiap persist(). Layar memantau nilainya lewat collectAsState dan LaunchedEffect memuat data lagi.

Letak: `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:96`

### 28. Alur siswa membayar bagaimana?

Pilih minggu, bayar lewat BNI atau QRIS, tombol 1 Kirim Bukti ke WA Admin, lalu tombol 2 Konfirmasi (baru aktif setelah langkah 1). Konfirmasi dilakukan siswa sendiri tanpa verifikasi admin.

Letak: `app/src/main/java/com/appcash/ui/screens/PaymentsScreen.kt:1092`

### 29. Jadwal pelajaran ada di mana?

ScheduleScreen.kt sudah dibuat tetapi belum dipasang ke bar bawah, jadi tidak tampil.

Letak: `app/src/main/java/com/appcash/ui/screens/ScheduleScreen.kt:51`

### 30. Perbedaan tampilan Admin dan Siswa?

Parameter isAdmin dari AppRoot dikirim ke tiap layar. Admin: centang pembayaran, tambah pengeluaran, edit anggota, rekap denda. Siswa: status pribadi dan bayar via BNI atau QRIS.

Letak: `app/src/main/java/com/appcash/ui/MainActivity.kt:125`

### 31. Kenapa ada file ApiService padahal offline?

Rancangan versi online (Retrofit) yang belum dipakai. Semua data lewat AppCashRepository.

Letak: `app/src/main/java/com/appcash/network/ApiService.kt:4`
