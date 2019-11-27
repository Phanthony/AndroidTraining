package com.example.androidtraining

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.androidtraining.service.GitHubIssueComment
import io.reactivex.Completable

@Dao
interface GitHubIssueCommentDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertComment(githubIssueComment: GitHubIssueComment)

    @Query("DELETE FROM Issue_Comment_Table WHERE commentId = :issueId")
    fun deleteComment(issueId: Int): Completable

    @Query("SELECT * FROM Issue_Comment_Table WHERE issueId LIKE :x")
    fun getAllComments(x: Int): DataSource.Factory<Int,GitHubIssueComment>

    @Query("SELECT * FROM Issue_Comment_Table WHERE issueId LIKE :x")
    fun getComment(x: Int): List<GitHubIssueComment>
}