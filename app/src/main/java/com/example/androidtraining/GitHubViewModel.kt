package com.example.androidtraining

import android.app.Application
import android.os.Handler
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class GitHubViewModel(application: Application) : AndroidViewModel(application) {

    private var lastRefreshed = getTime()
    private var minSinceLastRefresh = MutableLiveData<Int>()
    private val dataBase = GitHubRepoDataBase.getInstance(application)!!
    private var gitHubModel = ReposCompletedDatabase(dataBase)
    private var retrofitService = RetroFitService(Retrofit.Builder()
        .baseUrl("https://api.github.com/")
        .addConverterFactory(MoshiConverterFactory.create())
        .build())
    private var gitHubRepository = com.example.androidtraining.GitHubRepository(dataBase,gitHubModel,retrofitService)
    private var repoList = gitHubRepository.getAllRepos()
    private var errorCode = MutableLiveData<Int>()

    init {
        CoroutineScope(Dispatchers.IO).launch{
            runBlocking {
                //Set up lastday parameter in the repository
                gitHubRepository.getLastDayFromDatabase()
            }
            // Initial draw if there is no data in the database
            if (gitHubRepository.getRepoCount() == 0) {
                getRepos()
            }
        }

        //set up handler for constant time updates
        val timeHandler = Handler()
        val timeRunnable = object : Runnable {
            override fun run(){
                val minutesPassed = timePassed(lastRefreshed,getTime())
                minSinceLastRefresh.value = minutesPassed
                timeHandler.postDelayed(this,60000)
            }
        }
        timeRunnable.run()

        //Set up retrofit service
        retrofitService = RetroFitService(Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build())
    }

    fun getRepos() {
        CoroutineScope(Dispatchers.IO).launch {
            gitHubRepository.deleteAllReposIfNewDay()
            val result = gitHubRepository.getDailyRepos()
            withContext(Dispatchers.Main) {
                errorCode.value = result
            }
        }
    }

    fun getNetworkError(): LiveData<Int>{
        return errorCode
    }

    fun getRepoList(): LiveData<List<GitHubRepo>>?{
        return repoList
    }

    fun getMinSinceLastRefresh(): LiveData<Int>{
        return minSinceLastRefresh
    }

    fun resetNetworkError(){
        errorCode.value = 0
    }

    fun resetLastRefresh(){
        lastRefreshed = getTime()
    }

    fun getTime():String {
        return (DateTimeFormatter.ofPattern("k:m:D")
            .withLocale(Locale.US)
            .withZone(ZoneId.systemDefault()))
            .format(Instant.now())
    }

    fun timePassed(initialTime:String, currentTime:String): Int{
        val initialTimeList = initialTime.split(":")
        val currentTimeList = currentTime.split(":")

        val daysPassed = currentTime[3].toInt() - initialTime[3].toInt()
        val hoursPassed = currentTimeList[0].toInt() - initialTimeList[0].toInt()
        var minutesPassed = 60*hoursPassed + 1440*daysPassed
        minutesPassed += currentTimeList[1].toInt() - initialTimeList[1].toInt()
        return minutesPassed
    }

}