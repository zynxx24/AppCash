# Tanya Jawab Kode AppCash – Kelompok 6

Persiapan menjawab pertanyaan guru. Setiap jawaban diberi letak kodenya (file:baris). Di Android Studio tekan **Ctrl+Shift+N** lalu ketik nama file, atau **Ctrl+Shift+F** lalu ketik `TANYA-GURU`.

### 1. Aplikasi ini dibuat dengan apa?

Kotlin dengan Jetpack Compose (Material 3) di Android Studio, build memakai Gradle. Minimal Android 8.0 (API 26).

Letak: `app/build.gradle.kts:12`

### 2. Kode pertama yang jalan saat aplikasi dibuka?

`MainActivity.onCreate` mengambil repository dari `AppCashApplication`, lalu menampilkan layar login atau menu utama.

Letak: `app/src/main/java/com/appcash/ui/MainActivity.kt:59`

### 3. Bagaimana login admin dan siswa?

Admin memakai email dan password yang dicocokkan dengan hash SHA-256. Siswa cukup memilih nama.

Letak: `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:285`

### 4. Kenapa password admin tidak terlihat di kode?

Yang disimpan hanya hash SHA-256 (fungsi `sha256`), bukan password asli.

Letak: `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:49`

### 5. Data disimpan di mana? Hilang tidak kalau aplikasi ditutup?

Disimpan di HP memakai SharedPreferences (JSON lewat Gson) setiap ada perubahan, jadi tidak hilang. Dimuat lagi oleh `restore()`.

Letak: `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:75`

### 6. Kenapa belum memakai Firebase atau server?

Versi sekarang offline supaya bisa dipakai tanpa sinyal. Struktur Firestore sudah dirancang di SRS Bab 9, dan `ApiService.kt` adalah rancangan API yang belum dipakai.

Letak: `app/src/main/java/com/appcash/network/ApiService.kt:19`

### 7. Rumus saldo?

Saldo = saldo awal + (jumlah centang lunas x Rp5.000) + donasi - total pengeluaran. Data asli: Rp1.389.000.

Letak: `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:445`

### 8. Rumus denda?

Rp5.000 x 5% = Rp250 dikali jumlah bulan penuh terlambat, untuk setiap minggu yang belum dibayar. Terlambat kurang dari sebulan belum didenda.

Letak: `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:398`

### 9. Kalau tarif denda mau diubah?

Ubah konstanta persentase denda di companion object repository.

Letak: `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:28`

### 10. Kalau nominal kas mau diubah?

Nilai `kasAmount` di konfigurasi awal (Config).

Letak: `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:244`

### 11. Dispensasi BI/PKL diatur di mana?

Daftar siswa dan rentang tanggal bebas kas, dicek lewat `isExempt`. Minggu itu tidak ditagih dan tidak didenda.

Letak: `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:236`

### 12. Bagaimana admin mencatat pembayaran?

Menu Kas, Tambah Pembayaran, pilih siswa, centang minggu, Konfirmasi. Disimpan lewat `savePayments` / `updatePaymentStatus`.

Letak: `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:501`

### 13. Bagaimana siswa membayar?

Menu Kas, Bayar Kas, pilih BNI atau QRIS, centang minggu, kirim bukti ke WA admin.

Letak: `app/src/main/java/com/appcash/ui/screens/PaymentsScreen.kt:831`

### 14. Bagaimana mencatat pengeluaran dan validasinya?

Admin mengisi deskripsi, jumlah, tanggal. Keterangan wajib dan nominal harus lebih dari 0.

Letak: `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:524`

### 15. Grafik di Dashboard datanya dari mana?

Pemasukan dan pengeluaran dikelompokkan per bulan oleh fungsi `bucket` di `getDashboard`.

Letak: `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:456`

### 16. Kenapa siswa tidak melihat rekap denda teman?

Rekap denda bernama hanya untuk admin, supaya yang belum bayar tidak dipermalukan.

Letak: `app/src/main/java/com/appcash/ui/screens/DashboardScreen.kt:406`

### 17. Bagaimana tombol WhatsApp bekerja?

Membuka link wa.me dengan nomor dan pesan terisi lewat Intent ACTION_VIEW.

Letak: `app/src/main/java/com/appcash/ui/screens/MembersScreen.kt:283`

### 18. Data 33 siswa dan kas awal dari mana?

Disalin dari buku kas asli XII PPLG per 20 November 2026 (seedStatus).

Letak: `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:155`

### 19. Apa itu Repository di aplikasi ini?

Satu kelas pusat data. Semua layar membaca dan mengubah data lewat `AppCashRepository`, jadi rumus tidak tersebar.

Letak: `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:4`

### 20. Tabel ERD ada di kode mana?

Data class di Models.kt: Member, Expense, Donation, Config, PaymentsWrapper, dan lainnya.

Letak: `app/src/main/java/com/appcash/data/model/Models.kt:12`

### 21. Apa kekurangan aplikasi?

Transfer dicek manual, pengingat otomatis dan pengajuan dispensasi dari siswa ditunda, data belum online. Lihat SRS 5.2.

Letak: `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:4`
