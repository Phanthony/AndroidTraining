package com.example.androidtraining

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import androidx.lifecycle.LiveDataReactiveStreams.*
import com.example.androidtraining.service.AuthMobileRequestBody
import com.example.androidtraining.service.DevApi
import com.example.androidtraining.service.GitHubApi
import com.example.androidtraining.service.GitHubLoginResult
import com.example.androidtraining.service.error.UserEnteredBadDataResponseError
import com.example.androidtraining.service.interceptor.AuthHeaderInterceptor
import com.example.androidtraining.service.logger.AppActivityLogger
import com.levibostian.teller.cachestate.OnlineCacheState
import com.levibostian.teller.repository.OnlineRepository
import io.reactivex.BackpressureStrategy
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import okhttp3.OkHttpClient
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class GitHubViewModelDependencies(application: Application) : AndroidViewModel(application) {

    private val compositeDisposable: CompositeDisposable
    private val gitHubViewModelInjected: GitHubViewModelInjected
    private val dataBase: GitHubRepoDataBase = GitHubRepoDataBase.getInstance(application)!!
    private val tellerRepoRepository: TellerRepoOnlineRepository
    private val responseProcessor = ResponseProcessor(application, AppActivityLogger(), MoshiJsonAdapter())
    private val service: Service



    init {
        val accessToken = application.getSharedPreferences("github", Context.MODE_PRIVATE).getString("access_token", null)
        val repoService = buildRetrofitGitHub(accessToken)
        val loginService = Retrofit.Builder()
            .baseUrl("https://devclassserver.foundersclub.software")
            .addConverterFactory(MoshiConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .build()
            .create(DevApi::class.java)
        service = RetroFitService(repoService,loginService, responseProcessor)
        val dayInformation = DayInformation()
        compositeDisposable = CompositeDisposable()
        tellerRepoRepository = TellerRepoOnlineRepository(dataBase, service)
        gitHubViewModelInjected = GitHubViewModelInjected(tellerRepoRepository, dayInformation, service)
        gitHubViewModelInjected.initialSetup()
    }

    fun buildTellerIssueRepository(){

    }

    fun buildRetrofitGitHub(auth: String?): GitHubApi {
        val client = OkHttpClient.Builder()
            .addNetworkInterceptor(AuthHeaderInterceptor(auth))
            .build()
        return Retrofit.Builder()
            .client(client)
            .baseUrl("https://api.github.com/")
            .addConverterFactory(MoshiConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .build()
            .create(GitHubApi::class.java)
    }

    fun getComposite(): CompositeDisposable {
        return compositeDisposable
    }

    fun getRepoObservable(): LiveData<OnlineCacheState<List<GitHubRepo>>> {
        val observable = gitHubViewModelInjected.getAllReposObservable()
        return fromPublisher(observable.toFlowable(BackpressureStrategy.LATEST))
    }

    fun userRefresh() {
        gitHubViewModelInjected.refreshCache()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
            .addTo(compositeDisposable)
    }

    fun loginToGithub(password: String, username: String): Single<Result<GitHubLoginResult>>{
        return gitHubViewModelInjected.loginToGithub(password,username)
    }

}

class GitHubViewModelInjected(private val repositoryRepo: TellerRepoOnlineRepository, private val day: DayInformation, private val service: Service) {

    fun initialSetup() {
        val repoRequirements = TellerRepoOnlineRepository.GetReposRequirement(day)
        repositoryRepo.requirements = repoRequirements
    }

    fun getAllReposObservable(): Observable<OnlineCacheState<List<GitHubRepo>>> {
        return repositoryRepo.observe()
    }

    fun refreshCache(): Single<OnlineRepository.RefreshResult> {
        return repositoryRepo.refresh(true)
    }

    fun loginToGithub(password: String, username: String): Single<Result<GitHubLoginResult>>{
        return service.loginToGithub(password,username)
    }

}

class RetroFitService(private val ghService: GitHubApi, private val devService: DevApi, private val responseProcessor: ResponseProcessor) : Service {
    override fun getRepos(day: String): Single<Result<GitHubRepoList>> {
        val rxResult = ghService.getRepo("created:%3E$day+language:kotlin+stars:%3E0")
        return rxResult.map{ result ->
            val processedResponse = responseProcessor.process(result) { code, response, errorBody, jsonAdapter ->
                when (code) {
                    400 -> jsonAdapter.fromJson(errorBody, UserEnteredBadDataResponseError::class.java)
                    else -> null
                }
            }
            val kotlinResult = if (processedResponse.isFailure()){
                Result.failure(processedResponse.error!!)
            } else {
                Result.success(processedResponse.body!!)
            }

            kotlinResult
        }
    }

    override fun loginToGithub(password: String, username: String): Single<Result<GitHubLoginResult>> {
        return devService.loginGithub(
            AuthMobileRequestBody(
                listOf("repo"),
                "4cc5aa575096c8bcb036",
                password,
                username
            )
        ).map {
                result ->
            val processedResponse = responseProcessor.process(result) { code, response, errorBody, jsonAdapter ->
                when (code) {
                    401 -> jsonAdapter.fromJson(errorBody, UserEnteredBadDataResponseError::class.java)
                    else -> null
                }
            }
            val kotlinResult = if (processedResponse.isFailure()){
                Result.failure(processedResponse.error!!)
            } else {
                Result.success(processedResponse.body!!)
            }

            kotlinResult
        }
    }
}

interface Service {
    fun getRepos(day: String): Single<Result<GitHubRepoList>>

    fun loginToGithub(password: String, username: String): Single<Result<GitHubLoginResult>>
}

class DayInformation : Day {
    override fun getYesterday(): String {
        return (DateTimeFormatter.ofPattern("yyyy-MM-dd")
            .withLocale(Locale.US)
            .withZone(ZoneId.systemDefault()))
            .format(Instant.now().minus(Duration.ofDays(1)))
    }
}

interface Day {
    fun getYesterday(): String
}