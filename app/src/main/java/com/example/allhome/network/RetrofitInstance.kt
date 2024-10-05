package com.example.allhome.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val BASE_URL = "http://192.168.100.127:8084"

    // Create a CSRF Token Interceptor
    private val csrfInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val requestWithToken = originalRequest.newBuilder()
            .header("X-CSRF-Token", provideCsrfToken()) // Replace provideCsrfToken() with your logic to retrieve the token
            .build()
        chain.proceed(requestWithToken)
    }

    // Function to provide CSRF Token (replace with your logic)
    private fun provideCsrfToken(): String {
        // Implement your logic to retrieve CSRF token here, e.g., from SharedPreferences or any storage
        return "QVcwR+LaLSZw07ORhiIvujT+H71nDPcUHNQ78cHAAdieqsrcgnj0EWyD9+T28TmlpSvGXg1OKpLcxjjLC6yypK+9+DCar0RHBZsn31FvxiF+4VnHpPgx4fue/O2yS8BzD7bA3ZfADZKcTaM9NC9Lpw=="
    }

    private val okHttpClient = OkHttpClient.Builder()
        .cookieJar(SimpleCookieJar()) // Use the SimpleCookieJar
        .addInterceptor(csrfInterceptor) // Add CSRF Token Interceptor
        .build()

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient) // Use the configured OkHttpClient
            .build()
    }

    val api: UploadApi by lazy {
        retrofit.create(UploadApi::class.java)
    }
}
