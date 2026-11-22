// » Alamat folder file ini. File ini membuat TEMA: aturan warna & font yang membungkus seluruh layar
// » AppCash.
package com.appcash.ui.theme

// » Activity = satu jendela layar Android. Dibutuhkan untuk mengatur warna status bar.
import android.app.Activity
// » Context = "pegangan" ke lingkungan aplikasi. Dari sini kita mencari Activity.
import android.content.Context
// » ContextWrapper = Context yang membungkus Context lain; kita membuka bungkusnya satu per satu di fungsi
// » findActivity.
import android.content.ContextWrapper
// » Build = info versi Android di HP (tidak dipakai langsung di sini).
import android.os.Build
// » MaterialTheme = mesin tema Material 3 (gaya resmi tampilan Android modern).
import androidx.compose.material3.MaterialTheme
// » lightColorScheme = pembuat daftar warna tema terang.
import androidx.compose.material3.lightColorScheme
// » @Composable = penanda bahwa fungsi di bawahnya adalah bagian tampilan Compose.
import androidx.compose.runtime.Composable
// » SideEffect = cara menjalankan kode "di luar tampilan" (mengubah warna status bar) setiap tampilan
// » digambar.
import androidx.compose.runtime.SideEffect
// » toArgb = mengubah Color Compose menjadi angka warna yang dimengerti Android lama.
import androidx.compose.ui.graphics.toArgb
// » LocalView = mengambil View (kanvas) tempat tampilan sedang digambar.
import androidx.compose.ui.platform.LocalView
// » WindowCompat = alat untuk mengatur status bar dan navigation bar di berbagai versi Android.
import androidx.core.view.WindowCompat

// » Membuat daftar warna tema (private = hanya dipakai di file ini). Setiap nama di kiri adalah "peran"
// » warna di Material 3, nilai di kanan adalah warna dari Color.kt.
private val AppCashColorScheme = lightColorScheme(
    // » primary = warna utama (tombol, tab aktif): oranye.
    primary            = OrangePrimary,
    // » onPrimary = warna teks/ikon di ATAS warna primary: putih.
    onPrimary          = OrangeOnPrimary,
    // » primaryContainer = latar versi lembut dari primary: oranye muda (krem).
    primaryContainer   = OrangeContainer,
    // » onPrimaryContainer = warna teks di atas primaryContainer: oranye gelap.
    onPrimaryContainer = OrangeDark,
    // » secondary = warna kedua: oranye terang.
    secondary          = OrangeLight,
    // » onSecondary = teks di atas secondary: putih.
    onSecondary        = OrangeOnPrimary,
    // » secondaryContainer = latar lembut warna kedua: krem.
    secondaryContainer = OrangeContainer,
    // » onSecondaryContainer = teks di atas secondaryContainer: oranye gelap.
    onSecondaryContainer = OrangeDark,
    // » tertiary = warna ketiga/aksen: biru.
    tertiary           = BlueAccent,
    // » onTertiary = teks di atas tertiary: putih.
    onTertiary         = WhitePure,
    // » tertiaryContainer = latar lembut warna ketiga: krem.
    tertiaryContainer  = OrangeContainer,
    // » onTertiaryContainer = teks di atas tertiaryContainer: oranye gelap.
    onTertiaryContainer = OrangeDark,
    // » background = warna latar halaman: abu-abu sangat muda.
    background         = GreyBg,
    // » onBackground = teks di atas latar halaman: hampir hitam.
    onBackground       = TextDark,
    // » surface = warna permukaan kartu & dialog: putih.
    surface            = GreySurface,
    // » onSurface = teks di atas surface: hampir hitam.
    onSurface          = TextDark,
    // » surfaceVariant = warna permukaan alternatif (kotak dalam kartu): abu hampir putih.
    surfaceVariant     = GreyCard,
    // » onSurfaceVariant = teks keterangan di atas surfaceVariant: abu tua.
    onSurfaceVariant   = TextMedium,
    // » error = warna untuk keadaan error: merah tua.
    error              = ErrorRed,
    // » onError = teks di atas warna error: putih.
    onError            = WhitePure,
    // » outline = warna garis tepi: abu-abu.
    outline            = GreyBorder
)

// » Fungsi bantu: mencari Activity (jendela layar) dari sebuah Context. Dibutuhkan karena hanya Activity
// » yang bisa mengubah warna status bar.
// Mencari Activity dari Context untuk mengatur warna status bar.
// » "Context.findActivity" = fungsi tambahan yang bisa dipanggil pada semua Context. Hasilnya Activity atau
// » null (tidak ketemu).
private fun Context.findActivity(): Activity? {
    // » ctx = Context yang sedang diperiksa; mulai dari Context ini.
    var ctx = this
    // » Selama ctx masih "pembungkus" Context lain, terus buka bungkusnya.
    while (ctx is ContextWrapper) {
        // » Kalau ctx ternyata Activity, itu yang dicari, langsung kembalikan.
        if (ctx is Activity) return ctx
        // » Kalau bukan, buka satu lapis bungkus (baseContext) lalu ulangi.
        ctx = ctx.baseContext
    }
    // » Sampai habis tidak ketemu: kembalikan null (kosong).
    return null
}

// » Tema utama AppCash. Dipanggil sekali di MainActivity: semua layar ditaruh di dalam AppCashTheme { ... }
// » sehingga otomatis memakai warna dan font aplikasi.
// Tema warna oranye AppCash yang membungkus seluruh layar.
@Composable
// » content = isi tampilan (semua layar) yang akan dibungkus tema ini.
fun AppCashTheme(content: @Composable () -> Unit) {
    // » Mengambil View tempat layar digambar; dibutuhkan untuk mengubah status bar.
    val view = LocalView.current
    // » Kalau sedang tampil di pratinjau Android Studio (isInEditMode), lewati pengaturan status bar karena
    // » tidak ada jendela asli.
    if (!view.isInEditMode) {
        // » SideEffect: blok ini dijalankan setiap kali tampilan selesai digambar, tempat aman mengubah jendela.
        SideEffect {
            // » try = coba jalankan; kalau error, loncat ke catch di bawah agar aplikasi TIDAK crash.
            try {
                // » Cari Activity dari view. Kalau tidak ketemu (null), keluar dari SideEffect dengan "return@SideEffect".
                val activity = view.context.findActivity() ?: return@SideEffect
                // » window = jendela layar yang warnanya akan diubah.
                val window = activity.window
                // » @Suppress = menyembunyikan peringatan "fitur lama" dari Android Studio (kodenya tetap jalan).
                @Suppress("DEPRECATION")
                // » Mengubah warna status bar (bar tipis di atas layar tempat jam & baterai) menjadi putih.
                window.statusBarColor = WhitePure.toArgb()
                @Suppress("DEPRECATION")
                // » Mengubah warna bar navigasi (bar di bawah layar tempat tombol Home) menjadi putih.
                window.navigationBarColor = WhitePure.toArgb()
                // » Membuat ikon status bar (jam, baterai) berwarna GELAP, karena latarnya putih.
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
                // » Sama, tetapi untuk ikon di bar navigasi bawah.
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = true
            // » Kalau ada error apa pun (mis. di HP tertentu), diabaikan saja; "_" artinya error-nya tidak perlu
            // » dipakai.
            } catch (_: Exception) {
                // Silently ignore on devices where this fails
            }
        }
    }
    // » Memasang tema Material 3 ke seluruh isi.
    MaterialTheme(
        // » Memakai daftar warna AppCashColorScheme yang dibuat di baris 15.
        colorScheme = AppCashColorScheme,
        // » Memakai gaya huruf Typography dari Type.kt.
        typography  = Typography,
        // » Isi tampilan (semua layar aplikasi) ditaruh di dalam tema ini.
        content     = content
    )
}
