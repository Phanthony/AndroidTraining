package com.example.androidtraining

import com.example.androidtraining.service.*
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations.initMocks
import org.mockito.junit.MockitoJUnitRunner
import retrofit2.Response
import retrofit2.adapter.rxjava2.Result

@RunWith(MockitoJUnitRunner::class)
class RetrofitServiceTests {
    @Mock
    lateinit var mGitHubApi: GitHubApi

    @Mock
    lateinit var mDevApi: DevApi

    @Mock
    lateinit var mResponseProcessor: ResponseProcessor

    lateinit var mRetrofit: RetrofitService

    @Before
    fun setup() {
        initMocks(this)
        mRetrofit = RetrofitService(mGitHubApi, mDevApi, mResponseProcessor)
    }

    @Test
    fun `test loginToGithub success`() {
        val body = GitHubLoginResult("Success", GitHubLoginResponse("fake_token", "fake_url"))
        val response = Result.response(Response.success(200, body))
        whenever(mDevApi.loginGithub(any())).thenReturn(Single.just(response))
        whenever(mResponseProcessor.process(any<Result<GitHubLoginResult>>(), any())).thenAnswer {
            ResponseProcessor.ProcessedResult(
                null,
                null,
                body
            )
        }
        val res = mRetrofit.loginToGithub("TestUser", "TestPass").blockingGet()
        verify(mDevApi).loginGithub(
            AuthMobileRequestBody(
                listOf("repo"),
                "4cc5aa575096c8bcb036",
                "TestUser",
                "TestPass"
            )
        )
        assertThat(res.isSuccess).isTrue()
        assertThat(res.getOrNull()!!).isEqualTo(body)
    }

    @Test
    fun `test loginToGithub failure`(){
        
    }

}