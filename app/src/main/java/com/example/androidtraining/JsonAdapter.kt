package com.example.androidtraining

import com.squareup.moshi.Moshi
import javax.inject.Inject

interface JsonAdapter {
    fun <T> fromJson(json: String, clazz: Class<T>): T
}

class MoshiJsonAdapter @Inject constructor(): JsonAdapter {

    val moshi = Moshi.Builder().build()

    override fun <T> fromJson(json: String, clazz: Class<T>): T {
        val adapter = moshi.adapter<T>(clazz)
        return adapter.fromJson(json) as T
    }

}