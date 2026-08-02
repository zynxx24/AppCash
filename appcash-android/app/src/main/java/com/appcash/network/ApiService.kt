package com.appcash.network

import com.appcash.data.model.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface ApiService {
    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/logout")
    suspend fun logout(): Response<Unit>

    @GET("api/dashboard")
    suspend fun getDashboard(): Response<Dashboard>

    @GET("api/members")
    suspend fun getMembers(): Response<List<Member>>

    @GET("api/payments")
    suspend fun getPayments(): Response<PaymentsWrapper>

    @POST("api/payments")
    suspend fun savePayments(@Body wrapper: PaymentsWrapper): Response<Unit>

    @GET("api/expenses")
    suspend fun getExpenses(): Response<List<Expense>>

    @POST("api/expenses")
    suspend fun addExpense(@Body request: ExpenseRequest): Response<IdResponse>

    @HTTP(method = "DELETE", path = "api/expenses/{id}", hasBody = true)
    suspend fun deleteExpense(@Path("id") id: Int): Response<Unit>

    @GET("api/config")
    suspend fun getConfig(): Response<Config>

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

    fun getToken(): String? = token

    fun setToken(newToken: String?) {
        token = newToken
    }

    fun clearToken() {
        token = null
    }

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

        fun getInstance(): AuthInterceptor {
            if (instance == null) {
                instance = AuthInterceptor()
            }
            return instance!!
        }
    }
}
