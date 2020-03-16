package com.example.androidtraining

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams.fromPublisher
import androidx.paging.PagedList
import com.example.androidtraining.database.GitHubDataBase
import com.example.androidtraining.database.GitHubRepo
import com.example.androidtraining.database.teller.TellerIssueCommentsOnlineRepository
import com.example.androidtraining.database.teller.TellerIssueOnlineRepository
import com.example.androidtraining.database.teller.TellerRepoOnlineRepository
import com.example.androidtraining.service.GitHubIssue
import com.example.androidtraining.service.GitHubIssueComment
import com.example.androidtraining.service.GitHubLoginResult
import com.example.androidtraining.service.Service
import com.levibostian.teller.cachestate.OnlineCacheState
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GitHubViewModel @Inject constructor(
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

    fun userRepoRefresh(): Completable {
        return repositoryRepo.refresh(true)
            .observeOn(AndroidSchedulers.mainThread()).ignoreElement()
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