// » Alamat folder file ini. File ini mengatur GAYA HURUF (ukuran, ketebalan, jarak) untuk seluruh teks
// » aplikasi.
package com.appcash.ui.theme

// » Typography = daftar gaya huruf Material 3.
import androidx.compose.material3.Typography
// » TextStyle = satu gaya huruf (font, tebal, ukuran, jarak baris).
import androidx.compose.ui.text.TextStyle
// » FontFamily = jenis huruf (font).
import androidx.compose.ui.text.font.FontFamily
// » FontWeight = ketebalan huruf (Normal, Bold, dst).
import androidx.compose.ui.text.font.FontWeight
// » sp = satuan ukuran huruf Android; mengikuti ukuran font yang diatur pengguna di HP.
import androidx.compose.ui.unit.sp

// » Membuat daftar gaya huruf. Saat layar menulis style = MaterialTheme.typography.titleLarge, gaya yang
// » dipakai adalah yang didefinisikan di bawah ini.
val Typography = Typography(
    // » displayLarge = gaya paling besar (angka saldo besar, judul sambutan).
    displayLarge = TextStyle(
        // » Memakai font bawaan HP.
        fontFamily = FontFamily.Default,
        // » ExtraBold = sangat tebal.
        fontWeight = FontWeight.ExtraBold,
        // » Ukuran huruf 32sp.
        fontSize = 32.sp,
        // » Jarak antar baris 40sp (supaya baris tidak menempel).
        lineHeight = 40.sp,
        // » Jarak antar huruf dirapatkan sedikit (-0.5sp) agar judul besar terlihat rapi.
        letterSpacing = (-0.5).sp
    ),
    // » headlineLarge = judul besar halaman, tebal, 28sp.
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    // » headlineMedium = judul sedang, tebal, 22sp.
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    // » titleLarge = judul bagian/kartu, tebal, 20sp. Dipakai di judul layar seperti "Mata Pelajaran XII PPLG".
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    // » titleMedium = sub-judul, semi tebal, 16sp.
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    // » titleSmall = sub-judul kecil, semi tebal, 14sp.
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    // » bodyLarge = teks isi besar, normal, 16sp.
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    // » bodyMedium = teks isi standar, normal, 14sp. Gaya yang paling sering dipakai untuk paragraf dan baris
    // » daftar.
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    // » bodySmall = teks keterangan kecil, normal, 12sp.
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    // » labelLarge = teks tombol dan label, semi tebal, 14sp.
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    // » labelSmall = label sangat kecil (mis. keterangan di atas angka), 11sp.
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)
