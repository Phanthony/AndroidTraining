package com.example.androidtraining

import io.reactivex.Single
import retrofit2.adapter.rxjava2.Result
import retrofit2.http.Body
import retrofit2.http.POST

interface DevApi {

    @POST("/auth/mobile")
    fun loginGithub(@Body body: AuthMobileRequestBody): Single<Result<GitHubLoginResultSuccess>>
}

data class AuthMobileRequestBody(
    val scopes: List<String>,
    val client_id: String,
    val password: String,
    val username: String
)

