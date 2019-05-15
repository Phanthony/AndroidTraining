package com.example.androidtraining

import android.os.Parcel
import android.os.Parcelable
import androidx.room.*
import org.jetbrains.annotations.Nullable

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


data class GitHubRepoList(var items: List<GitHubRepo>)


data class GitHubRepoOwner(var login: String)