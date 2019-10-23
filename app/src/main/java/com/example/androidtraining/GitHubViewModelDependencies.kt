package com.example.androidtraining

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams.fromPublisher
import com.example.androidtraining.service.*
import com.example.androidtraining.service.error.UserEnteredBadDataResponseError
import com.example.androidtraining.service.interceptor.AuthHeaderInterceptor
import com.example.androidtraining.service.logger.AppActivityLogger
import com.levibostian.teller.cachestate.OnlineCacheState
import com.levibostian.teller.repository.OnlineRepository
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class GitHubViewModelDependencies(application: Application) : AndroidViewModel(application) {

    private val compositeDisposable: CompositeDisposable
    private val gitHubViewModelInjected: GitHubViewModelInjected
    private val dataBase: GitHubDataBase = GitHubDataBase.getInstance(application)!!
    private val tellerRepoRepository: TellerRepoOnlineRepository
    private val responseProcessor = ResponseProcessor(application, AppActivityLogger(), MoshiJsonAdapter())
    private val service: Service


    init {
        val accessToken =
            application.getSharedPreferences("github", Context.MODE_PRIVATE).getString("access_token", null)
        val repoService = buildRetrofitGitHub(accessToken)
        val loginService = buildRetrofitDev()
        service = RetroFitService(repoService, loginService, responseProcessor)
        val dayInformation = DayInformation()
        compositeDisposable = CompositeDisposable()
        tellerRepoRepository = TellerRepoOnlineRepository(dataBase, service)
        gitHubViewModelInjected = GitHubViewModelInjected(tellerRepoRepository, dayInformation, service, null)
        gitHubViewModelInjected.initialSetup()
        if (accessToken != null) {
            buildTellerIssueRepository(accessToken)
        }
    }

    fun buildTellerIssueRepository(auth: String) {
        val gHService = buildRetrofitGitHub(auth)
        val devService = buildRetrofitDev()
        val service = RetroFitService(gHService, devService, responseProcessor)
        gitHubViewModelInjected.updateIssueRepository(TellerIssueOnlineRepository(dataBase, service))
    }

    fun buildRetrofitDev(): DevApi {
        return Retrofit.Builder()
            .baseUrl("https://devclassserver.foundersclub.software")
            .addConverterFactory(MoshiConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .build()
            .create(DevApi::class.java)
    }

    fun buildRetrofitGitHub(auth: String?): GitHubApi {
        if (auth != null) {
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
        } else {
            return Retrofit.Builder()
                .baseUrl("https://api.github.com/")
                .addConverterFactory(MoshiConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .build()
                .create(GitHubApi::class.java)
        }
    }

    fun getComposite(): CompositeDisposable {
        return compositeDisposable
    }

    fun getRepoObservable(): LiveData<OnlineCacheState<List<GitHubRepo>>> {
        val observable = gitHubViewModelInjected.getAllReposObservable()
        return fromPublisher(observable.toFlowable(BackpressureStrategy.LATEST))
    }

    fun userRepoRefresh() {
        gitHubViewModelInjected.refreshRepoCache()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
            .addTo(compositeDisposable)
    }

    fun loginToGithub(password: String, username: String): Single<Result<GitHubLoginResult>> {
        return gitHubViewModelInjected.loginToGithub(password, username)
    }

    fun getIssueObservable(): LiveData<OnlineCacheState<List<GitHubIssue>>>{
        val observable = gitHubViewModelInjected.getAllIssuesObservable()
        return fromPublisher(observable.toFlowable(BackpressureStrategy.LATEST))
    }

    fun userIssueRefresh(){
        gitHubViewModelInjected.refreshIssueCache()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
            .addTo(compositeDisposable)
    }

}

class GitHubViewModelInjected(
    private val repositoryRepo: TellerRepoOnlineRepository,
    private val day: DayInformation,
    private val service: Service,
    private var repositoryIssue: TellerIssueOnlineRepository?
) {
    fun updateIssueRepository(updated: TellerIssueOnlineRepository){
        repositoryIssue = updated
        val issueRequirements = TellerIssueOnlineRepository.GetIssuesRequirement()
        repositoryIssue!!.requirements = issueRequirements
    }

    fun initialSetup() {
        val repoRequirements = TellerRepoOnlineRepository.GetReposRequirement(day)
        repositoryRepo.requirements = repoRequirements
    }

    fun getAllReposObservable(): Observable<OnlineCacheState<List<GitHubRepo>>> {
        return repositoryRepo.observe()
    }

    fun getAllIssuesObservable(): Observable<OnlineCacheState<List<GitHubIssue>>> {
        return repositoryIssue!!.observe()
    }

    fun refreshRepoCache(): Single<OnlineRepository.RefreshResult> {
        return repositoryRepo.refresh(true)
    }

    fun refreshIssueCache(): Single<OnlineRepository.RefreshResult>{
        return repositoryIssue!!.refresh(true)
    }

    fun loginToGithub(password: String, username: String): Single<Result<GitHubLoginResult>> {
        return service.loginToGithub(password, username)
    }

}

class RetroFitService(
    private val ghService: GitHubApi,
    private val devService: DevApi,
    private val responseProcessor: ResponseProcessor
) : Service {
    override fun getIssues(): Single<Result<List<GitHubIssue>>> {
        return ghService.getIssues().map { result ->
            val processedResponse = responseProcessor.process(result)
            val kotlinResult = if (processedResponse.isFailure()) {
                Result.failure(processedResponse.error!!)
            } else {
                Result.success(processedResponse.body!!)
            }
            kotlinResult
        }
    }

    override fun getRepos(day: String): Single<Result<GitHubRepoList>> {
        val rxResult = ghService.getRepo("created:%3E$day+language:kotlin+stars:%3E0")
        return rxResult.map { result ->
            val processedResponse = responseProcessor.process(result)
            val kotlinResult = if (processedResponse.isFailure()) {
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
        ).map { result ->
            val processedResponse = responseProcessor.process(result) { code, response, errorBody, jsonAdapter ->
                when (code) {
                    401 -> jsonAdapter.fromJson(errorBody, UserEnteredBadDataResponseError::class.java)
                    else -> null
                }
            }
            val kotlinResult = if (processedResponse.isFailure()) {
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

    fun getIssues(): Single<Result<List<GitHubIssue>>>
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