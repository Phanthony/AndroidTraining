package com.example.androidtraining

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface GitHubApi {

    @GET("search/repositories?q=stars:>0&language:kotlin&sort=stars")
    fun getRepo(): Call<GitHubRepoList>

}