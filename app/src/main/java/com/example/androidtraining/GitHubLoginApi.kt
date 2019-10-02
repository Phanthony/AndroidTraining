package com.example.androidtraining

import io.reactivex.Single
import retrofit2.http.POST
import retrofit2.adapter.rxjava2.Result
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded

interface GitHubLoginApi {

    @POST("/auth/mobile")
    fun loginGithub(@Body body: authmobileRequestBody): Single<Result<GitHubLoginResultSuccess>>
}

data class authmobileRequestBody(val scopes: List<String>, val client_id: String, val password: String, val username: String)