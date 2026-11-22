// ==========================================================================
// PETA FILE · AppCash (Kelompok 6) · AppCashApplication.kt
// Isi file : Objek Application; membuat satu repository untuk seluruh aplikasi.
// Penanda  : CARI-MULAI
// Cara cari: VS Code Ctrl+Shift+F (Android Studio Ctrl+Shift+F) lalu ketik
//            "CARI-" untuk semua penanda, atau salah satu nama di baris Penanda.
// ==========================================================================
//  BACA INI DULU
//  Application = objek yang hidup selama aplikasi berjalan. Di sini AppCashRepository dibuat SEKALI (lazy),
//  lalu dipakai MainActivity dan semua layar. Inilah sebabnya data dan sesi login tidak hilang saat layar diputar.
// » PACKAGE = alamat/folder file ini di dalam proyek. Semua file AppCash berawalan com.appcash.
package com.appcash

// » IMPORT = "memanggil" alat dari tempat lain supaya bisa dipakai di file ini. Application adalah kelas
// » bawaan Android untuk "induk" seluruh aplikasi.
import android.app.Application
// » Memanggil AppCashRepository, yaitu kelas tempat SEMUA data kas disimpan dan dihitung (file
// » data/repository/AppCashRepository.kt).
import com.appcash.data.repository.AppCashRepository

// CARI-MULAI | Repository dibuat sekali di sini supaya data & sesi tidak hilang saat layar diputar.
// » class = cetakan/objek. AppCashApplication adalah objek induk yang dibuat Android SEKALI saat aplikasi
// » dibuka (nama ini didaftarkan di AndroidManifest.xml). ": Application()" artinya mewarisi sifat
// » Application bawaan Android.
class AppCashApplication : Application() {
    // Satu repository untuk seluruh aplikasi: sesi login & data tidak hilang saat layar diputar
    // » val = nilai tetap. "by lazy" = baru dibuat ketika pertama kali dibutuhkan, lalu dipakai terus (tidak
    // » dibuat ulang). Hasilnya: hanya ada SATU repository, jadi data kas dan status login tidak hilang saat
    // » layar diputar. "this" = Context aplikasi yang dibutuhkan repository untuk membuka penyimpanan HP.
    val repository: AppCashRepository by lazy { AppCashRepository(this) }
}
