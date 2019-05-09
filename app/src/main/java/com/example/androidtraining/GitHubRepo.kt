package com.example.androidtraining

data class GitHubRepo(var name: String, var owner: GitHubRepoOwner, var stargazers_count: Int, var description: String)

data class GitHubRepoList(var items: ArrayList<GitHubRepo>)

data class GitHubRepoOwner(var login: String)