package com.example.androidtraining

import android.app.Application
import android.os.Handler
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import androidx.lifecycle.LiveDataReactiveStreams.*

class GitHubViewModelDependencies(application: Application) : AndroidViewModel(application) {

    private var minSinceLastRefresh = MutableLiveData<Int>()
    private val timeInformation = TimeInformation(minSinceLastRefresh)
    private val compositeDisposable : CompositeDisposable
    private val gitHubViewModelInjected : GitHubViewModelInjected

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
        compositeDisposable = CompositeDisposable()
        val gitHubRepository = GitHubRepository(dataBase, gitHubModel, retrofitService,dayInformation,compositeDisposable)
        gitHubViewModelInjected = GitHubViewModelInjected(gitHubRepository)
        gitHubViewModelInjected.initialSetup()
        //Set up time handler
        timeInformation.timeHandler().run()
    }

    fun getRepos() {
        gitHubViewModelInjected.getRepos()
    }

    fun getRepoList(): LiveData<List<GitHubRepo>>? {
        return gitHubViewModelInjected.getRepoList()
    }

    fun getMinSinceLastRefresh(): LiveData<Int> {
        return minSinceLastRefresh
    }

    fun resetLastRefresh() {
        timeInformation.resetLastRefresh()
    }

    fun getComposite(): CompositeDisposable {
        return compositeDisposable
    }

    fun getResultLiveData():LiveData<Result<List<GitHubRepo>>>{
        return gitHubViewModelInjected.getResultLiveData()
    }
}

class GitHubViewModelInjected(private var gitHubRepository: GitHubRepository) {
    private var repoList = gitHubRepository.getAllRepos()

    private var resultLiveData : MutableLiveData<Result<List<GitHubRepo>>> = MutableLiveData()

    fun getRepos(){
        val gitHubRepoListSingle = gitHubRepository.getDailyRepos()
        gitHubRepoListSingle
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
            onSuccess = {
                when(it.isError){
                false -> {gitHubRepository.checkYesterday()
                    Observable.fromIterable(it.response()!!.body()!!.items)
                    .observeOn(Schedulers.io())
                    .subscribeOn(Schedulers.io())
                    .subscribeBy(
                        onNext = { repo -> gitHubRepository.RepoModel.saveRepos(repo)},
                        onComplete = {
                            repoLiveDataSuccess()})}
                else -> {repoLiveDataFail(it.error()!!)}
            }},
            onError = {repoLiveDataFail(it)}
        )
            .addTo(gitHubRepository.disposable)
    }

    fun repoLiveDataFail(error: Throwable){
        val resultFail = Result.failure<List<GitHubRepo>>(error)
        CoroutineScope(Dispatchers.Main).launch{
            resultLiveData.value = resultFail
        }
    }

    fun repoLiveDataSuccess() {
        val resultSuccess = Result.success<List<GitHubRepo>>(listOf())
        CoroutineScope(Dispatchers.Main).launch {
            resultLiveData.value = resultSuccess
        }
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
            .addTo(gitHubRepository.disposable)

    }

    fun getRepoList(): LiveData<List<GitHubRepo>>?{
        return repoList
    }

    fun getResultLiveData():LiveData<Result<List<GitHubRepo>>>{
        return resultLiveData
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
