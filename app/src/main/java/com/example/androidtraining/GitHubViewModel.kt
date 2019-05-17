package com.example.androidtraining

import android.app.Application
import android.os.Handler
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class GitHubViewModel(application: Application) : AndroidViewModel(application) {

    private var lastRefreshed = getTime()
    private var minSinceLastRefresh = MutableLiveData<Int>()
    private val gitHubRepository = com.example.androidtraining.GitHubRepository(application)
    private var repoList = gitHubRepository.getAllRepos()
    private var networkError = gitHubRepository.getErrorCode()

    init {
        CoroutineScope(Dispatchers.IO).launch{
            runBlocking {
                gitHubRepository.getLastDayFromDatabase()
            }
            if (gitHubRepository.getRepoCount() == 0) getRepos()
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

    }



    fun getRepos(){
        gitHubRepository.callRepos()
    }

    fun getNetworkError(): LiveData<Int>{
        return networkError
    }

    fun getRepoList(): LiveData<List<GitHubRepo>>?{
        return repoList
    }

    fun getMinSinceLastRefresh(): LiveData<Int>{
        return minSinceLastRefresh
    }

    fun getTime():String {
        return (DateTimeFormatter.ofPattern("k:m")
            .withLocale(Locale.US)
            .withZone(ZoneId.systemDefault()))
            .format(Instant.now())
    }

    fun timePassed(initialTime:String, currentTime:String): Int{
        val initialTimeList = initialTime.split(":")
        val currentTimeList = currentTime.split(":")

        val hoursPassed = currentTimeList[0].toInt() - initialTimeList[0].toInt()
        var minutesPassed = 60*hoursPassed
        minutesPassed += currentTimeList[1].toInt() - initialTimeList[1].toInt()
        return minutesPassed
    }

    fun resetNetworkError(){
        gitHubRepository.resetErrorCode()
    }

    fun resetLastRefresh(){
        lastRefreshed = getTime()
    }


}