# Peta Kode AppCash – Kelompok 6

Panduan mencari bagian penting kode AppCash dengan cepat. Daftar di bawah dibuat otomatis dari komentar `CARI-…` dan `TANYA-GURU` di kode.

## Cara mencari

1. Buka folder proyek di VS Code / Android Studio.
2. Tekan **Ctrl+Shift+F** (cari di semua file).
3. Ketik **`CARI-`** untuk semua titik penting, atau penanda tertentu, misalnya **`CARI-DENDA`**.
4. Ketik **`TANYA-GURU`** untuk melihat jawaban singkat pertanyaan yang paling mungkin ditanyakan.
5. Setiap fungsi sudah diberi komentar satu baris di atasnya, dan setiap file utama punya kotak **`PETA FILE`** di baris paling atas.
6. Daftar pertanyaan lengkap beserta jawabannya ada di **`TANYA-JAWAB.md`**.

## Daftar penanda

| Penanda | Artinya | Jumlah titik |
|---|---|---|
| `CARI-MULAI` | Titik awal aplikasi (Activity, Application, splash) | 2 |
| `CARI-NAVIGASI` | Menu, tab, drawer, dan perpindahan halaman | 2 |
| `CARI-LOGIN` | Login, logout, akun admin, sesi | 5 |
| `CARI-PUSAT-DATA` | Pusat data aplikasi (repository / viewmodel) | 1 |
| `CARI-DATA-AWAL` | Data awal dari buku kas asli (siswa, pembayaran, pengeluaran) | 5 |
| `CARI-SIMPAN-DATA` | Penyimpanan permanen di HP (SharedPreferences) | 3 |
| `CARI-NOMINAL-KAS` | Nominal kas per minggu/bulan dan cara menghitung minggu | 3 |
| `CARI-SALDO` | Rumus dan tampilan saldo | 5 |
| `CARI-DENDA` | Rumus dan tampilan denda keterlambatan | 7 |
| `CARI-DISPENSASI` | Siswa yang dibebaskan kas (BI/PKL) | 3 |
| `CARI-DONASI` | Donasi sebagai pemasukan tambahan | 2 |
| `CARI-PEMBAYARAN` | Mencatat / menampilkan pembayaran kas | 8 |
| `CARI-PENGELUARAN` | Mencatat / menampilkan pengeluaran kas | 7 |
| `CARI-ANGGOTA` | Data siswa / anggota kelas | 7 |
| `CARI-GRAFIK` | Grafik pemasukan & pengeluaran | 2 |
| `CARI-PROFIL` | Halaman dan edit profil | 2 |
| `CARI-QRIS` | Kode QRIS dan rekening tujuan transfer | 1 |
| `CARI-WHATSAPP` | Pesan / konfirmasi lewat WhatsApp | 2 |
| `CARI-VALIDASI` | Pengecekan isian form (tanggal, nominal, nama) | 4 |
| `CARI-JADWAL` | Jadwal mata pelajaran | 1 |
| `CARI-FORMAT-RUPIAH` | Format angka ke Rupiah | 1 |
| `CARI-TEMA` | Warna dan tema tampilan | 1 |
| `CARI-TIDAK-DIPAKAI` | Kode yang ada tetapi belum dipakai di navigasi | 4 |

## Jawaban singkat di kode (`TANYA-GURU`)

- `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:74` — Data disimpan di mana? Di HP (SharedPreferences, format JSON lewat Gson). Belum online, Firebase masih rancangan.
- `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:283` — Bedanya admin dan siswa? Admin login email + password (hash SHA-256), siswa cukup pilih nama.
- `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:396` — Bagaimana denda dihitung? Kas Rp5.000 x 5% = Rp250 x n bulan penuh terlambat, per minggu yang belum lunas.
- `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:444` — Dari mana saldo? saldo awal + (centang lunas x Rp5.000) + donasi - pengeluaran. Hasil data asli Rp1.389.000.
- `app/src/main/java/com/appcash/ui/screens/DashboardScreen.kt:405` — Kenapa rekap denda tidak muncul di akun siswa? Supaya yang belum bayar tidak dipermalukan (sasaran menyama braya).

## Lokasi lengkap

### `CARI-MULAI`

- `app/src/main/java/com/appcash/AppCashApplication.kt:13` — Repository dibuat sekali di sini supaya data & sesi tidak hilang saat layar diputar.
- `app/src/main/java/com/appcash/ui/MainActivity.kt:55` — Aplikasi dibuka dari sini: ambil repository lalu tampilkan AppRoot.

### `CARI-NAVIGASI`

- `app/src/main/java/com/appcash/ui/MainActivity.kt:108` — Bottom bar + HorizontalPager. Nomor halaman: 0 Kas, 1 Pengeluaran, 2 Dashboard, 3 Anggota, 4 Profil.
- `app/src/main/java/com/appcash/ui/MainActivity.kt:237` — Label & ikon tab untuk admin (bendahara). Versi siswa ada di bottomNavItemsUser.

### `CARI-LOGIN`

- `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:43` — Akun admin: admin@gmail.com / admin123. Password disimpan sebagai hash SHA-256 di ADMIN_PASSWORD_HASH;
- `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:282` — role "admin" = email + password; role "user" = siswa cukup memilih nama.
- `app/src/main/java/com/appcash/ui/MainActivity.kt:77` — Menentukan yang tampil: LoginScreen (belum login) atau MainContent (sudah login).
- `app/src/main/java/com/appcash/ui/screens/LoginScreen.kt:35` — Tombol bawah mengganti mode Siswa <-> Email & Password; ada tombol lihat password.
- `app/src/main/java/com/appcash/ui/screens/ProfileScreen.kt:36` — Menampilkan profil, kartu denda (siswa), tombol Edit Profil dan Logout.

### `CARI-PUSAT-DATA`

- `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:24` — Semua layar mengambil dan mengubah data lewat kelas ini (tanpa server).

### `CARI-DATA-AWAL`

- `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:100` — Daftar 33 siswa XII PPLG: id, nama, NIS, jabatan, bio, umur, no. WA.
- `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:139` — Daftar minggu kas (setiap Senin). Tambah minggu baru di akhir dengan format "M38 (02 Nov)";
- `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:155` — Status bayar awal dari buku kas asli: satu string per siswa, 1 = lunas, 0 = belum, urut sesuai datesList.
- `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:192` — 24 pengeluaran asli (id, keterangan, nominal, tanggal yyyy-MM-dd, kategori).
- `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:257` — Mengubah seedStatus menjadi tabel pembayaran saat aplikasi pertama kali dipasang.

### `CARI-SIMPAN-DATA`

- `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:31` — Nama kunci penyimpanan. Naikkan versinya (mis. _v3) bila data awal diganti, supaya HP memuat data baru.
- `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:73` — Menyimpan semua data ke HP (Gson -> SharedPreferences). Dipanggil setiap ada perubahan.
- `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:81` — Memuat data tersimpan saat aplikasi dibuka; bila kosong/rusak dipakai data awal.

### `CARI-NOMINAL-KAS`

- `app/src/main/java/com/appcash/data/model/Models.kt:88` — Nilai bawaan konfigurasi kas (nominal, saldo awal, saldo fisik).
- `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:139` — Daftar minggu kas (setiap Senin). Tambah minggu baru di akhir dengan format "M38 (02 Nov)";
- `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:244` — kasAmount = iuran per minggu, previousBalance = saldo awal, realBalance = uang fisik di peti kas.

### `CARI-SALDO`

- `app/src/main/java/com/appcash/data/model/Models.kt:29` — Ringkasan angka untuk Dashboard dan kartu saldo.
- `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:244` — kasAmount = iuran per minggu, previousBalance = saldo awal, realBalance = uang fisik di peti kas.
- `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:442` — Saldo = saldo awal + (jumlah centang lunas x kas) + donasi - total pengeluaran.
- `app/src/main/java/com/appcash/ui/screens/DashboardScreen.kt:59` — Kartu saldo (dicocokkan dengan saldo fisik), status kas pribadi, dan rekap denda (khusus admin).
- `app/src/main/java/com/appcash/ui/screens/PaymentsScreen.kt:247` — Kartu oranye berisi total kas, pemasukan (kas + donasi), dan pengeluaran.

### `CARI-DENDA`

- `app/src/main/java/com/appcash/data/model/Models.kt:151` — Hasil hitung denda per siswa.
- `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:28` — Persentase denda per bulan keterlambatan (5 = 5%). Ubah angka ini untuk mengganti tarif denda.
- `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:390` — Rumus: kas x 5% x n untuk setiap minggu belum lunas; n = jumlah bulan penuh sejak tanggal kas.
- `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:421` — Denda hanya untuk minggu yang dicentang saat membayar (dipakai dialog Bayar Kas).
- `app/src/main/java/com/appcash/ui/screens/DashboardScreen.kt:59` — Kartu saldo (dicocokkan dengan saldo fisik), status kas pribadi, dan rekap denda (khusus admin).
- `app/src/main/java/com/appcash/ui/screens/DashboardScreen.kt:404` — Rekap denda bernama hanya untuk admin, siswa melihat dendanya sendiri di kartu Status Kas Anda.
- `app/src/main/java/com/appcash/ui/screens/PaymentsScreen.kt:298` — Admin memilih siswa lalu mencentang minggu yang dibayar; total = kas + denda minggu terpilih.

### `CARI-DISPENSASI`

- `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:230` — Siswa yang bebas kas pada rentang tanggal tertentu (mis. BI/PKL).
- `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:234` — Dipakai layar Kas & rumus denda untuk melewati minggu dispensasi.
- `app/src/main/java/com/appcash/ui/screens/PaymentsScreen.kt:629` — Tampilan siswa: status kas pribadi per minggu (Lunas / Dispensasi / Belum Bayar).

### `CARI-DONASI`

- `app/src/main/java/com/appcash/data/model/Models.kt:69` — Data satu donasi.
- `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:220` — Donasi dihitung sebagai pemasukan di luar kas mingguan.

### `CARI-PEMBAYARAN`

- `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:155` — Status bayar awal dari buku kas asli: satu string per siswa, 1 = lunas, 0 = belum, urut sesuai datesList.
- `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:421` — Denda hanya untuk minggu yang dicentang saat membayar (dipakai dialog Bayar Kas).
- `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:500` — Menyimpan centang lunas per siswa per minggu.
- `app/src/main/java/com/appcash/ui/screens/PaymentsScreen.kt:79` — Tampilan admin: kartu saldo, tombol Tambah Pembayaran, daftar minggu kas.
- `app/src/main/java/com/appcash/ui/screens/PaymentsScreen.kt:298` — Admin memilih siswa lalu mencentang minggu yang dibayar; total = kas + denda minggu terpilih.
- `app/src/main/java/com/appcash/ui/screens/PaymentsScreen.kt:500` — Edit centang lunas semua siswa untuk satu minggu.
- `app/src/main/java/com/appcash/ui/screens/PaymentsScreen.kt:629` — Tampilan siswa: status kas pribadi per minggu (Lunas / Dispensasi / Belum Bayar).
- `app/src/main/java/com/appcash/ui/screens/PaymentsScreen.kt:831` — Siswa membayar via BNI/QRIS, kirim bukti ke WA admin, lalu konfirmasi.

### `CARI-PENGELUARAN`

- `app/src/main/java/com/appcash/data/model/Models.kt:60` — Data satu pengeluaran (tanggal format yyyy-MM-dd).
- `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:192` — 24 pengeluaran asli (id, keterangan, nominal, tanggal yyyy-MM-dd, kategori).
- `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:523` — Tambah pengeluaran; keterangan wajib diisi dan nominal > 0.
- `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:540` — Edit pengeluaran yang sudah ada.
- `app/src/main/java/com/appcash/ui/screens/ExpensesScreen.kt:41` — Daftar pengeluaran urut terbaru + total di atas; tombol tambah hanya untuk admin.
- `app/src/main/java/com/appcash/ui/screens/ExpensesScreen.kt:253` — Form tambah: tanggal otomatis hari ini, tombol aktif hanya bila isian valid.
- `app/src/main/java/com/appcash/ui/screens/ExpensesScreen.kt:319` — Form edit pengeluaran.

### `CARI-ANGGOTA`

- `app/src/main/java/com/appcash/data/model/Models.kt:48` — Data satu siswa.
- `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:100` — Daftar 33 siswa XII PPLG: id, nama, NIS, jabatan, bio, umur, no. WA.
- `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:555` — Edit data siswa (khusus admin).
- `app/src/main/java/com/appcash/ui/screens/MembersScreen.kt:42` — Daftar siswa + kolom cari nama/NIS/jabatan.
- `app/src/main/java/com/appcash/ui/screens/MembersScreen.kt:230` — Detail siswa; tombol Hubungi WA mengubah nomor 08xx menjadi 628xx.
- `app/src/main/java/com/appcash/ui/screens/MembersScreen.kt:316` — Form edit siswa (khusus admin).
- `app/src/main/java/com/appcash/ui/screens/PaymentsScreen.kt:1158` — Foto siswa dari res/drawable/student_<id>; bila tidak ada, tampil nomor id.

### `CARI-GRAFIK`

- `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:442` — Saldo = saldo awal + (jumlah centang lunas x kas) + donasi - total pengeluaran.
- `app/src/main/java/com/appcash/ui/screens/DashboardScreen.kt:569` — Grafik garis pemasukan (hijau) vs pengeluaran (merah) dengan label bulan di bawah.

### `CARI-PROFIL`

- `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:352` — Menyimpan perubahan nama, no. HP, dan bio siswa yang sedang login.
- `app/src/main/java/com/appcash/ui/screens/ProfileScreen.kt:36` — Menampilkan profil, kartu denda (siswa), tombol Edit Profil dan Logout.

### `CARI-QRIS`

- `app/src/main/java/com/appcash/ui/screens/PaymentsScreen.kt:831` — Siswa membayar via BNI/QRIS, kirim bukti ke WA admin, lalu konfirmasi.

### `CARI-WHATSAPP`

- `app/src/main/java/com/appcash/ui/screens/MembersScreen.kt:230` — Detail siswa; tombol Hubungi WA mengubah nomor 08xx menjadi 628xx.
- `app/src/main/java/com/appcash/ui/screens/PaymentsScreen.kt:831` — Siswa membayar via BNI/QRIS, kirim bukti ke WA admin, lalu konfirmasi.

### `CARI-VALIDASI`

- `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:523` — Tambah pengeluaran; keterangan wajib diisi dan nominal > 0.
- `app/src/main/java/com/appcash/ui/screens/DashboardScreen.kt:700` — Tampilan error + tombol Coba lagi yang dipakai beberapa layar.
- `app/src/main/java/com/appcash/ui/screens/ExpensesScreen.kt:253` — Form tambah: tanggal otomatis hari ini, tombol aktif hanya bila isian valid.
- `app/src/main/java/com/appcash/ui/screens/ExpensesScreen.kt:385` — Menolak tanggal yang tidak ada di kalender, mis. 2026-02-30.

### `CARI-JADWAL`

- `app/src/main/java/com/appcash/ui/screens/ScheduleScreen.kt:46` — Belum dipasang di bottom bar; tambahkan di MainContent bila ingin ditampilkan.

### `CARI-FORMAT-RUPIAH`

- `app/src/main/java/com/appcash/ui/screens/DashboardScreen.kt:716` — Mengubah 1389000 menjadi "1.389.000" (tanpa "Rp").

### `CARI-TEMA`

- `app/src/main/java/com/appcash/ui/theme/Color.kt:13` — Warna utama aplikasi; ganti nilai hex di sini untuk mengubah tema.

### `CARI-TIDAK-DIPAKAI`

- `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:584` — Untuk PaymentInputScreen yang belum dipasang di navigasi; tidak ikut dihitung di saldo.
- `app/src/main/java/com/appcash/network/ApiService.kt:18` — Aplikasi saat ini offline; semua data berasal dari AppCashRepository.
- `app/src/main/java/com/appcash/ui/screens/PaymentInputScreen.kt:74` — Tidak dipanggil dari navigasi; pembayaran resmi dicatat lewat PaymentsScreen.
- `app/src/main/java/com/appcash/ui/screens/ScheduleScreen.kt:46` — Belum dipasang di bottom bar; tambahkan di MainContent bila ingin ditampilkan.
