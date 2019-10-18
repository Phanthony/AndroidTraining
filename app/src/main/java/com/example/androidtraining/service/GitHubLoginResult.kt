package com.example.androidtraining.service

data class GitHubLoginResult(val message: String, val response: GitHubLoginResponse)

data class GitHubLoginResponse(val access_token: String, val auth_url: String)
