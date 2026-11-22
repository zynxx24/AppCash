// ==========================================================================
// PETA FILE · AppCash (Kelompok 6) · ApiService.kt
// Isi file : Definisi API server (Retrofit) untuk versi online.
// Penanda  : CARI-TIDAK-DIPAKAI
// Cara cari: VS Code Ctrl+Shift+F (Android Studio Ctrl+Shift+F) lalu ketik
//            "CARI-" untuk semua penanda, atau salah satu nama di baris Penanda.
// ==========================================================================
//  CATATAN: seluruh file ini (Retrofit, daftar API, token) adalah RANCANGAN versi online yang BELUM DIPAKAI.
//  Aplikasi sekarang offline penuh: semua data lewat AppCashRepository dan disimpan di HP.
// » Alamat folder file ini. Folder "network" = urusan internet. ISI FILE INI BELUM DIPAKAI (aplikasi
// » offline).
package com.appcash.network

// » Memanggil semua bentuk data dari Models.kt (tanda * = semuanya).
import com.appcash.data.model.*
// » OkHttpClient = "kurir" yang mengirim permintaan ke server di internet.
import okhttp3.OkHttpClient
// » HttpLoggingInterceptor = pencatat: menampilkan isi permintaan/jawaban di Logcat untuk membantu mencari
// » error.
import okhttp3.logging.HttpLoggingInterceptor
// » Response = bungkus jawaban server (isi + kode status).
import retrofit2.Response
// » Retrofit = alat yang mengubah daftar fungsi di bawah menjadi permintaan internet sungguhan.
import retrofit2.Retrofit
// » GsonConverterFactory = penerjemah JSON dari/ke objek Kotlin.
import retrofit2.converter.gson.GsonConverterFactory
// » Anotasi Retrofit: @GET, @POST, @Body, @Path, dst. (tanda * = semuanya).
import retrofit2.http.*

// CARI-TIDAK-DIPAKAI | Aplikasi saat ini offline; semua data berasal dari AppCashRepository.
// » interface = DAFTAR "menu" permintaan ke server. Hanya daftar nama, tanpa isi; Retrofit yang membuat
// » isinya.
interface ApiService {
    // BELUM DIPAKAI: rancangan API login untuk versi online. Versi sekarang tanpa server.
    // » @POST = kirim data ke alamat api/login (untuk login).
    @POST("api/login")
    // » suspend = fungsi yang boleh menunggu (jaringan lambat) tanpa membekukan layar. @Body = objek request
    // » dikirim sebagai isi permintaan. Hasilnya LoginResponse.
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    // BELUM DIPAKAI: rancangan API logout.
    // » Kirim ke api/logout untuk keluar.
    @POST("api/logout")
    // » Tidak ada isi jawaban (Unit = kosong).
    suspend fun logout(): Response<Unit>

    // BELUM DIPAKAI: rancangan API ringkasan dashboard.
    // » @GET = minta data dari api/dashboard.
    @GET("api/dashboard")
    // » Jawaban: objek Dashboard.
    suspend fun getDashboard(): Response<Dashboard>

    // BELUM DIPAKAI: rancangan API daftar anggota.
    // » Minta daftar anggota.
    @GET("api/members")
    // » Jawaban: daftar (List) Member.
    suspend fun getMembers(): Response<List<Member>>

    // BELUM DIPAKAI: rancangan API tabel pembayaran.
    // » Minta tabel pembayaran.
    @GET("api/payments")
    // » Jawaban: PaymentsWrapper.
    suspend fun getPayments(): Response<PaymentsWrapper>

    // BELUM DIPAKAI: rancangan API simpan pembayaran.
    // » Kirim (simpan) tabel pembayaran ke server.
    @POST("api/payments")
    // » Isi kirimannya adalah PaymentsWrapper.
    suspend fun savePayments(@Body wrapper: PaymentsWrapper): Response<Unit>

    // BELUM DIPAKAI: rancangan API daftar pengeluaran.
    // » Minta daftar pengeluaran.
    @GET("api/expenses")
    // » Jawaban: daftar Expense.
    suspend fun getExpenses(): Response<List<Expense>>

    // BELUM DIPAKAI: rancangan API tambah pengeluaran.
    // » Kirim pengeluaran baru.
    @POST("api/expenses")
    // » Isi kirimannya ExpenseRequest; jawabannya id yang baru.
    suspend fun addExpense(@Body request: ExpenseRequest): Response<IdResponse>

    // BELUM DIPAKAI: rancangan API hapus pengeluaran.
    // » Hapus pengeluaran dengan alamat api/expenses/{id}; {id} diganti nomor yang dikirim.
    @HTTP(method = "DELETE", path = "api/expenses/{id}", hasBody = true)
    // » @Path("id") = isi angka ke {id}.
    suspend fun deleteExpense(@Path("id") id: Int): Response<Unit>

    // BELUM DIPAKAI: rancangan API pengaturan kas.
    // » Minta pengaturan kas.
    @GET("api/config")
    // » Jawaban: Config.
    suspend fun getConfig(): Response<Config>

    // BELUM DIPAKAI: rancangan API simpan pengaturan.
    // » Simpan pengaturan kas.
    @POST("api/config")
    // » Isi kirimannya ConfigRequest.
    suspend fun saveConfig(@Body request: ConfigRequest): Response<Unit>
// » Tutup daftar permintaan.
}

// » object = satu-satunya objek (hanya ada satu di aplikasi). RetrofitClient = perakit koneksi ke server.
object RetrofitClient {
    // » Alamat dasar server. 10.0.2.2 = alamat khusus emulator Android untuk menunjuk ke komputer pembuatnya
    // » (localhost).
    private const val BASE_URL = "http://10.0.2.2:8080/"

    // » Membuat pencatat log; apply { } = langsung atur isinya di dalam kurung.
    private val logging = HttpLoggingInterceptor().apply {
        // » Level BODY = catat seluruh isi permintaan dan jawaban.
        level = HttpLoggingInterceptor.Level.BODY
    }

    // » Mengambil objek penempel token login.
    private val authInterceptor = AuthInterceptor.getInstance()

    // » Membuat kurir OkHttp dengan dua "penjaga": ...
    private val okHttp = OkHttpClient.Builder()
        // » ... pencatat log, ...
        .addInterceptor(logging)
        // » ... dan penempel token. Lalu .build() menyelesaikannya.
        .addInterceptor(authInterceptor)
        .build()

    // » Membuat objek api: gabungan semua yang dirakit di bawah.
    val api: ApiService = Retrofit.Builder()
        // » Alamat dasar server.
        .baseUrl(BASE_URL)
        // » Kurir yang dipakai.
        .client(okHttp)
        // » Penerjemah JSON.
        .addConverterFactory(GsonConverterFactory.create())
        // » Selesai merakit Retrofit.
        .build()
        // » Membuat isi nyata untuk daftar ApiService.
        .create(ApiService::class.java)
}

// » AuthInterceptor = penjaga yang menempelkan token login ke tiap permintaan. "private constructor" = tidak
// » boleh dibuat dari luar, harus lewat getInstance() (satu objek saja = singleton).
class AuthInterceptor private constructor() : okhttp3.Interceptor {
    // » Menyimpan token; null = belum login.
    private var token: String? = null

    // Token login untuk API (belum dipakai di versi offline).
    // » Mengambil token yang tersimpan.
    fun getToken(): String? = token

    // Menyimpan token login API.
    // » Mengisi/menyimpan token baru.
    fun setToken(newToken: String?) {
        // » Isi token dengan nilai baru.
        token = newToken
    }

    // Menghapus token saat logout.
    // » Menghapus token (dipakai saat logout).
    fun clearToken() {
        // » Mengosongkan token.
        token = null
    }

    // Menempelkan token ke header setiap permintaan API (rancangan versi online).
    // » intercept = dijalankan otomatis setiap kali ada permintaan keluar. Tugasnya menyisipkan token.
    override fun intercept(chain: okhttp3.Interceptor.Chain): okhttp3.Response {
        // » Ambil permintaan asli.
        val original = chain.request()
        // » Salin permintaan itu supaya boleh diubah.
        val request = original.newBuilder()
        // » Ambil token ke variabel sementara.
        val tokenToUse = token
        // » Kalau token ada isinya...
        if (!tokenToUse.isNullOrBlank()) {
            // » ...tambah header "Authorization: Bearer <token>" (format standar untuk menunjukkan sudah login).
            request.header("Authorization", "Bearer $tokenToUse")
        }
        // » Lanjutkan permintaan yang sudah ditambah header ke server.
        return chain.proceed(request.build())
    }

    // » companion object = bagian milik kelas, bukan milik objek (mirip "static").
    companion object {
        // » Menyimpan satu-satunya objek AuthInterceptor.
        private var instance: AuthInterceptor? = null

        // Mengambil satu-satunya objek AuthInterceptor (pola singleton).
        // » Fungsi untuk mengambil satu-satunya objek tersebut.
        fun getInstance(): AuthInterceptor {
            // » Kalau belum pernah dibuat...
            if (instance == null) {
                // » ...buat sekarang (hanya sekali).
                instance = AuthInterceptor()
            }
            // » Kembalikan objek itu. "!!" = yakin tidak kosong.
            return instance!!
        }
    }
}
