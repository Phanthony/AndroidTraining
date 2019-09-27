package com.example.androidtraining

data class GitHubLoginResultSuccess(val message: String, val response: GitHubLoginResponse)

data class GitHubLoginResponse(val access_token: String, val auth_url: String)

data class GitHubLoginResultError(val message: String, val error_type: String)