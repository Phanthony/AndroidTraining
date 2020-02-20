package com.example.androidtraining

import androidx.paging.PagedList
import androidx.paging.toObservable
import com.example.androidtraining.service.GitHubIssueComment
import com.levibostian.teller.repository.GetCacheRequirementsTag
import com.levibostian.teller.repository.OnlinePagingRepository
import com.levibostian.teller.type.Age
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class TellerIssueCommentsOnlineRepository @Inject constructor(private val db: GitHubDataBase, private val service: Service) :
    OnlinePagingRepository<PagedList<GitHubIssueComment>, TellerIssueCommentsOnlineRepository.PagingRequirements, TellerIssueCommentsOnlineRepository.GetCommentRequirement, List<GitHubIssueComment>>(
        PagingRequirements()
    ) {

    companion object {
        private const val PAGE_SIZE = 30
    }

    private var morePagesDataToLoad = true

    override fun deleteOldCache(requirements: GetCommentRequirement, persistFirstPage: Boolean): Completable {
        return Completable.fromCallable {
            db.gitHubIssueCommentDAO().getComment(requirements.issueID).let {
                val commentsToDelete: List<GitHubIssueComment>? = when {
                    (persistFirstPage && it.size < 30) -> null
                    persistFirstPage -> it.subList(PAGE_SIZE, it.size)
                    else -> it
                }

                commentsToDelete?.let {
                    for (i in commentsToDelete) {
                        db.gitHubIssueCommentDAO().deleteComment(i.id)
                    }
                }
            }
        }.subscribeOn(Schedulers.io())
    }

    override fun fetchFreshCache(
        requirements: GetCommentRequirement,
        pagingRequirements: PagingRequirements
    ): Single<FetchResponse<List<GitHubIssueComment>>> {
        return service.getIssueComments(requirements.issueNumber,requirements.issueName,requirements.user,requirements.issueID)
            .map {
                val fetchResponse: FetchResponse<List<GitHubIssueComment>> = if (it.result.isFailure) {
                    FetchResponse.fail(it.result.exceptionOrNull()!!)
                } else {
                    FetchResponse.success(it.result.getOrNull()!!)
                }
                morePagesDataToLoad = it.morePagesToLoad

                fetchResponse
            }

    }

    override fun isCacheEmpty(
        cache: PagedList<GitHubIssueComment>,
        requirements: GetCommentRequirement,
        pagingRequirements: PagingRequirements
    ): Boolean {
        return cache.isEmpty()
    }

    override fun observeCache(
        requirements: GetCommentRequirement,
        pagingRequirements: PagingRequirements
    ): Observable<PagedList<GitHubIssueComment>> {
        return db.gitHubIssueCommentDAO().getAllComments(requirements.issueID)
            .toObservable(PAGE_SIZE, boundaryCallback = PagedListBoundaryCallback())
            .subscribeOn(Schedulers.io())
    }

    override fun saveCache(
        cache: List<GitHubIssueComment>,
        requirements: GetCommentRequirement,
        pagingRequirements: PagingRequirements
    ) {
        for (comments in cache) {
            db.gitHubIssueCommentDAO().insertComment(comments)
        }
    }

    private fun goToNextPage() {
        if (morePagesDataToLoad) {
            val currentPage = pagingRequirements.pageNumber
            pagingRequirements = PagingRequirements(currentPage + 1)
        }
    }

    data class PagingRequirements(val pageNumber: Int = 1) : OnlinePagingRepository.PagingRequirements

    inner class PagedListBoundaryCallback : PagedList.BoundaryCallback<GitHubIssueComment>() {
        override fun onItemAtEndLoaded(itemAtEnd: GitHubIssueComment) {
            this@TellerIssueCommentsOnlineRepository.goToNextPage()
        }
    }

    override var maxAgeOfCache: Age = Age(7, Age.Unit.DAYS)

    class GetCommentRequirement(var issueNumber: Int, var issueName: String, var user: String, var issueID: Int) : GetCacheRequirements {
        override var tag: GetCacheRequirementsTag = "Comments for Issue $issueName #$issueNumber, by $user"
    }


}
