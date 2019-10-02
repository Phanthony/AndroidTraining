package com.example.androidtraining

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.androidtraining.service.vo.response.MessageResponse

@Entity(tableName = "Repo_Table")
data class GitHubRepo(@ColumnInfo(name = "repoTitle") private var name: String,
                      @Embedded(prefix = "repo") private var owner: GitHubRepoOwner,
                      @ColumnInfo(name = "repoStarCount") private var stargazers_count: Int,
                      @ColumnInfo(name = "repoDescription") private var description: String?,
                      @PrimaryKey @ColumnInfo(name = "repoId") private var id: Int){
    fun getId() = id
    fun getStargazers_count() = stargazers_count
    fun getDescription() = description
    fun getName() = name
    fun getOwner() = owner
}


data class GitHubRepoList(
    override val message: String = "Here are the top trending Kotlin repositories.",
    var items: List<GitHubRepo>): MessageResponse


data class GitHubRepoOwner(var login: String, var avatar_url: String)