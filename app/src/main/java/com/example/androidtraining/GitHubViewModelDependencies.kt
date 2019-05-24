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

class GitHubViewModelDependencies(application: Application) : AndroidViewModel(application) {

    private val dataBase = GitHubRepoDataBase.getInstance(application)!!
    private var gitHubModel = ReposCompletedDatabase(dataBase)
    private var retrofitService = RetroFitService(Retrofit.Builder()
        .baseUrl("https://api.github.com/")
        .addConverterFactory(MoshiConverterFactory.create())
        .build())
    private var gitHubRepository = com.example.androidtraining.GitHubRepository(dataBase,gitHubModel,retrofitService)

    private var repoList = gitHubRepository.getAllRepos()
    private var minSinceLastRefresh = MutableLiveData<Int>()
    private var errorCode = MutableLiveData<Int>()
    private var timeInformation = com.example.androidtraining.TimeInformation(minSinceLastRefresh)

    private var gitHubViewModelInjected = GitHubViewModelInjected(gitHubRepository)

    init {
        CoroutineScope(Dispatchers.IO).launch{
           gitHubViewModelInjected.initialSetup()
        }

        //Set up time handler
        timeInformation.timeHandler().run()
    }

    fun getRepos() {
        CoroutineScope(Dispatchers.IO).launch {
            val result = gitHubViewModelInjected.getRepos()
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
        timeInformation.resetLastRefresh()
    }
}

class GitHubViewModelInjected(private var gitHubRepository: GitHubRepository){

    suspend fun getRepos(): Int {
        gitHubRepository.deleteAllReposIfNewDay()
        return gitHubRepository.getDailyRepos()
    }

    suspend fun initialSetup(){
        runBlocking {
            //Set up lastday parameter in the repository
            gitHubRepository.getLastDayFromDatabase()
        }
        // Initial pull if there is no data in the database
        if (gitHubRepository.getRepoCount() == 0) {
            getRepos()
        }
    }
}


class TimeInformation(private var minSinceLastRefresh : MutableLiveData<Int>){

    private var lastRefreshed = getTime()

    fun getTime():String {
        return (DateTimeFormatter.ofPattern("k:m:D")
            .withLocale(Locale.US)
            .withZone(ZoneId.systemDefault()))
            .format(Instant.now())
    }

    fun timePassed(initialTime:String, currentTime:String): Int{
        val initialTimeList = initialTime.split(":")
        val currentTimeList = currentTime.split(":")

        val daysPassed = currentTimeList[2].toInt() - initialTimeList[2].toInt()
        val hoursPassed = currentTimeList[0].toInt() - initialTimeList[0].toInt()
        var minutesPassed = 60*hoursPassed + 1440*daysPassed
        minutesPassed += currentTimeList[1].toInt() - initialTimeList[1].toInt()
        return minutesPassed
    }

    fun timeHandler(): java.lang.Runnable{
        val timeHandler = Handler()
        return object : Runnable {
            override fun run(){
                val minutesPassed = timePassed(lastRefreshed, getTime())
                minSinceLastRefresh.value = minutesPassed
                timeHandler.postDelayed(this,60000)
            }
        }
    }

    fun resetLastRefresh(){
        lastRefreshed = getTime()
    }
}
