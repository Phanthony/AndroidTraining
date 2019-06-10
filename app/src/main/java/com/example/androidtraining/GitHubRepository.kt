package com.example.androidtraining

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.*
import io.reactivex.rxkotlin.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import retrofit2.adapter.rxjava2.Result
import java.io.IOException
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class GitHubRepository(var db: GitHubRepoDataBase, var RepoModel: ReposCompleted, var service: Service, var day: Day, var disposable: CompositeDisposable) {

    private var lastDay: String? = null

    fun checkYesterday() {
        if (!lastDay.equals(day.getYesterday())) {
            insertYesterdayToDatabase()
            deleteAllRepos()
        }
    }

    //Error Code -> 1 = Unsuccessful, 2 = Successful, 0 = Default state
    fun getDailyRepos(): Single<Result<GitHubRepoList>> {
        return service.getRepos(day.getYesterday())
    }

    // All Database Functions
    private fun deleteAllRepos() {
        db.gitHubRepoDAO().deleteAllRepos()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
    }

    private fun insertYesterdayToDatabase() {
        db.dayDAO().insertDay(DayEntry(day.getYesterday(),1))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onComplete = {
                    getLastDayFromDatabase()
                }
            )
            .addTo(disposable)

        //db.dayDAO().insertDay(DayEntry(day.getYesterday(), 1))
        //lastDay = db.dayDAO().getDay()
    }

    //These functions are accessed from ViewModel Layer
   fun getLastDayFromDatabase() {
        db.dayDAO().getDay()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {lastDay = it},
                onError = {}
            )
            .addTo(disposable)
    }

    fun getAllRepos(): LiveData<List<GitHubRepo>>? {
        return db.gitHubRepoDAO().getAllRepos()
    }

    fun getRepoCount(): Single<Int> {
        return db.gitHubRepoDAO().getRepoCount()
    }



}

class ReposCompletedDatabase(db: GitHubRepoDataBase) : ReposCompleted {
    private val githubRepoDAO = db.gitHubRepoDAO()
    override fun saveRepos(gitHubRepo: GitHubRepo) {
        githubRepoDAO.insert(gitHubRepo)
    }
}

class RetroFitService(private var service: GitHubApi) : Service {
    override fun getRepos(day: String): Single<Result<GitHubRepoList>> {
        return service.getRepo("created:%3E$day+language:kotlin+stars:%3E0")
    }
}

class DayInformation: Day{
    override fun getYesterday(): String {
        return (DateTimeFormatter.ofPattern("yyyy-MM-dd")
            .withLocale(Locale.US)
            .withZone(ZoneId.systemDefault()))
            .format(Instant.now().minus(Duration.ofDays(1)))
    }
}

interface Service {
    fun getRepos(day: String): Single<Result<GitHubRepoList>>
}

interface ReposCompleted {
   fun saveRepos(gitHubRepo: GitHubRepo)
}

interface Day{
    fun getYesterday(): String
}

