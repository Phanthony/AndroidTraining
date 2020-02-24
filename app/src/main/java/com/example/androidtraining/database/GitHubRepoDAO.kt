package com.example.androidtraining.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

@Dao
interface GitHubRepoDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRepo(gitHubRepo: GitHubRepo)

    @Query("DELETE FROM Repo_Table")
    fun deleteAllRepos(): Completable

    @Query("SELECT * FROM Repo_Table ORDER BY repoStarCount DESC")
    fun getAllRepos(): Observable<List<GitHubRepo>>

    @Query("SELECT COUNT(*) FROM Repo_Table")
    fun getRepoCount(): Single<Int>

}