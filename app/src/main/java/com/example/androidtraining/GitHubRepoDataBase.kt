package com.example.androidtraining

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [GitHubRepo::class],version = 1)
abstract class GitHubRepoDataBase: RoomDatabase() {


    abstract fun gitHubRepoDAO(): GitHubRepoDAO

    companion object {
        private lateinit var instance: GitHubRepoDataBase

        @Synchronized
        fun getInstance(context: Context): GitHubRepoDataBase {
            if (!this::instance.isInitialized) {
                instance = Room.databaseBuilder(context.applicationContext, GitHubRepoDataBase::class.java, "Repo_Database")
                    .fallbackToDestructiveMigration()
                    .build()
            }
            return instance
        }
    }
}
