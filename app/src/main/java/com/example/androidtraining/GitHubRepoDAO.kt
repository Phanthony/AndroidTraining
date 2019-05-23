package com.example.androidtraining

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface GitHubRepoDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(gitHubRepo: GitHubRepo)

    @Query("DELETE FROM Repo_Table")
    suspend fun deleteAllRepos()

    @Query("SELECT * FROM Repo_Table ORDER BY repoStarCount DESC")
    fun getAllRepos(): LiveData<List<GitHubRepo>>

    @Query("SELECT COUNT(*) FROM Repo_Table")
    suspend fun getRepoCount(): Int



}