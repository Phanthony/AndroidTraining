package com.example.androidtraining.database

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(tableName = "Repo_Table")
data class GitHubRepo(
    @ColumnInfo(name = "repoTitle") val name: String,
    @Embedded(prefix = "repo") val owner: GitHubUser,
    @ColumnInfo(name = "repoStarCount") val stargazers_count: Int,
    @ColumnInfo(name = "repoDescription") val description: String?,
    @PrimaryKey @ColumnInfo(name = "repoId") val id: Int
)

@JsonClass(generateAdapter = true)
data class GitHubRepoList(var items: List<GitHubRepo>)

@JsonClass(generateAdapter = true)
data class GitHubUser(var login: String, var avatar_url: String)