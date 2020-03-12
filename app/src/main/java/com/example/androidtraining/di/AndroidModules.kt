package com.example.androidtraining.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.example.androidtraining.Day
import com.example.androidtraining.DayInformation
import com.example.androidtraining.JsonAdapter
import com.example.androidtraining.MoshiJsonAdapter
import com.example.androidtraining.typeconverters.GitHubIssueListTypeConverter
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AndroidModules(private val application: Application){

    @Provides
    fun provideJsonAdapter(moshi: Moshi): JsonAdapter {
        return MoshiJsonAdapter(moshi)
    }

    @Provides
    fun provideMoshi(): Moshi{
        return Moshi.Builder().add(GitHubIssueListTypeConverter()).build()
    }

    @Provides
    fun provideDayInfo(): Day {
        return DayInformation()
    }

    @Singleton
    @Provides
    fun provideContext(): Context {
        return application
    }

    @Singleton
    @Provides
    fun provideApplication(): Application{
        return application
    }

    @Provides
    fun provideSharedPrefs(context: Context): SharedPreferences{
        return context.getSharedPreferences("github",Context.MODE_PRIVATE)
    }
}