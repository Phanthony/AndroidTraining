package com.example.androidtraining.service

import com.example.androidtraining.database.GitHubRepoList
import com.example.androidtraining.database.teller.ResultPaging
import io.reactivex.Single

interface Service {
    fun getRepos(day: String): Single<Result<GitHubRepoList>>

    fun loginToGithub(password: String, username: String): Single<Result<GitHubLoginResult>>

    fun getIssues(): Single<Result<List<GitHubIssue>>>

    fun getIssueComments(
        issueNum: Int,
        issueName: String,
        user: String,
        issueId: Int
    ): Single<ResultPaging<List<GitHubIssueComment>>>
}