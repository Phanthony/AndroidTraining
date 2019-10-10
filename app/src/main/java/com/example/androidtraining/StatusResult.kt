package com.example.androidtraining

import java.lang.Exception

class StatusResult<out T>(val statusCode: Int, success: Boolean, val value: T? = null, exception: Throwable? = null) {

    val result: Result<T> = if (success){
        Result.success(value!!)
    } else{
        Result.failure(exception!!)
    }

}