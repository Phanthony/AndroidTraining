package com.example.androidtraining.database.teller

import com.example.androidtraining.Day
import com.example.androidtraining.database.GitHubDataBase
import com.example.androidtraining.database.GitHubRepo
import com.example.androidtraining.database.GitHubRepoList
import com.example.androidtraining.service.Service
import com.levibostian.teller.repository.GetCacheRequirementsTag
import com.levibostian.teller.repository.OnlineRepository
import com.levibostian.teller.type.Age
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TellerRepoOnlineRepository @Inject constructor(private val db: GitHubDataBase, private val service: Service): OnlineRepository<List<GitHubRepo>, TellerRepoOnlineRepository.GetReposRequirement, GitHubRepoList>() {

    class GetReposRequirement(val dayInformation: Day): GetCacheRequirements{
        override var tag: GetCacheRequirementsTag = "Trending Kotlin repos"
    }

    override var maxAgeOfCache = Age(1, Age.Unit.DAYS)

    override fun fetchFreshCache(requirements: GetReposRequirement): Single<FetchResponse<GitHubRepoList>> {
        return service.getRepos(requirements.dayInformation.getYesterday())
            .map { result ->
                val fetchResponse: FetchResponse<GitHubRepoList> = if (result.isFailure) {
                    FetchResponse.fail(result.exceptionOrNull()!!)
                } else {
                    FetchResponse.success(result.getOrNull()!!)
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

    public override fun saveCache(cache: GitHubRepoList, requirements: GetReposRequirement) {
        for (repos in cache.items){
            db.gitHubRepoDAO().insertRepo(repos)
        }
    }

}
