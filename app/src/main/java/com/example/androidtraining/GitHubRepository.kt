package com.example.androidtraining

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*
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

class GitHubRepository(application: Application) {

    private var lastDay: String? = null
    private var errorCode = MutableLiveData<Int>()

    //set up database
    private val dataBase = GitHubRepoDataBase.getInstance(application)
    //set up retrofit
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.github.com/")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
    private val service = retrofit.create(GitHubApi::class.java)

    // Error code -> 1 = Network error, 2 = Successful, 0 = default state
    fun callRepos() {
        //Check if it's a new day
        //Delete old database entries if so and update the day entry
        CoroutineScope(Dispatchers.IO).launch {
            runBlocking {
            if (lastDay != getYesterday()) {
                    insertYesterdayToDatabase()
                    deleteAllRepos()
                }
            }
            //do the github call
            val result = service.getRepo("created:%3E${getYesterday()}+language:kotlin+stars:%3E0")
            result.clone().enqueue(object : Callback<GitHubRepoList> {
                override fun onFailure(call: Call<GitHubRepoList>, t: Throwable) {
                    Log.e("Network Error", "", t)
                    errorCode.value = 1
                }

                override fun onResponse(call: Call<GitHubRepoList>, response: Response<GitHubRepoList>) {
                    if (response.body() != null) {
                        val test = response.body()!!
                        for (i in test.items) {
                            Log.i(
                                "GitHub Repo",
                                "${i.getName()} by ${i.getOwner().login}. It has gotten ${i.getStargazers_count()} stars recently."
                            )
                            insertToDatabase(i)
                        }
                        errorCode.value = 2
                    }
                }
            })
        }
    }

    private fun insertToDatabase(repo: GitHubRepo) {
        CoroutineScope(Dispatchers.IO).launch{
            dataBase?.gitHubRepoDAO()?.insert(repo)
            }
        }

    fun getAllRepos(): LiveData<List<GitHubRepo>>?{
        return dataBase?.gitHubRepoDAO()?.getAllRepos()
    }

    private suspend fun deleteAllRepos(){
        dataBase?.gitHubRepoDAO()?.deleteAllRepos()
    }

    private fun getYesterday():String{
        return (DateTimeFormatter.ofPattern("yyyy-MM-dd")
            .withLocale(Locale.US)
            .withZone(ZoneId.systemDefault()))
            .format(Instant.now().minus(Duration.ofDays(1)))
    }

    suspend fun getLastDayFromDatabase(){
        lastDay = dataBase?.dayDAO()?.getDay()
    }

    private suspend fun insertYesterdayToDatabase(){
        dataBase!!.dayDAO().insertDay(DayEntry(getYesterday(),1))
        lastDay = dataBase.dayDAO().getDay()
    }

    fun getErrorCode(): LiveData<Int>{
        return errorCode
    }

    fun resetErrorCode(){
        errorCode.value = 0
    }

    suspend fun getRepoCount(): Int{
        return dataBase?.gitHubRepoDAO()!!.getRepoCount()
    }

    fun resetLastRefresh(){
    }

}