package com.example.androidtraining

data class ResultPaging<T>(var morePagesToLoad: Boolean, var result: Result<T>)