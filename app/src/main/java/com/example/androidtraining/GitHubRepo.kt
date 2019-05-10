package com.example.androidtraining

class GitHubRepo(var name: String, var owner: GitHubRepoOwner, var stargazers_count: Int, var description: String)

class GitHubRepoList(var items: List<GitHubRepo>)

class GitHubRepoOwner(var login: String)