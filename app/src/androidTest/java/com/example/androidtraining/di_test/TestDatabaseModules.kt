package com.example.androidtraining.di_test

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.example.androidtraining.database.GitHubDataBase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class TestDatabaseModules {

    @Provides
    @Singleton
    fun provideDatabase(context: Context): GitHubDataBase {
        return Room.inMemoryDatabaseBuilder(context, GitHubDataBase::class.java).build()
    }

    @Provides
    fun provideSharedPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences("github_test",Context.MODE_PRIVATE)
    }
}