// ==========================================================================
// PETA FILE · AppCash (Kelompok 6) · Color.kt
// Isi file : Palet warna tema Warm Sunset Orange.
// Penanda  : CARI-TEMA
// Cara cari: VS Code Ctrl+Shift+F (Android Studio Ctrl+Shift+F) lalu ketik
//            "CARI-" untuk semua penanda, atau salah satu nama di baris Penanda.
// ==========================================================================
// » Alamat folder file ini. Ini file tempat semua warna aplikasi didaftar.
package com.appcash.ui.theme

// » Memanggil tipe "Color" milik Compose, supaya bisa menulis Color(0xFF...) di bawah.
import androidx.compose.ui.graphics.Color

// AppCash Orange Palette – v3.0
// CARI-TEMA | Warna utama aplikasi; ganti nilai hex di sini untuk mengubah tema.
// » val = nama warna yang bisa dipakai di mana saja. Color(0xFFFF6B00) = kode warna HEX: "FF" artinya tidak
// » transparan, lalu "FF6B00" = warna oranye. Ini oranye UTAMA aplikasi (tombol, tab aktif, judul).
val OrangePrimary   = Color(0xFFFF6B00)
// » Oranye lebih terang. Dipakai untuk bagian gradien/aksen supaya tampilan tidak datar.
val OrangeLight     = Color(0xFFFF8C42)
// » Oranye lebih gelap. Dipakai untuk bayangan, teks di atas latar oranye muda, dan ujung bawah gradien
// » kartu.
val OrangeDark      = Color(0xFFE55A00)
// Warna AppCash: OrangePrimary/OrangeContainer (oranye utama, kartu, tab aktif), GreenPositive (lunas, pemasukan), RedNegative (denda, pengeluaran), TextMedium (teks keterangan).
// » Oranye sangat muda (krem). Dipakai sebagai latar chip/kotak kecil yang bernuansa oranye.
val OrangeContainer = Color(0xFFFFF3E0)
// » Putih. Dipakai untuk teks atau ikon yang berada di atas latar oranye (supaya terbaca).
val OrangeOnPrimary = Color(0xFFFFFFFF)

// » Putih murni. Dipakai untuk status bar, bar navigasi, dan kartu putih.
val WhitePure   = Color(0xFFFFFFFF)
// » Abu-abu sangat muda untuk latar belakang halaman.
val GreyBg      = Color(0xFFF5F5F5)
// » Warna permukaan kartu/dialog (putih).
val GreySurface = Color(0xFFFFFFFF)
// » Abu-abu hampir putih untuk kotak/kartu di dalam halaman.
val GreyCard    = Color(0xFFFAFAFA)
// » Abu-abu untuk garis tepi (border) dan garis pemisah.
val GreyBorder  = Color(0xFFE0E0E0)

// » Hampir hitam. Warna teks utama (judul, angka penting).
val TextDark    = Color(0xFF1A1A1A)
// » Abu-abu tua. Warna teks keterangan/penjelasan kecil.
val TextMedium  = Color(0xFF555555)
// » Abu-abu muda. Warna teks yang kurang penting (hint, placeholder).
val TextMuted   = Color(0xFF9E9E9E)
// » Putih untuk teks di atas kartu berwarna.
val TextOnCard  = Color(0xFFFFFFFF)

// » Hijau. Dipakai untuk hal positif: status LUNAS, uang masuk, angka plus.
val GreenPositive = Color(0xFF22C55E)
// » Merah. Dipakai untuk hal negatif: denda, belum bayar, pengeluaran.
val RedNegative   = Color(0xFFEF4444)
// » Biru. Warna aksen tambahan (mis. ikon informasi atau garis grafik kedua).
val BlueAccent    = Color(0xFF3B82F6)

// » Merah tua khusus untuk pesan error (mis. "Password salah").
val ErrorRed    = Color(0xFFDC2626)
