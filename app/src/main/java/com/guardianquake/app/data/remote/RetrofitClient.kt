package com.guardianquake.app.data.remote

import com.guardianquake.app.BuildConfig
import com.guardianquake.app.data.local.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.ConnectionPool
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { tokenManager.obtenerTokenSync() }
        val request = chain.request().newBuilder()
        if (!token.isNullOrBlank()) {
            request.addHeader("Authorization", "Bearer $token")
        }
        return chain.proceed(request.build())
    }
}

object RetrofitClient {

    @Volatile
    private var apiService: ApiService? = null

    fun getInstance(tokenManager: TokenManager): ApiService {
        return apiService ?: synchronized(this) {
            apiService ?: crear(tokenManager).also { apiService = it }
        }
    }

    private fun crear(tokenManager: TokenManager): ApiService {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenManager))
            .addInterceptor(logging)
            // Sin esto, OkHttp reutiliza conexiones TCP "en pool" que quedan
            // muertas cuando el sistema pierde y recupera internet -- las
            // peticiones fallan en silencio hasta que el pool expira solo
            // (hasta 5 minutos por defecto). maxIdleConnections = 0 fuerza
            // una conexion nueva en cada peticion, evitando ese problema.
            .connectionPool(ConnectionPool(0, 1, TimeUnit.NANOSECONDS))
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
