// ==========================================================================
// PETA FILE · AppCash (Kelompok 6) · AppCashApplication.kt
// Isi file : Objek Application; membuat satu repository untuk seluruh aplikasi.
// Penanda  : CARI-MULAI
// Cara cari: VS Code Ctrl+Shift+F (Android Studio Ctrl+Shift+F) lalu ketik
//            "CARI-" untuk semua penanda, atau salah satu nama di baris Penanda.
// ==========================================================================
package com.appcash

import android.app.Application
import com.appcash.data.repository.AppCashRepository

// CARI-MULAI | Repository dibuat sekali di sini supaya data & sesi tidak hilang saat layar diputar.
class AppCashApplication : Application() {
    // Satu repository untuk seluruh aplikasi: sesi login & data tidak hilang saat layar diputar
    val repository: AppCashRepository by lazy { AppCashRepository(this) }
}
