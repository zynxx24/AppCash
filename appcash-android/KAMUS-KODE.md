# Kamus Istilah Kode AppCash

Untuk yang belum terbiasa membaca Kotlin dan Jetpack Compose. Semua istilah di bawah muncul di kode AppCash.

## Cara berpikir singkat
Layar di Compose adalah **fungsi** yang menggambar tampilan. Layar tidak menyimpan data sendiri.
Data hidup di repository, lalu layar menampilkannya. Bila data berubah, layar digambar ulang otomatis.

## Istilah tata letak (apa yang tampil)
| Istilah | Artinya |
|---|---|
| `@Composable` | Penanda bahwa fungsi ini menggambar sebagian layar |
| `Column` | Susun isi dari atas ke bawah |
| `Row` | Susun isi dari kiri ke kanan |
| `Box` | Tumpuk isi satu di atas yang lain, atau satu kotak |
| `Card` | Kartu berlatar dengan sudut membulat |
| `Text` | Tulisan |
| `Icon` | Gambar ikon kecil |
| `Spacer` | Ruang kosong pemisah |
| `LazyColumn` | Daftar vertikal yang bisa digulir, hanya menggambar yang terlihat |
| `Modifier` | Daftar "dandanan" untuk elemen: ukuran, jarak (`padding`), latar (`background`), bisa ditekan (`clickable`), sudut (`clip`) |
| `weight(1f)` | Bagi lebar sama rata dengan elemen lain di baris yang sama |
| `dp` dan `sp` | Satuan ukuran. `dp` untuk jarak dan bentuk, `sp` untuk huruf |
| `ModalBottomSheet` | Lembar yang muncul dari bawah layar |
| `AlertDialog` | Kotak pesan di tengah layar |
| `Canvas` | Kanvas kosong untuk menggambar manual (garis, lingkaran). Dipakai grafik |
| `Scaffold` | Kerangka halaman (isi dan bar bawah) |
| `NavigationBar` | Bar 5 tab di bawah layar |

## Istilah data dan logika (apa yang terjadi)
| Istilah | Artinya |
|---|---|
| `remember { mutableStateOf(...) }` | Variabel yang diingat layar. Bila nilainya berubah, layar digambar ulang |
| `StateFlow` | Nilai yang bisa "dipantau". Perubahannya otomatis sampai ke layar |
| `ViewModel` | Perantara antara layar dan data (AppCash memakai repository langsung dari layar) |
| `Repository` | Tempat data dan rumus (`AppCashRepository`) |
| `data class` | Cetakan data, contoh `Member`, `Expense` |
| `enum class` | Daftar pilihan tetap |
| `SharedPreferences` | Laci penyimpanan kecil di HP. AppCash menyimpan data di sini |
| `JSON` | Format teks untuk menyimpan data terstruktur |
| `Intent` | Perintah ke Android untuk membuka aplikasi lain (WhatsApp, menu Bagikan) |
| `combine` | Gabungkan dua sumber data lalu hitung ulang tiap kali salah satu berubah |
| `map`, `filter`, `sumOf` | Mengubah, menyaring, dan menjumlahkan isi daftar |
| `?.` dan `?:` | Aman bila nilainya kosong (null), dan nilai cadangan bila kosong |
| `remember(x) { ... }` | Hitung sekali, hitung ulang hanya bila `x` berubah |

## Istilah AppCash
| Istilah | Artinya |
|---|---|
| Admin | Akun bendahara (login email). Boleh mencentang pembayaran, mengelola pengeluaran, dan melihat rekap denda |
| Siswa (user) | Akun yang login hanya dengan memilih nama. Melihat datanya sendiri |
| `isAdmin` | Nilai benar bila akun yang login adalah admin |
| Minggu M01 sampai M37 | Satu minggu kas (tiap Senin). Contoh `M05 (02 Feb)` |
| Centang bayar | `paymentRecords[idSiswa][minggu] = true` artinya lunas |
| Dispensasi | Minggu bebas kas karena BI atau PKL, tidak didenda |
| Denda | Rp5.000 x 5 persen x bulan penuh telat, per minggu belum lunas |
| Saldo fisik | Uang nyata di peti kas (`realBalance`), dicocokkan dengan saldo buku |
| `persist()` | Menyimpan seluruh data ke HP dan menaikkan nomor versi supaya layar memuat ulang |
| SHA-256 | Pengacak password satu arah. Password admin tidak disimpan asli |
