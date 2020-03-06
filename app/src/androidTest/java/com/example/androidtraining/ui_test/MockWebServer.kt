package com.example.androidtraining.ui_test

import com.example.androidtraining.JsonAdapter
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wrapper around OkHttp [MockWebServer] to give an api that is consistent no matter the networking library used.
 */
@Singleton
class MockWebServer @Inject constructor(private val mockWebServer: MockWebServer, private val jsonAdapter: JsonAdapter) {

    val url: String
        get() = mockWebServer.url("/").toString()

    fun <T: Any> queue(statusCode: Int, data: T, headers: Map<String, String>? = null) {
        this.queueResponse(statusCode, data, headers)
    }

    fun <T: Any> queue(statusCode: Int, data: Array<T>, headers: Map<String, String>? = null) {
        this.queueResponse(statusCode, data, headers)
    }

    // Unfortunately, I have not figured out a way to test this. mock web server does not have a method to do this. retrofit-mock does have a method, but this requires you to also mock the return value from your retrofit interface which then makes my test unusable as my code is not running through retrofit.
//    fun queueError(error: Throwable) {
//
//    }

    private fun <T: Any> queueResponse(statusCode: Int, data: T, headers: Map<String, String>? = null) {
        val body = jsonAdapter.toJson(data)

        val mockResponse = MockResponse().setResponseCode(statusCode).setBody(body).throttleBody(1024,1,
            TimeUnit.SECONDS)

        headers?.let { header ->
            header.forEach { (key, value) ->
                mockResponse.setHeader(key, value)
            }
        }

        mockWebServer.enqueue(mockResponse)
    }

}