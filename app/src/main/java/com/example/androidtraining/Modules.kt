package com.example.androidtraining

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.example.androidtraining.service.DevApi
import com.example.androidtraining.service.GitHubApi
import com.example.androidtraining.service.interceptor.AuthHeaderInterceptor
import dagger.Module
import dagger.Provides
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
class Modules(private val application: Application){

    @Singleton
    @Provides
    fun provideContext(): Context{
        return application
    }

    @Singleton
    @Provides
    fun provideApplication(): Application{
        return application
    }

    @Singleton
    @Provides
    fun provideDatabase(context: Context): GitHubDataBase{
        return GitHubDataBase.getInstance(context)!!
    }

    @Provides
    fun provideSharedPrefs(context: Context): SharedPreferences{
        return context.getSharedPreferences("github",Context.MODE_PRIVATE)
    }

    @Provides
    fun provideDevApi(): DevApi{
        return Retrofit.Builder()
            .baseUrl("https://devclassserver.foundersclub.software")
            .addConverterFactory(MoshiConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .build()
            .create(DevApi::class.java)
    }

    @Provides
    fun provideGitHubApi(sharedPreferences: SharedPreferences): GitHubApi{
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
}