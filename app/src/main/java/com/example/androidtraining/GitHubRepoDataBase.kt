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
                    //getInstance(context)?.populateDatabaseWithDay()
                }
                return instance
            }
        }
    }

    private fun populateDatabaseWithDay() {
        instance!!.dayDAO().getDayCount()
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribeBy(onSuccess = {if(it == 0){
                instance!!.dayDAO().insertDay(DayEntry("1998-03-10", 1)).subscribe()
                }
            })
    }
}
