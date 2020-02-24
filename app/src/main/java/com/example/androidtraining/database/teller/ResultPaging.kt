package com.example.androidtraining.database.teller

data class ResultPaging<T>(var morePagesToLoad: Boolean, var result: Result<T>)