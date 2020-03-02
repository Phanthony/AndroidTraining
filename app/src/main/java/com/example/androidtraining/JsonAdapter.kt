package com.example.androidtraining

import com.squareup.moshi.Moshi
import javax.inject.Inject

interface JsonAdapter {
    fun <T> fromJson(json: String, clazz: Class<T>): T

    fun <T: Any> toJson(data: T): String
}

class MoshiJsonAdapter @Inject constructor(): JsonAdapter {

    val moshi = Moshi.Builder().build()

    override fun <T> fromJson(json: String, clazz: Class<T>): T {
        val adapter = moshi.adapter<T>(clazz)
        return adapter.fromJson(json) as T
    }

    override fun <T: Any> toJson(data: T): String {
        val jsonAdapter = moshi.adapter<T>(data::class.java)

        return jsonAdapter.toJson(data)
    }

}