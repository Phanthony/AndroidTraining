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

    suspend fun deleteAllReposIfNewDay() {
        checkYesterday(getYesterday())
    }

    suspend fun checkYesterday(day: String) {
        if (!lastDay.equals(day)) {
            insertYesterdayToDatabase()
            deleteAllRepos()
        }
    }

    //Error Code -> 1 = Unsuccessful, 2 = Successful, 0 = Default state
    suspend fun getDailyRepos(): Int {
        val result = service.getRepos(getYesterday())
        if (result != null) {
            RepoModel.saveRepos(result)
            return 2
        } else {
            return 1
        }
    }

    private fun getYesterday(): String {
        return (DateTimeFormatter.ofPattern("yyyy-MM-dd")
            .withLocale(Locale.US)
            .withZone(ZoneId.systemDefault()))
            .format(Instant.now().minus(Duration.ofDays(1)))
    }

    // All Database Functions
    private suspend fun deleteAllRepos() {
        db.gitHubRepoDAO().deleteAllRepos()
    }

    private suspend fun insertYesterdayToDatabase() {
        db.dayDAO().insertDay(DayEntry(getYesterday(), 1))
        lastDay = db.dayDAO().getDay()
    }

    //These functions are accessed from ViewModel Layer
    suspend fun getLastDayFromDatabase() {
        lastDay = db.dayDAO().getDay()
    }

    fun getAllRepos(): LiveData<List<GitHubRepo>>? {
        return db.gitHubRepoDAO().getAllRepos()
    }

    suspend fun getRepoCount(): Int {
        return db.gitHubRepoDAO().getRepoCount()
    }

}

class ReposCompletedDatabase(private var db: GitHubRepoDataBase) : ReposCompleted {
    override suspend fun saveRepos(gitHubRepoList: GitHubRepoList) {
        val githubRepoDAO = db.gitHubRepoDAO()
        for (repo in gitHubRepoList.items) {
            githubRepoDAO.insert(repo)
        }
    }

}

class RetroFitService(private var service: GitHubApi) : Service {

    override suspend fun getRepos(day: String): GitHubRepoList? {
        var result: GitHubRepoList? = null
        try {
            val response = service.getRepo("created:%3E$day+language:kotlin+stars:%3E0").execute()
            if (response.isSuccessful) {
                result = response.body()
            } else {
                Log.e("Network Error", response.errorBody().toString())
            }
        } catch (exception: IOException) {
            Log.e("Network Error", "Could not connect to the server")
        }

        return result
    }
}

interface Service {
    suspend fun getRepos(day: String): GitHubRepoList?
}

interface ReposCompleted {
    suspend fun saveRepos(gitHubRepoList: GitHubRepoList)
}