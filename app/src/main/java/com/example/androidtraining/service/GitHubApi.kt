package com.example.androidtraining.service

import com.example.androidtraining.database.GitHubRepoList
import io.reactivex.Single
import retrofit2.adapter.rxjava2.Result
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

//https://api.github.com/search/repositories?q=created:%3E2019-05-03+language:kotlin&sort=stars&order=desc
interface GitHubApi {

    @GET("/search/repositories?sort=stars")
    fun getRepo(@Query("q", encoded = true) q: String): Single<Result<GitHubRepoList>>

    @GET("/user/issues?filter=all&state=all&direction=asc")
    fun getIssues(): Single<Result<List<GitHubIssue>>>

    @GET("/repos/{user}/{issue}/issues/{number}/comments")
    fun getIssueComment(@Path("number") number: String, @Path("user") user: String, @Path("issue") issue: String): Single<Result<List<GitHubIssueComment>>>

}
