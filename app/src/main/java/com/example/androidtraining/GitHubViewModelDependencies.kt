package com.example.androidtraining

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams.fromPublisher
import androidx.paging.PagedList
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
    private val responseProcessor = ResponseProcessor(application, AppActivityLogger(), MoshiJsonAdapter())
    private val service: Service
    private val sharedPreferences = application.getSharedPreferences("github", Context.MODE_PRIVATE)


    init {
        val repoService = buildRetrofitGitHub()
        val loginService = buildRetrofitDev()
        service = RetroFitService(repoService, loginService, responseProcessor)
        val dayInformation = DayInformation()
        compositeDisposable = CompositeDisposable()
        val tellerRepoRepository = TellerRepoOnlineRepository(dataBase, service)
        val tellerIssueRepository = TellerIssueOnlineRepository(dataBase, service)
        val tellerIssueCommentRepository = TellerIssueCommentsOnlineRepository(dataBase, service)
        gitHubViewModelInjected = GitHubViewModelInjected(
            tellerRepoRepository,
            dayInformation,
            service,
            tellerIssueRepository,
            tellerIssueCommentRepository
        )
        gitHubViewModelInjected.initialSetup()
    }

    fun buildRetrofitDev(): DevApi {
        return Retrofit.Builder()
            .baseUrl("https://devclassserver.foundersclub.software")
            .addConverterFactory(MoshiConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .build()
            .create(DevApi::class.java)
    }

    fun buildRetrofitGitHub(): GitHubApi {
        val client = OkHttpClient.Builder()
            .addNetworkInterceptor(AuthHeaderInterceptor(sharedPreferences))
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

    fun userRepoRefresh() {
        gitHubViewModelInjected.refreshRepoCache()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
            .addTo(compositeDisposable)
    }

    fun loginToGithub(password: String, username: String): Single<Result<GitHubLoginResult>> {
        return gitHubViewModelInjected.loginToGithub(password, username)
    }

    fun getIssueObservable(): LiveData<OnlineCacheState<List<GitHubIssue>>> {
        val observable = gitHubViewModelInjected.getAllIssuesObservable()
        return fromPublisher(observable.toFlowable(BackpressureStrategy.LATEST))
    }

    fun getIssueCommentObservable(): LiveData<OnlineCacheState<PagedList<GitHubIssueComment>>>{
        val observable = gitHubViewModelInjected.getAllIssueCommentObservable()
        return fromPublisher(observable.toFlowable(BackpressureStrategy.LATEST))
    }

    fun userIssueRefresh() {
        gitHubViewModelInjected.refreshIssueCache()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
            .addTo(compositeDisposable)
    }

    fun userCommentsRefresh(){
        gitHubViewModelInjected.refreshCommentCache()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
            .addTo(compositeDisposable)
    }

    fun changeIssueUser(user: String){
        gitHubViewModelInjected.updateIssueUserRequirement(user)
    }

    fun changeIssueComment(issueNumber: Int,issueName: String,user: String, issueId: Int){
        gitHubViewModelInjected.updateIssueCommentRequirement(issueNumber,issueName,user,issueId)
    }
}

class GitHubViewModelInjected(
    private val repositoryRepo: TellerRepoOnlineRepository,
    private val day: DayInformation,
    private val service: Service,
    private var repositoryIssue: TellerIssueOnlineRepository,
    private var repositoryIssueComment: TellerIssueCommentsOnlineRepository
) {

    fun initialSetup() {
        val issueRequirements = TellerIssueOnlineRepository.GetIssuesRequirement("null")
        val repoRequirements = TellerRepoOnlineRepository.GetReposRequirement(day)
        val issueCommentRequirement = TellerIssueCommentsOnlineRepository.GetCommentRequirement(Int.MIN_VALUE,"","",
            Int.MIN_VALUE)
        repositoryRepo.requirements = repoRequirements
        repositoryIssue.requirements = issueRequirements
        repositoryIssueComment.requirements = issueCommentRequirement
    }

    fun updateIssueUserRequirement(user: String){
        val newReq = TellerIssueOnlineRepository.GetIssuesRequirement(user)
        repositoryIssue.requirements = newReq
    }

    fun updateIssueCommentRequirement(issueNumber: Int,issueName: String,user: String,issueId: Int){
        val newRequirements = TellerIssueCommentsOnlineRepository.GetCommentRequirement(issueNumber,issueName,user,issueId)
        repositoryIssueComment.requirements = newRequirements
    }

    fun getAllReposObservable(): Observable<OnlineCacheState<List<GitHubRepo>>> {
        return repositoryRepo.observe()
    }

    fun getAllIssuesObservable(): Observable<OnlineCacheState<List<GitHubIssue>>> {
        return repositoryIssue.observe()
    }

    fun getAllIssueCommentObservable(): Observable<OnlineCacheState<PagedList<GitHubIssueComment>>>{
        return repositoryIssueComment.observe()
    }

    fun refreshRepoCache(): Single<OnlineRepository.RefreshResult> {
        return repositoryRepo.refresh(true)
    }

    fun refreshIssueCache(): Single<OnlineRepository.RefreshResult> {
        return repositoryIssue.refresh(true)
    }

    fun refreshCommentCache(): Single<OnlineRepository.RefreshResult>{
        return repositoryIssueComment.refresh(true)
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
    override fun getIssueComments(issueNum: Int, issueName: String, user: String, issueId: Int): Single<ResultPaging<List<GitHubIssueComment>>> {
        return ghService.getIssueComment(issueNum.toString(),user,issueName).map { result ->
            val processedResponse = responseProcessor.process(result)
            val kotlinResult = if (processedResponse.isFailure()) {
                Result.failure(processedResponse.error!!)
            } else {
                Result.success(processedResponse.body!!)
            }
            kotlinResult.getOrNull()?.map { it.issueId = issueId }

            val moreData = result.response()?.headers()?.get("Link")?.contains("rel=\"next") ?: false

            ResultPaging(moreData,kotlinResult)
        }
    }

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

    fun getIssueComments(issueNum: Int, issueName: String, user: String, issueId: Int): Single<ResultPaging<List<GitHubIssueComment>>>
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