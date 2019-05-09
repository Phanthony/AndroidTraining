package com.example.androidtraining

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

//https://api.github.com/search/repositories?q=created:%3E2019-05-03+language:kotlin&sort=stars&order=desc
interface GitHubApi {

    @GET("/search/repositories?q=created:>{date}+language:kotlin+stars:>0&sort=stars&order=desc")
    fun getRepo(@Path("date") date: String): Call<GitHubRepoList>

}