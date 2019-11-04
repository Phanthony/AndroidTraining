package com.example.androidtraining.service

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.androidtraining.GitHubUser

@Entity(tableName = "Issue_Comment_Table")
data class GitHubIssueComment(@PrimaryKey @ColumnInfo(name = "commentId") val id: Int,
                                  @ColumnInfo(name = "commentUser") val user: GitHubUser,
                                  @ColumnInfo(name = "commentBody") val body: String)



