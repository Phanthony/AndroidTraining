package com.example.androidtraining.di

import android.content.SharedPreferences
import com.example.androidtraining.service.*
import com.example.androidtraining.service.interceptor.AuthHeaderInterceptor
import com.example.androidtraining.service.logger.ActivityLogger
import com.example.androidtraining.service.logger.AppActivityLogger
import dagger.Module
import dagger.Provides
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

@Module
class NetworkModules {
    @Provides
    fun provideDevApi(): DevApi {
        return Retrofit.Builder()
            .baseUrl("https://devclassserver.foundersclub.software")
            .addConverterFactory(MoshiConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .build()
            .create(DevApi::class.java)
    }

    @Provides
    fun provideService(devApi: DevApi, gitHubApi: GitHubApi, responseProcessor: ResponseProcessor): Service {
        return RetrofitService(gitHubApi,devApi,responseProcessor)
    }

    @Provides
    fun provideGitHubApi(sharedPreferences: SharedPreferences): GitHubApi {
        val client = OkHttpClient.Builder()
            .addNetworkInterceptor(AuthHeaderInterceptor(sharedPreferences))
            .build()
        return Retrofit.Builder()
            .client(client)
            .baseUrl("https://api.github.com/")
            .addConverterFactory(MoshiConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .build()
            .create(GitHubApi::class.java)
    }

    @Provides
    fun provideActivityLogger(): ActivityLogger {
        return AppActivityLogger()
    }
}