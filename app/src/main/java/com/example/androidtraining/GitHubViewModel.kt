package com.example.androidtraining

import android.content.Context
import android.content.DialogInterface
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.android.synthetic.main.activity_main.*
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

class GitHubViewModel : ViewModel() {

    private var repoList = MutableLiveData<ArrayList<GitHubRepo>>()
    private var lastRefreshed = getTime()
    private var minSinceLastRefresh = MutableLiveData<Int>()
    private val gitHubRepository = com.example.androidtraining.GitHubRepository()
    private var networkError = MutableLiveData<Int>()

    init {
        repoList.value = arrayListOf()
        networkError.value = 0
        getRepos()

        //set up handler for constant time updates
        val timeHandler = Handler()
        val timeRunnable = object : Runnable {
            override fun run(){
                val minutesPassed = timePassed(lastRefreshed,getTime())
                minSinceLastRefresh.value = minutesPassed
                timeHandler.postDelayed(this,60000)
            }
        }
            .run()
    }

    fun getRepos(){
        gitHubRepository.callRepos(repoList, networkError)
        networkError.value = 0
    }

    fun getNetworkError(): LiveData<Int>{
        return networkError
    }

    fun getRepoList(): LiveData<ArrayList<GitHubRepo>>{
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
        var minutesPassed = 0

        val hoursPassed = currentTimeList[0].toInt() - initialTimeList[0].toInt()
        minutesPassed += 60*hoursPassed
        minutesPassed += currentTimeList[1].toInt() - initialTimeList[1].toInt()
        return minutesPassed
    }


}