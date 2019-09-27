package com.example.androidtraining

import io.reactivex.Single
import retrofit2.http.POST
import retrofit2.adapter.rxjava2.Result
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded

interface GitHubLoginApi {

    @FormUrlEncoded
    @POST("/auth/mobile?scope=[repo]&client_id=4cc5aa575096c8bcb036")
    fun loginGithub(@Field("password") password: String, @Field("username") username: String): Single<Result<GitHubLoginResultSuccess>>
}