package com.example.androidtraining

import android.util.Log
import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import java.io.IOException
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class GitHubRepository(var db: GitHubRepoDataBase, var RepoModel: ReposCompleted, var service: Service) {

    private var lastDay: String? = null

    suspend fun deleteAllReposIfNewDay(){
        if (lastDay != getYesterday()){
            insertYesterdayToDatabase()
            deleteAllRepos()
        }
    }

    //Error Code -> 1 = Unsuccessful, 2 = Successful, 0 = Default state
    suspend fun getDailyRepos(): Int{
        val result = service.getRepos(getYesterday())
        if (result != null){
            RepoModel.saveRepos(result)
            return 2
        }
        else{
            return 1
        }
    }

    private fun getYesterday():String{
        return (DateTimeFormatter.ofPattern("yyyy-MM-dd")
            .withLocale(Locale.US)
            .withZone(ZoneId.systemDefault()))
            .format(Instant.now().minus(Duration.ofDays(1)))
    }

    // All Database Functions
    private suspend fun deleteAllRepos(){
        db.gitHubRepoDAO().deleteAllRepos()
    }

    private suspend fun insertYesterdayToDatabase(){
        db.dayDAO().insertDay(DayEntry(getYesterday(),1))
        lastDay = db.dayDAO().getDay()
    }

    //These functions are accessed from ViewModel Layer
    suspend fun getLastDayFromDatabase(){
        lastDay = db.dayDAO().getDay()
    }

    fun getAllRepos(): LiveData<List<GitHubRepo>>?{
        return db.gitHubRepoDAO().getAllRepos()
    }

    suspend fun getRepoCount(): Int{
        return db.gitHubRepoDAO().getRepoCount()
    }

}

class ReposCompletedDatabase(var db: GitHubRepoDataBase): ReposCompleted{
    override fun saveRepos(list: List<GitHubRepo>) {
        CoroutineScope(Dispatchers.IO).launch {
            val githubRepoDAO = db.gitHubRepoDAO()
            for (repo in list){
                githubRepoDAO.insert(repo)
            }
        }
    }

}

class RetroFitService(retrofit: Retrofit): Service {

    private var service = retrofit.create(GitHubApi::class.java)

    override suspend fun getRepos(day: String): List<GitHubRepo>? {
        var result: List<GitHubRepo>? = null
        try {
            val response = service.getRepo("created:%3E$day+language:kotlin+stars:%3E0").execute()
            if(response.isSuccessful){
                result = response.body()!!.items
            }
            else{
                Log.e("Network Error",response.message())
            }
        }
        catch(exception: IOException) {
            Log.e("Network Error","Could not connect to the server")
        }

        return result
    }
}

interface Service{
    suspend fun getRepos(day: String):List<GitHubRepo>?
}

interface ReposCompleted{
    fun saveRepos(list: List<GitHubRepo>)
}