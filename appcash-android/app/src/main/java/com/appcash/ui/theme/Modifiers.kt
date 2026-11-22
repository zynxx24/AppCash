// » Alamat folder file ini. Isinya satu "resep" tampilan: kartu oranye yang dipakai di Dashboard.
package com.appcash.ui.theme

// » Import = memanggil alat Compose: background (warna latar), border (garis tepi), RoundedCornerShape
// » (sudut membulat).
import androidx.compose.foundation.background
// » border = garis tepi sebuah tampilan.
import androidx.compose.foundation.border
// » RoundedCornerShape = bentuk persegi dengan sudut membulat.
import androidx.compose.foundation.shape.RoundedCornerShape
// » Modifier = daftar "dekorasi" yang ditempel pada sebuah tampilan (ukuran, warna, bentuk, bayangan).
import androidx.compose.ui.Modifier
// » clip = memotong tampilan mengikuti bentuk tertentu (di sini sudut bulat).
import androidx.compose.ui.draw.clip
// » shadow = membuat bayangan di belakang tampilan.
import androidx.compose.ui.draw.shadow
// » Brush = cat warna. Dipakai untuk gradien (warna yang berubah halus dari satu ke warna lain).
import androidx.compose.ui.graphics.Brush
// » Color = tipe warna Compose.
import androidx.compose.ui.graphics.Color
// » Dp = satuan ukuran Android yang tidak tergantung ukuran layar (density-independent pixel).
import androidx.compose.ui.unit.Dp
// » Fungsi bantu agar bisa menulis angka seperti 16.dp.
import androidx.compose.ui.unit.dp

/** Gradasi kartu oranye sesuai desain: terang di atas, gelap di bawah. */
// » Membuat cat gradien VERTIKAL (atas ke bawah): atas oranye terang 0xFFFFB347, bawah OrangeDark. Disimpan
// » di satu nama supaya semua kartu oranye warnanya sama.
val OrangeCardBrush = Brush.verticalGradient(listOf(Color(0xFFFFB347), OrangeDark))

// Pembuat kartu oranye bergradien (dipakai kartu saldo dan dua kartu kecil di Dashboard): .orangeCard(24.dp) = sudut bulat 24dp.
/**
 * Kartu oranye standar AppCash: bayangan lembut + border tipis + sudut bulat.
 * Urutan modifier penting: shadow -> border -> clip -> background,
 * supaya bayangan tidak terpotong dan border mengikuti sudut kartu.
 */
// » Membuat "modifier baru" bernama orangeCard. Kalau ditulis .orangeCard(24.dp) di sebuah Box, kotak itu
// » otomatis jadi kartu oranye bersudut 24dp. Parameter radius = seberapa bulat sudutnya, default 16dp.
fun Modifier.orangeCard(radius: Dp = 16.dp): Modifier {
    // » Membuat bentuk persegi dengan sudut bulat sebesar radius. Bentuk ini dipakai berulang di bawah supaya
    // » bayangan, garis tepi, dan isi mengikuti bentuk yang sama.
    val shape = RoundedCornerShape(radius)
    // » Mulai dari modifier yang sudah ada (this), lalu tempel dekorasi satu per satu dari atas ke bawah. URUTAN
    // » PENTING.
    return this
        // » 1) Bayangan setinggi 6dp berwarna oranye gelap, mengikuti bentuk kartu. Dibuat PERTAMA supaya tidak
        // » terpotong oleh clip.
        .shadow(elevation = 6.dp, shape = shape, ambientColor = OrangeDark, spotColor = OrangeDark)
        // » 2) Garis tepi tebal 1dp berwarna putih transparan 35% (alpha 0.35), memberi kesan kilap di pinggir
        // » kartu.
        .border(1.dp, Color.White.copy(alpha = 0.35f), shape)
        // » 3) Memotong isi kartu mengikuti sudut bulat supaya isi tidak keluar dari sudut.
        .clip(shape)
        // » 4) Mengisi latar kartu dengan gradien oranye yang dibuat di baris 15.
        .background(OrangeCardBrush)
}
