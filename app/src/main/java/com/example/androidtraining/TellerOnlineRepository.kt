package com.example.androidtraining

import com.levibostian.teller.repository.GetCacheRequirementsTag
import com.levibostian.teller.repository.OnlineRepository
import com.levibostian.teller.type.Age
import io.reactivex.Observable
import io.reactivex.Single
import java.io.IOException


class TellerOnlineRepository(private val db: GitHubRepoDataBase, private val service: Service): OnlineRepository<List<GitHubRepo>, TellerOnlineRepository.GetReposRequirement, GitHubRepoList>() {
    class GetReposRequirement(val day: String): OnlineRepository.GetCacheRequirements{
        override var tag: GetCacheRequirementsTag = "Trending Kotlin repos for: $day"
    }

    override var maxAgeOfCache = Age(1, Age.Unit.DAYS)

    override fun fetchFreshCache(requirements: GetReposRequirement): Single<FetchResponse<GitHubRepoList>> {
        return service.getRepos(requirements.day)
            .map {response -> val fetchResponse: FetchResponse<GitHubRepoList> =
                if (response.isError){
                    val errorResult = when(response.error()!!){
                        is IOException -> {FetchResponse.fail<GitHubRepoList>(NetworkError())}
                        else -> {FetchResponse.fail(UnhandledError())}
                    }
                    errorResult
                }
                else{
                    val result = when(response.response()!!.code()){
                        200 -> {FetchResponse.success(response.response()!!.body()!!)}
                        in 400..422 -> {FetchResponse.fail(JsonError())}
                        else -> {FetchResponse.fail(UnhandledError())}
                    }
                    result
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
