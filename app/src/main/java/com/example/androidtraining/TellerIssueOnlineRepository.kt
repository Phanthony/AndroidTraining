package com.example.androidtraining

import com.example.androidtraining.service.GitHubIssue
import com.levibostian.teller.repository.GetCacheRequirementsTag
import com.levibostian.teller.repository.OnlineRepository
import com.levibostian.teller.type.Age
import io.reactivex.Observable
import io.reactivex.Single

class TellerIssueOnlineRepository(): OnlineRepository<List<GitHubIssue>, TellerIssueOnlineRepository.GetIssuesRequirement, List<GitHubIssue>>() {

    class GetIssuesRequirement: GetCacheRequirements{
        override var tag: GetCacheRequirementsTag = "All Issues"
    }

    override var maxAgeOfCache = Age(7, Age.Unit.DAYS)

    override fun fetchFreshCache(requirements: GetIssuesRequirement): Single<FetchResponse<List<GitHubIssue>>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isCacheEmpty(cache: List<GitHubIssue>, requirements: GetIssuesRequirement): Boolean {
        return cache.isEmpty()
    }

    override fun observeCache(requirements: GetIssuesRequirement): Observable<List<GitHubIssue>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun saveCache(cache: List<GitHubIssue>, requirements: GetIssuesRequirement) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }



}