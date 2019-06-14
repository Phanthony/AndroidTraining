package com.example.androidtraining

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [GitHubRepo::class], version = 29)
abstract class GitHubRepoDataBase : RoomDatabase() {

    abstract fun gitHubRepoDAO(): GitHubRepoDAO

    companion object {
        private var instance: GitHubRepoDataBase? = null

        @Synchronized
        fun getInstance(context: Context): GitHubRepoDataBase? {
            synchronized(this) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        GitHubRepoDataBase::class.java,
                        "Repo_Database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                }
                return instance
            }
        }
    }

}
