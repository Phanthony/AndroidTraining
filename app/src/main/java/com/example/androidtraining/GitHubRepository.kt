package com.example.androidtraining

import android.util.Log
import androidx.lifecycle.MutableLiveData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class GitHubRepository {

    //set up retrofit
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.github.com/")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
    private val service = retrofit.create(GitHubApi::class.java)
    private val result = service.getRepo("created:%3E${getYesterday()}+language:kotlin+stars:%3E0")

    // Error code -> 1 = Network error, 2 = Successful, 0 = default state
    fun callRepos(liveList: MutableLiveData<ArrayList<GitHubRepo>>, errorCode: MutableLiveData<Int>){
        //do the github call
        liveList.value?.clear()
        result.clone().enqueue(object : Callback<GitHubRepoList> {
            override fun onFailure(call: Call<GitHubRepoList>, t: Throwable) {
                Log.e("Network Error","",t)
                errorCode.value = 1
            }
            override fun onResponse(call: Call<GitHubRepoList>, response: Response<GitHubRepoList>) {
                if(response.body() != null){
                    val test = response.body()!!
                    for (i in test.items) {
                        Log.i("GitHub Repo", "${i.name} by ${i.owner.login}. It has gotten ${i.stargazers_count} stars recently.")
                    }
                    liveList.value = ArrayList(test.items)
                    errorCode.value = 2
                }
            }
        })
    }

    fun getYesterday():String{
        return (DateTimeFormatter.ofPattern("yyyy-MM-dd")
            .withLocale(Locale.US)
            .withZone(ZoneId.systemDefault()))
            .format(Instant.now().minus(Duration.ofDays(1)))
    }
}