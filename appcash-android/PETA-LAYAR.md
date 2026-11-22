# Peta Layar AppCash (Kelompok 6)

Dipakai kalau guru bertanya **"ini yang di layar kodenya di mana?"**. Cari baris di tabel, lalu buka file dan nomor barisnya.
Di Android Studio: **Ctrl+Shift+N** (ketik nama file) lalu **Ctrl+G** (ketik nomor baris).

Alur singkat aplikasi: `AppCashApplication` membuat `AppCashRepository` (data), `MainActivity` memanggil `AppRoot`, lalu Login atau 5 tab (`MainContent`).
Grafik: **hanya satu yang tampil**, yaitu grafik garis dua warna di Dashboard (`WeeklyKasLineChart`). `MiniBarChart` ada tetapi tidak dipanggil.

## DASHBOARD (tab tengah, halaman awal)

| Yang terlihat atau ditanyakan | Penjelasan | Letak kode |
|---|---|---|
| Sapaan dan nama (foto untuk siswa) | Bagian Top Header | `app/src/main/java/com/appcash/ui/screens/DashboardScreen.kt:243` |
| Kartu oranye SALDO besar | Hero Balance Card, angka dari getDashboard | `app/src/main/java/com/appcash/ui/screens/DashboardScreen.kt:294` |
| Pencocokan saldo fisik di peti kas | Variabel selisih | `app/src/main/java/com/appcash/ui/screens/DashboardScreen.kt:338` |
| Kotak Anggota (33 Siswa) dan Periode (37 Minggu) | Stat Cards Row | `app/src/main/java/com/appcash/ui/screens/DashboardScreen.kt:455` |
| Kartu Status Kas Anda (siswa) | USER Status Kas Anda | `app/src/main/java/com/appcash/ui/screens/DashboardScreen.kt:556` |
| GRAFIK Kas per Bulan (kartu) | Chart Card | `app/src/main/java/com/appcash/ui/screens/DashboardScreen.kt:673` |
| GRAFIK garis dua warna (gambarnya) | WeeklyKasLineChart, Canvas manual | `app/src/main/java/com/appcash/ui/screens/DashboardScreen.kt:1024` |
| Rekap Denda Kas Tertunggak (admin) | Denda Summary Card | `app/src/main/java/com/appcash/ui/screens/DashboardScreen.kt:733` |

## KAS (tab kiri)

| Yang terlihat atau ditanyakan | Penjelasan | Letak kode |
|---|---|---|
| Pintu masuk, admin atau siswa | PaymentsScreen memilih tampilan | `app/src/main/java/com/appcash/ui/screens/PaymentsScreen.kt:136` |
| Admin: kartu saldo dan daftar 37 minggu | AdminPaymentsScreen | `app/src/main/java/com/appcash/ui/screens/PaymentsScreen.kt:157` |
| Admin: dialog Tambah Pembayaran | AdminPaymentDialog | `app/src/main/java/com/appcash/ui/screens/PaymentsScreen.kt:535` |
| Admin: dialog centang satu minggu | AdminWeekDetailDialog | `app/src/main/java/com/appcash/ui/screens/PaymentsScreen.kt:873` |
| Siswa: status dan riwayat per minggu | UserPaymentsScreen | `app/src/main/java/com/appcash/ui/screens/PaymentsScreen.kt:1095` |
| Siswa: dialog bayar BNI atau QRIS | UserBniPaymentDialog | `app/src/main/java/com/appcash/ui/screens/PaymentsScreen.kt:1448` |
| Nomor rekening BNI | Teks rekening dan tombol salin | `app/src/main/java/com/appcash/ui/screens/PaymentsScreen.kt:1645` |
| Tombol Kirim Bukti ke WA Admin | Intent api.whatsapp.com | `app/src/main/java/com/appcash/ui/screens/PaymentsScreen.kt:1775` |
| Tombol Konfirmasi (terkunci sebelum WA) | isWaConfirmed | `app/src/main/java/com/appcash/ui/screens/PaymentsScreen.kt:1828` |
| Kartu satu minggu (M01..M37) | PaymentDateCard | `app/src/main/java/com/appcash/ui/screens/PaymentsScreen.kt:1860` |
| Foto bulat siswa | StudentAvatar | `app/src/main/java/com/appcash/ui/screens/PaymentsScreen.kt:2022` |

## PENGELUARAN, ANGGOTA, PROFIL, LOGIN

| Yang terlihat atau ditanyakan | Penjelasan | Letak kode |
|---|---|---|
| Daftar pengeluaran dan total | ExpensesScreen | `app/src/main/java/com/appcash/ui/screens/ExpensesScreen.kt:85` |
| Form tambah pengeluaran | AddExpenseDialog | `app/src/main/java/com/appcash/ui/screens/ExpensesScreen.kt:440` |
| Daftar 33 siswa dan kolom cari | MembersScreen | `app/src/main/java/com/appcash/ui/screens/MembersScreen.kt:86` |
| Dialog bio dan tombol Hubungi WA | MemberBioDialog | `app/src/main/java/com/appcash/ui/screens/MembersScreen.kt:404` |
| Profil, kartu denda, Keluar | ProfileScreen | `app/src/main/java/com/appcash/ui/screens/ProfileScreen.kt:72` |
| Login siswa (pilih nama) atau admin (email) | LoginScreen | `app/src/main/java/com/appcash/ui/screens/LoginScreen.kt:73` |
| Pindah antar tab bawah | MainContent, HorizontalPager | `app/src/main/java/com/appcash/ui/MainActivity.kt:210` |

## OTAK DATA (tidak terlihat di layar)

| Yang terlihat atau ditanyakan | Penjelasan | Letak kode |
|---|---|---|
| Rumus SALDO | getDashboard (netBalance) | `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:804` |
| Rumus DENDA | calculateDenda | `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:704` |
| Denda minggu yang dicentang | dendaForWeeks | `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:751` |
| Data untuk GRAFIK per bulan | getDashboard bagian bucket | `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:814` |
| Login admin (hash SHA-256) | login dan ADMIN_PASSWORD_HASH | `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:507` |
| Tabel centang bayar | paymentRecords | `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:405` |
| Data awal 33 siswa | membersList | `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:205` |
| Dispensasi BI/PKL | exemptions dan isExempt | `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:420` |
| Simpan ke HP | persist | `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:154` |
| Baca dari HP | restore | `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:169` |
| Bentuk data | Models.kt | `app/src/main/java/com/appcash/data/model/Models.kt:96` |
