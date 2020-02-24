package com.example.androidtraining.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.androidtraining.service.GitHubIssue
import com.example.androidtraining.service.GitHubIssueComment


@Database(entities = [GitHubRepo::class, GitHubIssue::class, GitHubIssueComment::class], version = 1)
abstract class GitHubDataBase : RoomDatabase() {

    abstract fun gitHubRepoDAO(): GitHubRepoDAO

    abstract fun gitHubIssueDAO(): GitHubIssueDAO

    abstract fun gitHubIssueCommentDAO(): GitHubIssueCommentDAO

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
