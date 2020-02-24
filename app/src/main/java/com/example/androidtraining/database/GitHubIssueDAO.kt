package com.example.androidtraining.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.androidtraining.service.GitHubIssue
import io.reactivex.Completable
import io.reactivex.Observable

@Dao
interface GitHubIssueDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertIssue(gitHubIssue: GitHubIssue)

    @Query("DELETE FROM Issue_Table")
    fun deleteAllIssues(): Completable

    @Query("SELECT * FROM Issue_Table")
    fun getAllIssues(): Observable<List<GitHubIssue>>

}