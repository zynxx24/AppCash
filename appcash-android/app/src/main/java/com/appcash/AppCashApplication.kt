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
package com.appcash

import android.app.Application
import com.appcash.data.repository.AppCashRepository

// CARI-MULAI | Repository dibuat sekali di sini supaya data & sesi tidak hilang saat layar diputar.
class AppCashApplication : Application() {
    // Satu repository untuk seluruh aplikasi: sesi login & data tidak hilang saat layar diputar
    val repository: AppCashRepository by lazy { AppCashRepository(this) }
}
