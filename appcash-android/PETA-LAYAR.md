# Peta Layar AppCash (Kelompok 6)

Dipakai kalau guru bertanya **"ini yang di layar kodenya di mana?"**. Cari baris di tabel, lalu buka file dan nomor barisnya.
Di Android Studio: **Ctrl+Shift+N** (ketik nama file) lalu **Ctrl+G** (ketik nomor baris).

Alur singkat aplikasi: `AppCashApplication` membuat `AppCashRepository` (data), `MainActivity` memanggil `AppRoot`, lalu Login atau 5 tab (`MainContent`).
Grafik: **hanya satu yang tampil**, yaitu grafik garis dua warna di Dashboard (`WeeklyKasLineChart`). `MiniBarChart` ada tetapi tidak dipanggil.

## DASHBOARD (tab tengah, halaman awal)

| Yang terlihat atau ditanyakan | Penjelasan | Letak kode |
|---|---|---|
| Sapaan dan nama (foto untuk siswa) | Bagian Top Header | `app/src/main/java/com/appcash/ui/screens/DashboardScreen.kt:142` |
| Kartu oranye SALDO besar | Hero Balance Card, angka dari getDashboard | `app/src/main/java/com/appcash/ui/screens/DashboardScreen.kt:174` |
| Pencocokan saldo fisik di peti kas | Variabel selisih | `app/src/main/java/com/appcash/ui/screens/DashboardScreen.kt:198` |
| Kotak Anggota (33 Siswa) dan Periode (37 Minggu) | Stat Cards Row | `app/src/main/java/com/appcash/ui/screens/DashboardScreen.kt:269` |
| Kartu Status Kas Anda (siswa) | USER Status Kas Anda | `app/src/main/java/com/appcash/ui/screens/DashboardScreen.kt:330` |
| GRAFIK Kas per Bulan (kartu) | Chart Card | `app/src/main/java/com/appcash/ui/screens/DashboardScreen.kt:399` |
| GRAFIK garis dua warna (gambarnya) | WeeklyKasLineChart, Canvas manual | `app/src/main/java/com/appcash/ui/screens/DashboardScreen.kt:614` |
| Rekap Denda Kas Tertunggak (admin) | Denda Summary Card | `app/src/main/java/com/appcash/ui/screens/DashboardScreen.kt:435` |

## KAS (tab kiri)

| Yang terlihat atau ditanyakan | Penjelasan | Letak kode |
|---|---|---|
| Pintu masuk, admin atau siswa | PaymentsScreen memilih tampilan | `app/src/main/java/com/appcash/ui/screens/PaymentsScreen.kt:79` |
| Admin: kartu saldo dan daftar 37 minggu | AdminPaymentsScreen | `app/src/main/java/com/appcash/ui/screens/PaymentsScreen.kt:95` |
| Admin: dialog Tambah Pembayaran | AdminPaymentDialog | `app/src/main/java/com/appcash/ui/screens/PaymentsScreen.kt:325` |
| Admin: dialog centang satu minggu | AdminWeekDetailDialog | `app/src/main/java/com/appcash/ui/screens/PaymentsScreen.kt:529` |
| Siswa: status dan riwayat per minggu | UserPaymentsScreen | `app/src/main/java/com/appcash/ui/screens/PaymentsScreen.kt:660` |
| Siswa: dialog bayar BNI atau QRIS | UserBniPaymentDialog | `app/src/main/java/com/appcash/ui/screens/PaymentsScreen.kt:873` |
| Nomor rekening BNI | Teks rekening dan tombol salin | `app/src/main/java/com/appcash/ui/screens/PaymentsScreen.kt:983` |
| Tombol Kirim Bukti ke WA Admin | Intent api.whatsapp.com | `app/src/main/java/com/appcash/ui/screens/PaymentsScreen.kt:1060` |
| Tombol Konfirmasi (terkunci sebelum WA) | isWaConfirmed | `app/src/main/java/com/appcash/ui/screens/PaymentsScreen.kt:1092` |
| Kartu satu minggu (M01..M37) | PaymentDateCard | `app/src/main/java/com/appcash/ui/screens/PaymentsScreen.kt:1114` |
| Foto bulat siswa | StudentAvatar | `app/src/main/java/com/appcash/ui/screens/PaymentsScreen.kt:1220` |

## PENGELUARAN, ANGGOTA, PROFIL, LOGIN

| Yang terlihat atau ditanyakan | Penjelasan | Letak kode |
|---|---|---|
| Daftar pengeluaran dan total | ExpensesScreen | `app/src/main/java/com/appcash/ui/screens/ExpensesScreen.kt:49` |
| Form tambah pengeluaran | AddExpenseDialog | `app/src/main/java/com/appcash/ui/screens/ExpensesScreen.kt:263` |
| Daftar 33 siswa dan kolom cari | MembersScreen | `app/src/main/java/com/appcash/ui/screens/MembersScreen.kt:49` |
| Dialog bio dan tombol Hubungi WA | MemberBioDialog | `app/src/main/java/com/appcash/ui/screens/MembersScreen.kt:237` |
| Profil, kartu denda, Keluar | ProfileScreen | `app/src/main/java/com/appcash/ui/screens/ProfileScreen.kt:42` |
| Login siswa (pilih nama) atau admin (email) | LoginScreen | `app/src/main/java/com/appcash/ui/screens/LoginScreen.kt:44` |
| Pindah antar tab bawah | MainContent, HorizontalPager | `app/src/main/java/com/appcash/ui/MainActivity.kt:125` |

## OTAK DATA (tidak terlihat di layar)

| Yang terlihat atau ditanyakan | Penjelasan | Letak kode |
|---|---|---|
| Rumus SALDO | getDashboard (netBalance) | `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:523` |
| Rumus DENDA | calculateDenda | `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:459` |
| Denda minggu yang dicentang | dendaForWeeks | `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:486` |
| Data untuk GRAFIK per bulan | getDashboard bagian bucket | `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:529` |
| Login admin (hash SHA-256) | login dan ADMIN_PASSWORD_HASH | `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:336` |
| Tabel centang bayar | paymentRecords | `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:267` |
| Data awal 33 siswa | membersList | `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:134` |
| Dispensasi BI/PKL | exemptions dan isExempt | `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:279` |
| Simpan ke HP | persist | `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:104` |
| Baca dari HP | restore | `app/src/main/java/com/appcash/data/repository/AppCashRepository.kt:113` |
| Bentuk data | Models.kt | `app/src/main/java/com/appcash/data/model/Models.kt:62` |
