// ==========================================================================
// PETA FILE · AppCash (Kelompok 6) · ApiService.kt
// Isi file : Definisi API server (Retrofit) untuk versi online.
// Penanda  : CARI-TIDAK-DIPAKAI
// Cara cari: VS Code Ctrl+Shift+F (Android Studio Ctrl+Shift+F) lalu ketik
//            "CARI-" untuk semua penanda, atau salah satu nama di baris Penanda.
// ==========================================================================
package com.appcash.network

import com.appcash.data.model.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

// CARI-TIDAK-DIPAKAI | Aplikasi saat ini offline; semua data berasal dari AppCashRepository.
interface ApiService {
    // BELUM DIPAKAI: rancangan API login untuk versi online. Versi sekarang tanpa server.
    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    // BELUM DIPAKAI: rancangan API logout.
    @POST("api/logout")
    suspend fun logout(): Response<Unit>

    // BELUM DIPAKAI: rancangan API ringkasan dashboard.
    @GET("api/dashboard")
    suspend fun getDashboard(): Response<Dashboard>

    // BELUM DIPAKAI: rancangan API daftar anggota.
    @GET("api/members")
    suspend fun getMembers(): Response<List<Member>>

    // BELUM DIPAKAI: rancangan API tabel pembayaran.
    @GET("api/payments")
    suspend fun getPayments(): Response<PaymentsWrapper>

    // BELUM DIPAKAI: rancangan API simpan pembayaran.
    @POST("api/payments")
    suspend fun savePayments(@Body wrapper: PaymentsWrapper): Response<Unit>

    // BELUM DIPAKAI: rancangan API daftar pengeluaran.
    @GET("api/expenses")
    suspend fun getExpenses(): Response<List<Expense>>

    // BELUM DIPAKAI: rancangan API tambah pengeluaran.
    @POST("api/expenses")
    suspend fun addExpense(@Body request: ExpenseRequest): Response<IdResponse>

    // BELUM DIPAKAI: rancangan API hapus pengeluaran.
    @HTTP(method = "DELETE", path = "api/expenses/{id}", hasBody = true)
    suspend fun deleteExpense(@Path("id") id: Int): Response<Unit>

    // BELUM DIPAKAI: rancangan API pengaturan kas.
    @GET("api/config")
    suspend fun getConfig(): Response<Config>

    // BELUM DIPAKAI: rancangan API simpan pengaturan.
    @POST("api/config")
    suspend fun saveConfig(@Body request: ConfigRequest): Response<Unit>
}

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8080/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val authInterceptor = AuthInterceptor.getInstance()

    private val okHttp = OkHttpClient.Builder()
        .addInterceptor(logging)
        .addInterceptor(authInterceptor)
        .build()

    val api: ApiService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttp)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)
}

class AuthInterceptor private constructor() : okhttp3.Interceptor {
    private var token: String? = null

    // Token login untuk API (belum dipakai di versi offline).
    fun getToken(): String? = token

    // Menyimpan token login API.
    fun setToken(newToken: String?) {
        token = newToken
    }

    // Menghapus token saat logout.
    fun clearToken() {
        token = null
    }

    // Menempelkan token ke header setiap permintaan API (rancangan versi online).
    override fun intercept(chain: okhttp3.Interceptor.Chain): okhttp3.Response {
        val original = chain.request()
        val request = original.newBuilder()
        val tokenToUse = token
        if (!tokenToUse.isNullOrBlank()) {
            request.header("Authorization", "Bearer $tokenToUse")
        }
        return chain.proceed(request.build())
    }

    companion object {
        private var instance: AuthInterceptor? = null

        // Mengambil satu-satunya objek AuthInterceptor (pola singleton).
        fun getInstance(): AuthInterceptor {
            if (instance == null) {
                instance = AuthInterceptor()
            }
            return instance!!
        }
    }
}
