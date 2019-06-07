package com.example.androidtraining

import androidx.lifecycle.LiveData
import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface GitHubRepoDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(gitHubRepo: GitHubRepo)

    @Query("DELETE FROM Repo_Table")
    fun deleteAllRepos(): Completable

    @Query("SELECT * FROM Repo_Table ORDER BY repoStarCount DESC")
    fun getAllRepos(): LiveData<List<GitHubRepo>>

    @Query("SELECT COUNT(*) FROM Repo_Table")
    fun getRepoCount(): Single<Int>



}