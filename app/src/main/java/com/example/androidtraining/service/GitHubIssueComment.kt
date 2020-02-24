package com.example.androidtraining.service

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.androidtraining.database.GitHubUser

@Entity(tableName = "Issue_Comment_Table")
data class GitHubIssueComment(@PrimaryKey @ColumnInfo(name = "commentId") val id: Int,
                              @Embedded(prefix = "comment") val user: GitHubUser,
                              @ColumnInfo(name = "commentBody") val body: String,
                              var issueId: Int?)



