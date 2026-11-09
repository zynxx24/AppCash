package com.appcash

import android.app.Application
import com.appcash.data.repository.AppCashRepository

class AppCashApplication : Application() {
    // Satu repository untuk seluruh aplikasi: sesi login & data tidak hilang saat layar diputar
    val repository: AppCashRepository by lazy { AppCashRepository(this) }
}
