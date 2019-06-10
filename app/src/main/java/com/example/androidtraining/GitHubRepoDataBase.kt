package com.example.androidtraining

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers


@Database(entities = [GitHubRepo::class, DayEntry::class], version = 17)
abstract class GitHubRepoDataBase : RoomDatabase() {

    abstract fun gitHubRepoDAO(): GitHubRepoDAO
    abstract fun dayDAO(): DayEntryDataDAO

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
