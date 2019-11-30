package com.example.androidtraining

import com.example.androidtraining.service.GitHubIssue
import com.levibostian.teller.repository.GetCacheRequirementsTag
import com.levibostian.teller.repository.OnlineRepository
import com.levibostian.teller.type.Age
import io.reactivex.Observable
import io.reactivex.Single

class TellerIssueOnlineRepository(private val db: GitHubDataBase, private val service: Service): OnlineRepository<List<GitHubIssue>, TellerIssueOnlineRepository.GetIssuesRequirement, List<GitHubIssue>>() {

    class GetIssuesRequirement(var user: String): GetCacheRequirements{
        override var tag: GetCacheRequirementsTag = "All Issues for user $user"
    }

    override var maxAgeOfCache = Age(1, Age.Unit.DAYS)

    override fun fetchFreshCache(requirements: GetIssuesRequirement): Single<FetchResponse<List<GitHubIssue>>> {
        return service.getIssues()
            .map { result ->
                val fetchResponse: FetchResponse<List<GitHubIssue>> = if (result.isFailure){
                    FetchResponse.fail(result.exceptionOrNull()!!)
                }
                else{
                    FetchResponse.success(result.getOrNull()!!)
                }

                fetchResponse
        }
    }

    override fun isCacheEmpty(cache: List<GitHubIssue>, requirements: GetIssuesRequirement): Boolean {
        return cache.isEmpty()
    }

    override fun observeCache(requirements: GetIssuesRequirement): Observable<List<GitHubIssue>> {
        return db.gitHubIssueDAO().getAllIssues()
    }

    override fun saveCache(cache: List<GitHubIssue>, requirements: GetIssuesRequirement) {
        for (issues in cache){
            db.gitHubIssueDAO().insertIssue(issues)
        }
    }



}