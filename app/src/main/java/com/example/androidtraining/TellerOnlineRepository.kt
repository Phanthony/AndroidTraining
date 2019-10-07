package com.example.androidtraining

import com.example.androidtraining.service.error.UserEnteredBadDataResponseError
import com.levibostian.teller.repository.GetCacheRequirementsTag
import com.levibostian.teller.repository.OnlineRepository
import com.levibostian.teller.type.Age
import io.reactivex.Observable
import io.reactivex.Single


class TellerOnlineRepository(private val db: GitHubRepoDataBase, private val service: RepoService, private val responseProcessor: ResponseProcessor): OnlineRepository<List<GitHubRepo>, TellerOnlineRepository.GetReposRequirement, GitHubRepoList>() {

    class GetReposRequirement(val dayInformation: DayInformation): GetCacheRequirements{
        override var tag: GetCacheRequirementsTag = "Trending Kotlin repos"
    }

    override var maxAgeOfCache = Age(1, Age.Unit.DAYS)

    override fun fetchFreshCache(requirements: GetReposRequirement): Single<FetchResponse<GitHubRepoList>> {
        return service.getRepos(requirements.dayInformation.getYesterday())
            .map { result ->
                val processedResponse = responseProcessor.process(result) { code, response, errorBody, jsonAdapter ->
                    when (code) {
                        400 -> jsonAdapter.fromJson(errorBody, UserEnteredBadDataResponseError::class.java)
                        else -> null
                    }
                }

                val fetchResponse: FetchResponse<GitHubRepoList> = if (processedResponse.isFailure()) {
                    FetchResponse.fail(processedResponse.error!!)
                } else {
                    FetchResponse.success(processedResponse.body!!)
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

class JsonError: Throwable("Can not compute JSON request")

class UnhandledError: Throwable()

class NetworkError: Throwable("There is a problem with your network connection")
