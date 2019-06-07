package com.example.androidtraining

import android.app.Application
import android.os.Handler
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class GitHubViewModelDependencies(application: Application) : AndroidViewModel(application) {

    private var minSinceLastRefresh = MutableLiveData<Int>()
    private var timeInformation = TimeInformation(minSinceLastRefresh)

    private var gitHubViewModelInjected : GitHubViewModelInjected

    init {
        val dataBase = GitHubRepoDataBase.getInstance(application)!!
        val gitHubModel = ReposCompletedDatabase(dataBase)
        val retrofitService = RetroFitService(
            Retrofit.Builder()
                .baseUrl("https://api.github.com/")
                .addConverterFactory(MoshiConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .build()
                .create(GitHubApi::class.java)
        )
        val dayInformation = DayInformation()
        val gitHubRepository = GitHubRepository(dataBase, gitHubModel, retrofitService,dayInformation)
        gitHubViewModelInjected = GitHubViewModelInjected(gitHubRepository)
        gitHubViewModelInjected.initialSetup()
        //Set up time handler
        timeInformation.timeHandler().run()
    }

    fun getRepos() {
        gitHubViewModelInjected.getRepos()
    }

    fun getNetworkError(): LiveData<Int> {
        return gitHubViewModelInjected.getErrorCode()
    }

    fun getRepoList(): LiveData<List<GitHubRepo>>? {
        return gitHubViewModelInjected.getRepoList()
    }

    fun getMinSinceLastRefresh(): LiveData<Int> {
        return minSinceLastRefresh
    }

    fun resetNetworkError() {
       gitHubViewModelInjected.setErrorCode()
    }

    fun resetLastRefresh() {
        timeInformation.resetLastRefresh()
    }
}

class GitHubViewModelInjected(private var gitHubRepository: GitHubRepository) {

    val mComposite = CompositeDisposable()

    val FAILURE = 1
    val SUCCESS = 2

    private var errorCode = MutableLiveData<Int>()
    private var repoList = gitHubRepository.getAllRepos()

    fun getRepos(){
        val gitHubRepoListSingle = gitHubRepository.getDailyRepos()
        gitHubRepoListSingle
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
            onSuccess = {
                when(it.isError){
                false -> {Observable.fromIterable(it.response()!!.body()!!.items)
                    .observeOn(Schedulers.io())
                    .subscribeOn(Schedulers.io())
                    .subscribeBy(
                        onNext = {gitHubRepository.RepoModel.saveRepos(it)},
                        onComplete = {
                            setErrorCode(SUCCESS)})}
                else -> {setErrorCode(FAILURE)}
            }},
            onError = {setErrorCode(FAILURE)}
        )
            .addTo(mComposite)
    } 

    fun initialSetup() {
        //Set up lastday parameter in the repository
        gitHubRepository.getLastDayFromDatabase()
        // Initial pull if there is no data in the database

        gitHubRepository.getRepoCount()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {if(it == 0){
                    getRepos()
                }}
            )

    }

    fun getErrorCode(): LiveData<Int>{
        return errorCode
    }

    fun setErrorCode(code:Int = 0){
        CoroutineScope(Dispatchers.Main).launch{
            errorCode.value = code
        }
    }

    fun getRepoList(): LiveData<List<GitHubRepo>>?{
        return repoList
    }
}

class TimeInformation(private var minSinceLastRefresh: MutableLiveData<Int>) {

    private var lastRefreshed = getTime()

    fun getTime(): String {
        return (DateTimeFormatter.ofPattern("k:m:D")
            .withLocale(Locale.US)
            .withZone(ZoneId.systemDefault()))
            .format(Instant.now())
    }

    fun timePassed(initialTime: String, currentTime: String): Int {
        val initialTimeList = initialTime.split(":")
        val currentTimeList = currentTime.split(":")

        val daysPassed = currentTimeList[2].toInt() - initialTimeList[2].toInt()
        val hoursPassed = currentTimeList[0].toInt() - initialTimeList[0].toInt()
        var minutesPassed = 60 * hoursPassed + 1440 * daysPassed
        minutesPassed += currentTimeList[1].toInt() - initialTimeList[1].toInt()
        return minutesPassed
    }

    fun timeHandler(): java.lang.Runnable {
        val timeHandler = Handler()
        return object : Runnable {
            override fun run() {
                val minutesPassed = timePassed(lastRefreshed, getTime())
                minSinceLastRefresh.value = minutesPassed
                timeHandler.postDelayed(this, 60000)
            }
        }
    }

    fun resetLastRefresh() {
        lastRefreshed = getTime()
    }
}
