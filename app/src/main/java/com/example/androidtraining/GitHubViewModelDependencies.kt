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
import com.example.androidtraining.service.error.UserEnteredBadDataResponseError
import com.example.androidtraining.service.logger.AppActivityLogger
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
    private val responseProcessor = ResponseProcessor(application, AppActivityLogger(), MoshiJsonAdapter())
    private var retrofitLoginGithubService : RetroFitLoginService

    init {
        val retrofitRepoService = RetroFitRepoService(
            Retrofit.Builder()
                .baseUrl("https://api.github.com/")
                .addConverterFactory(MoshiConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .build()
                .create(GitHubApi::class.java)
        )
        val dayInformation = DayInformation()
        compositeDisposable = CompositeDisposable()
        tellerRepository = TellerOnlineRepository(dataBase, retrofitRepoService, responseProcessor)
        gitHubViewModelInjected = GitHubViewModelInjected(tellerRepository, dayInformation)
        gitHubViewModelInjected.initialSetup()

        retrofitLoginGithubService = RetroFitLoginService(Retrofit.Builder()
            .baseUrl("https://devclassserver.foundersclub.software")
            .addConverterFactory(MoshiConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .build()
            .create(GitHubLoginApi::class.java))
    }

    fun logIntoGitHub(username: String, password: String) : Single<OnlineRepository.FetchResponse<GitHubLoginResultSuccess>> {
        return retrofitLoginGithubService.loginToGithub(password,username)
            .map { result ->
                val processedResponse = responseProcessor.process(result) { code, response, errorBody, jsonAdapter ->
                    when (code) {
                        400 -> jsonAdapter.fromJson(errorBody, UserEnteredBadDataResponseError::class.java)
                        else -> null
                    }
                }
                val fetchResponse: OnlineRepository.FetchResponse<GitHubLoginResultSuccess> = if (processedResponse.isFailure()) {
                    OnlineRepository.FetchResponse.fail(processedResponse.error!!)
                } else {
                    OnlineRepository.FetchResponse.success(processedResponse.body!!)
                }

                fetchResponse
            }
    }

    fun getComposite(): CompositeDisposable {
        return compositeDisposable
    }

    fun getRepoObservable(): LiveData<OnlineCacheState<List<GitHubRepo>>> {
        val observable = gitHubViewModelInjected.getAllReposObservable()
        return fromPublisher(observable.toFlowable(BackpressureStrategy.LATEST))
    }

    fun userRefresh(){
        gitHubViewModelInjected.refreshCache()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
            .addTo(compositeDisposable)
    }

}

class GitHubViewModelInjected(private val repository: TellerOnlineRepository, private val day: DayInformation) {

    fun initialSetup() {
        val repoRequirements = TellerOnlineRepository.GetReposRequirement(day)
        repository.requirements = repoRequirements
    }

    fun getAllReposObservable(): Observable<OnlineCacheState<List<GitHubRepo>>> {
        return repository.observe()
    }

    fun refreshCache(): Single<OnlineRepository.RefreshResult> {
        return repository.refresh(true)
    }
}

class RetroFitRepoService(private var service: GitHubApi) : RepoService {
    override fun getRepos(day: String): Single<Result<GitHubRepoList>> {
        return service.getRepo("created:%3E$day+language:kotlin+stars:%3E0")
    }
}

class RetroFitLoginService(private var service: GitHubLoginApi) : LoginService{
    override fun loginToGithub(password: String, username: String): Single<Result<GitHubLoginResultSuccess>> {
        return service.loginGithub(authmobileRequestBody(listOf("repo"),"4cc5aa575096c8bcb036",password,username))
    }
}


interface RepoService {
    fun getRepos(day: String): Single<Result<GitHubRepoList>>
}

interface LoginService {
    fun loginToGithub(password: String,username: String): Single<Result<GitHubLoginResultSuccess>>
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