package com.example.androidtraining.service.interceptor

import android.content.SharedPreferences
import okhttp3.Interceptor
import okhttp3.Response

class AuthHeaderInterceptor(var sharedPreferences: SharedPreferences): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        if(sharedPreferences.contains("access_token")){
            val authToken = sharedPreferences.getString("access_token",null)
            request = request
                .newBuilder()
                .addHeader("Authorization", "Bearer $authToken")
                .build()
        }

        return chain.proceed(request)
    }
}