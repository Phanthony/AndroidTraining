package com.example.androidtraining.typeconverters

import com.example.androidtraining.service.GitHubIssue
import com.squareup.moshi.FromJson
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import com.squareup.moshi.Types

class GitHubIssueListTypeConverter {
    val moshi = Moshi.Builder().build()
    val type = Types.newParameterizedType(List::class.java, GitHubIssue::class.java)
    val adapter = moshi.adapter<List<GitHubIssue>>(type)

    @ToJson
    fun toJson(list: List<GitHubIssue>): String {
        return adapter.toJson(list)
    }

    @FromJson
    fun fromJson(data: String): List<GitHubIssue>{
        return adapter.fromJson(data)!!
    }
}