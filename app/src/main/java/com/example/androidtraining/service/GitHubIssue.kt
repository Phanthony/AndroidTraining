package com.example.androidtraining.service

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.androidtraining.database.GitHubRepo
import com.example.androidtraining.database.GitHubUser

@Entity(tableName = "Issue_Table")
data class GitHubIssue(
    @PrimaryKey @ColumnInfo(name = "issueId") val id: Int,
    @ColumnInfo(name = "issueNumber") val number: Int,
    @ColumnInfo(name = "issueState") val state: String,
    @ColumnInfo(name = "issueTitle") val title: String,
    @Embedded(prefix = "issueCreated") val user: GitHubUser,
    @ColumnInfo(name = "issueComments") val comments: Int,
    @ColumnInfo(name = "issueLastUpdate") val updated_at: String,
    @Embedded(prefix = "issueFor") val repository: GitHubRepo
)