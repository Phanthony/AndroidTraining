package com.example.androidtraining

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface GitHubApi {

    @GET("repositories?language=kotlin&since=daily")
    fun getRepo(): Call<List<GitHubRepo>>

}