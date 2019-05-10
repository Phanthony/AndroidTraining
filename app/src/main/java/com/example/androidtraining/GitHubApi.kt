package com.example.androidtraining

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

//https://api.github.com/search/repositories?q=created:%3E2019-05-03+language:kotlin&sort=stars&order=desc
interface GitHubApi {

    @GET("/search/repositories?sort=stars")
    fun getRepo(@Query("q", encoded = true) q: String): Call<GitHubRepoList>

}