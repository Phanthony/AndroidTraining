package com.example.androidtraining

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.androidtraining.service.GitHubIssueComment
import io.reactivex.Completable
import io.reactivex.Observable

@Dao
interface GitHubIssueCommentDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertComment(githubIssueComment: GitHubIssueComment)

    @Query("DELETE FROM Issue_Comment_Table")
    fun deleteAllComments(): Completable

    @Query("SELECT * FROM Issue_Table")
    fun getAllComments(): Observable<List<GitHubIssueComment>>
}