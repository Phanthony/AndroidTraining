package com.example.androidtraining

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import androidx.lifecycle.LiveDataReactiveStreams.*
import com.levibostian.teller.cachestate.OnlineCacheState
import com.levibostian.teller.repository.OnlineRepository
import io.reactivex.BackpressureStrategy
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import retrofit2.adapter.rxjava2.Result
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class GitHubViewModelDependencies(application: Application) : AndroidViewModel(application) {

    private val compositeDisposable: CompositeDisposable
    private val gitHubViewModelInjected: GitHubViewModelInjected
    private val dataBase: GitHubRepoDataBase = GitHubRepoDataBase.getInstance(application)!!
    private val tellerRepository : TellerOnlineRepository

    init {
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
        tellerRepository = TellerOnlineRepository(dataBase, retrofitService)
        gitHubViewModelInjected = GitHubViewModelInjected(tellerRepository, dayInformation)
        gitHubViewModelInjected.initialSetup()
    }

    fun getComposite(): CompositeDisposable {
        return compositeDisposable
    }

    fun getRepoObservable(): LiveData<OnlineCacheState<List<GitHubRepo>>> {
        val observable = gitHubViewModelInjected.getAllReposObservable()
        return fromPublisher(observable.toFlowable(BackpressureStrategy.LATEST))
    }

    fun userRefresh(){
        gitHubViewModelInjected.updateTellerRequirement()
        gitHubViewModelInjected.refreshCache()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
            .addTo(compositeDisposable)
    }

    fun clearDB(){
        dataBase.gitHubRepoDAO().deleteAllRepos()
            .subscribeOn(Schedulers.io())
            .subscribe()
            .addTo(compositeDisposable)
    }
}

class GitHubViewModelInjected(private val repository: TellerOnlineRepository, private val day: DayInformation) {

    fun updateTellerRequirement(){
        val repoRequirements = TellerOnlineRepository.GetReposRequirement(day.getYesterday())
        repository.requirements = repoRequirements
    }

    fun getRepos(): Single<OnlineRepository.FetchResponse<GitHubRepoList>> {
        val repoRequirements = TellerOnlineRepository.GetReposRequirement(day.getYesterday())
        return repository.fetchFreshCache(repoRequirements)
    }

    fun initialSetup() {
        updateTellerRequirement()
    }

    fun getAllReposObservable(): Observable<OnlineCacheState<List<GitHubRepo>>> {
        return repository.observe()
    }

    fun refreshCache(): Single<OnlineRepository.RefreshResult> {
        return repository.refresh(true)
    }
}

class RetroFitService(private var service: GitHubApi) : Service {
    override fun getRepos(day: String): Single<Result<GitHubRepoList>> {
        return service.getRepo("created:%3E$day+language:kotlin+stars:%3E0")
    }
}

interface Service {
    fun getRepos(day: String): Single<Result<GitHubRepoList>>
}

class DayInformation: Day{
    override fun getYesterday(): String {
        return (DateTimeFormatter.ofPattern("yyyy-MM-dd")
            .withLocale(Locale.US)
            .withZone(ZoneId.systemDefault()))
            .format(Instant.now().minus(Duration.ofDays(1)))
    }
}

interface Day{
    fun getYesterday(): String
}