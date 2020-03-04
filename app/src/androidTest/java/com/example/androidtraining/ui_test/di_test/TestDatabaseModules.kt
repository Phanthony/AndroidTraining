package com.example.androidtraining.ui_test.di_test

import android.content.Context
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
}