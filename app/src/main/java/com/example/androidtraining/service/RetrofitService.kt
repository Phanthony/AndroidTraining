package com.example.androidtraining.service

import com.example.androidtraining.database.GitHubRepoList
import com.example.androidtraining.database.teller.ResultPaging
import com.example.androidtraining.service.error.UserEnteredBadDataResponseError
import io.reactivex.Single
import javax.inject.Inject

class RetrofitService @Inject constructor(
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

            ResultPaging(
                moreData,
                kotlinResult
            )
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