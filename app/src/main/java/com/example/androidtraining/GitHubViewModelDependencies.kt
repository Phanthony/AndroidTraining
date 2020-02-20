package com.example.androidtraining

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams.fromPublisher
import androidx.paging.PagedList
import com.example.androidtraining.service.*
import com.example.androidtraining.service.error.UserEnteredBadDataResponseError
import com.levibostian.teller.cachestate.OnlineCacheState
import io.reactivex.BackpressureStrategy
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GitHubViewModelDependencies @Inject constructor(
    application: Application,
    var repositoryRepo: TellerRepoOnlineRepository,
    var day: Day,
    var service: Service,
    var repositoryIssue: TellerIssueOnlineRepository,
    var repositoryIssueComment: TellerIssueCommentsOnlineRepository,
    var database: GitHubDataBase
) : AndroidViewModel(application) {

    init {
        initialSetup()
    }

    fun initialSetup() {
        val issueRequirements = TellerIssueOnlineRepository.GetIssuesRequirement("null")
        val repoRequirements = TellerRepoOnlineRepository.GetReposRequirement(day)
        val issueCommentRequirement = TellerIssueCommentsOnlineRepository.GetCommentRequirement(Int.MIN_VALUE,"","",
            Int.MIN_VALUE)
        repositoryRepo.requirements = repoRequirements
        repositoryIssue.requirements = issueRequirements
        repositoryIssueComment.requirements = issueCommentRequirement
    }

    fun getRepoObservable(): LiveData<OnlineCacheState<List<GitHubRepo>>> {
        val observable = repositoryRepo.observe()
        return fromPublisher(observable.toFlowable(BackpressureStrategy.LATEST))
    }

    fun userRepoRefresh() {
        repositoryRepo.refresh(true)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
    }

    fun loginToGithub(password: String, username: String): Single<Result<GitHubLoginResult>> {
        return service.loginToGithub(password, username)
    }

    fun getIssueObservable(): LiveData<OnlineCacheState<List<GitHubIssue>>> {
        val observable = repositoryIssue.observe()
        return fromPublisher(observable.toFlowable(BackpressureStrategy.LATEST))
    }

    fun getIssueCommentObservable(): LiveData<OnlineCacheState<PagedList<GitHubIssueComment>>> {
        val observable = repositoryIssueComment.observe()
        return fromPublisher(observable.toFlowable(BackpressureStrategy.LATEST))
    }

    fun userIssueRefresh() {
        repositoryIssue.refresh(true)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
    }

    fun userCommentsRefresh() {
        repositoryIssueComment.refresh(true)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
    }

    fun changeIssueUser(user: String) {
        val newReq = TellerIssueOnlineRepository.GetIssuesRequirement(user)
        repositoryIssue.requirements = newReq
    }

    fun changeIssueComment(issueNumber: Int, issueName: String, user: String, issueId: Int) {
        val newRequirements = TellerIssueCommentsOnlineRepository.GetCommentRequirement(
            issueNumber,
            issueName,
            user,
            issueId
        )
        repositoryIssueComment.requirements = newRequirements
    }
}

class RetroFitService @Inject constructor(
    private val ghService: GitHubApi,
    private val devService: DevApi,
    private val responseProcessor: ResponseProcessor
) : Service {
    override fun getIssueComments(
        issueNum: Int,
        issueName: String,
        user: String,
        issueId: Int
    ): Single<ResultPaging<List<GitHubIssueComment>>> {
        return ghService.getIssueComment(issueNum.toString(), user, issueName).map { result ->
            val processedResponse = responseProcessor.process(result)
            val kotlinResult = if (processedResponse.isFailure()) {
                Result.failure(processedResponse.error!!)
            } else {
                Result.success(processedResponse.body!!)
            }
            kotlinResult.getOrNull()?.map { it.issueId = issueId }

            val moreData =
                result.response()?.headers()?.get("Link")?.contains("rel=\"next") ?: false

            ResultPaging(moreData, kotlinResult)
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

    override fun loginToGithub(
        password: String,
        username: String
    ): Single<Result<GitHubLoginResult>> {
        return devService.loginGithub(
            AuthMobileRequestBody(
                listOf("repo"),
                "4cc5aa575096c8bcb036",
                password,
                username
            )
        ).map { result ->
            val processedResponse =
                responseProcessor.process(result) { code, response, errorBody, jsonAdapter ->
                    when (code) {
                        401 -> jsonAdapter.fromJson(
                            errorBody,
                            UserEnteredBadDataResponseError::class.java
                        )
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

    fun getIssueComments(
        issueNum: Int,
        issueName: String,
        user: String,
        issueId: Int
    ): Single<ResultPaging<List<GitHubIssueComment>>>
}

class DayInformation @Inject constructor() : Day {
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