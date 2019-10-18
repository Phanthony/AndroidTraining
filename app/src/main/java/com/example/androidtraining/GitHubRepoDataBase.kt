package com.example.androidtraining

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.androidtraining.service.GitHubIssue
import com.example.androidtraining.ui.GitHubIssueDAO


@Database(entities = [GitHubRepo::class, GitHubIssue::class], version = 31)
abstract class GitHubRepoDataBase : RoomDatabase() {

    abstract fun gitHubRepoDAO(): GitHubRepoDAO

    abstract fun gitHubIssueDAO(): GitHubIssueDAO

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
