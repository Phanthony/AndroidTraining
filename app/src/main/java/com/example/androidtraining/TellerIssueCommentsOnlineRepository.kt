package com.example.androidtraining

import com.example.androidtraining.service.GitHubIssueComment
import com.levibostian.teller.repository.GetCacheRequirementsTag
import com.levibostian.teller.repository.OnlineRepository
import com.levibostian.teller.type.Age
import io.reactivex.Observable
import io.reactivex.Single

class TellerIssueCommentsOnlineRepository(private val db: GitHubDataBase,private val service: Service): OnlineRepository<List<GitHubIssueComment>, TellerIssueCommentsOnlineRepository.GetCommentRequirement, List<GitHubIssueComment>>() {
    override var maxAgeOfCache: Age = Age(7, Age.Unit.DAYS)

    override fun fetchFreshCache(requirements: GetCommentRequirement): Single<FetchResponse<List<GitHubIssueComment>>> {
        return service.getIssueComments(requirements.urlHolder.url)
            .map { result ->
                val fetchResponse: FetchResponse<List<GitHubIssueComment>> = if (result.isFailure){
                    FetchResponse.fail(result.exceptionOrNull()!!)
                }
                else{
                    FetchResponse.success(result.getOrNull()!!)
                }

                fetchResponse
            }
    }

    override fun isCacheEmpty(cache: List<GitHubIssueComment>, requirements: GetCommentRequirement): Boolean {
        return cache.isEmpty()
    }

    override fun observeCache(requirements: GetCommentRequirement): Observable<List<GitHubIssueComment>> {
        return db.gitHubIssueCommentDAO().getAllComments()
    }

    override fun saveCache(cache: List<GitHubIssueComment>, requirements: GetCommentRequirement) {
        for(comments in cache){
            db.gitHubIssueCommentDAO().insertComment(comments)
        }
    }

    class GetCommentRequirement(var urlHolder: TellerIssueCommentsUrlHolder): GetCacheRequirements{
        override var tag: GetCacheRequirementsTag = "Issue Comments"
    }

}
