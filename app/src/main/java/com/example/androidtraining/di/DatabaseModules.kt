package com.example.androidtraining.di

import android.content.Context
import com.example.androidtraining.database.GitHubDataBase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DatabaseModules {
    @Singleton
    @Provides
    fun provideDatabase(context: Context): GitHubDataBase {
        return GitHubDataBase.getInstance(
            context
        )!!
    }
}