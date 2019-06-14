package com.example.androidtraining

import com.levibostian.teller.repository.GetCacheRequirementsTag
import com.levibostian.teller.repository.OnlineRepository
import com.levibostian.teller.type.Age
import io.reactivex.Observable
import io.reactivex.Single


class TellerOnlineRepository(private val db: GitHubRepoDataBase, private val service: Service): OnlineRepository<List<GitHubRepo>, TellerOnlineRepository.GetReposRequirement, GitHubRepoList>() {
    class GetReposRequirement(val day: String): OnlineRepository.GetCacheRequirements{
        override var tag: GetCacheRequirementsTag = "Trending Kotlin repos for: $day"
    }

    override var maxAgeOfCache = Age(9999, Age.Unit.DAYS)

    override fun fetchFreshCache(requirements: GetReposRequirement): Single<FetchResponse<GitHubRepoList>> {
        return service.getRepos(requirements.day)
            .map {response -> val fetchResponse: FetchResponse<GitHubRepoList> =
                if (response.isError){
                    FetchResponse.fail(response.error()!!)
                }
                else{
                    FetchResponse.success(response.response()!!.body()!!)
                }
                fetchResponse
            }
    }

    override fun isCacheEmpty(cache: List<GitHubRepo>, requirements: GetReposRequirement): Boolean {
        return cache.isEmpty()
    }

    override fun observeCache(requirements: GetReposRequirement): Observable<List<GitHubRepo>> {
        return db.gitHubRepoDAO().getAllRepos()
    }

    override fun saveCache(cache: GitHubRepoList, requirements: GetReposRequirement) {
        for (repos in cache.items){
            db.gitHubRepoDAO().insert(repos)
        }
    }


}