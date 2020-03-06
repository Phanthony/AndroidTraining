package com.example.androidtraining.ui_test.di_test
import android.content.SharedPreferences
import com.example.androidtraining.service.interceptor.AuthHeaderInterceptor
import com.squareup.moshi.Moshi
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ServiceProvider @Inject constructor(val moshi: Moshi) {

    fun <T> get(endpoint: String, clazz: Class<T>, sharedPreferences: SharedPreferences? = null): T{
        return getService(endpoint, sharedPreferences).create(clazz)
    }

    private fun getService(hostname: String,sharedPreferences: SharedPreferences? = null): Retrofit {
        val client = OkHttpClient.Builder().apply {
            if(sharedPreferences != null){
                addNetworkInterceptor(AuthHeaderInterceptor(sharedPreferences))
            }
        }.connectTimeout(2, TimeUnit.SECONDS) // For testing purposes
            .readTimeout(2, TimeUnit.SECONDS) // For testing purposes
            .writeTimeout(2, TimeUnit.SECONDS).build()

        return Retrofit.Builder()
            .client(client)
            .baseUrl(hostname)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

}