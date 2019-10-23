package com.example.androidtraining.service.interceptor

import okhttp3.Interceptor
import okhttp3.Response

class AuthHeaderInterceptor(val authToken: String): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        authToken.let {
            request = request
                .newBuilder()
                .addHeader("Authorization", "Bearer $authToken")
                .build()
        }

        return chain.proceed(request)
    }
}