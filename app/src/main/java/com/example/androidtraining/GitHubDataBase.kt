package com.example.androidtraining

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.androidtraining.service.GitHubIssue


@Database(entities = [GitHubRepo::class, GitHubIssue::class], version = 1)
abstract class GitHubDataBase : RoomDatabase() {

    abstract fun gitHubRepoDAO(): GitHubRepoDAO

    abstract fun gitHubIssueDAO(): GitHubIssueDAO

    companion object {
        private var instance: GitHubDataBase? = null

        @Synchronized
        fun getInstance(context: Context): GitHubDataBase? {
            synchronized(this) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        GitHubDataBase::class.java,
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
