package com.example.androidtraining

import android.content.Context
import com.example.androidtraining.database.GitHubRepoList
import com.example.androidtraining.service.*
import com.example.androidtraining.service.error.NoInternetConnectionException
import com.example.androidtraining.service.error.ServerErrorException
import com.example.androidtraining.service.logger.AppActivityLogger
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import retrofit2.Response
import retrofit2.adapter.rxjava2.Result

@RunWith(MockitoJUnitRunner::class)
class RetrofitServiceTests {

    //Use Mockito

    @Mock
    lateinit var mService: GitHubApi
    @Mock
    lateinit var mLoginService: DevApi
    @Mock
    lateinit var mApp: Context

    @Test
    fun `getRepos correct String`() {
        val mResponseProcessor =
            ResponseProcessor(
                mApp,
                AppActivityLogger(),
                MoshiJsonAdapter()
            )
        val testService = RetroFitService(mService, mLoginService, mResponseProcessor)
        val fResult = Single.just(Result.response(Response.success(200,
            GitHubRepoList(listOf())
        )))
        whenever(mService.getRepo(any())).thenReturn(fResult)
        val testString = "1998-03-10"
        val getRepoArgumentCaptor = argumentCaptor<String>()
        testService.getRepos(testString)
        verify(mService).getRepo(getRepoArgumentCaptor.capture())
        assertThat(getRepoArgumentCaptor.firstValue).isEqualTo("created:%3E1998-03-10+language:kotlin+stars:%3E0")
    }

    @Test
    fun `getRepos mapping a successful call`() {
        val mResponseProcessor =
            ResponseProcessor(
                mApp,
                AppActivityLogger(),
                MoshiJsonAdapter()
            )
        val testService = RetroFitService(mService, mLoginService, mResponseProcessor)
        val returnList =
            GitHubRepoList(listOf())
        val fResult = Single.just(Result.response(Response.success(200, returnList)))
        whenever(mService.getRepo(any())).thenReturn(fResult)
        val single = testService.getRepos("1998-03-10")
        val expecetedResult = kotlin.Result.success(returnList)
        val result = single.blockingGet()
        assertThat(result.isSuccess).isTrue()
        assertThat(result.exceptionOrNull()).isNull()
        assertThat(result.getOrNull()).isNotNull()
        assertThat(result).isEqualTo(expecetedResult)
        assertThat(result.getOrNull()).isEqualTo(returnList)
    }

    @Test
    fun `getRepos mapping a failure call`() {
        val mResponseProcessor =
            ResponseProcessor(
                mApp,
                AppActivityLogger(),
                MoshiJsonAdapter()
            )
        val testService = RetroFitService(mService, mLoginService, mResponseProcessor)
        val exception = NoInternetConnectionException("")
        val fResult = Single.just(Result.error<GitHubRepoList>(exception))
        whenever(mService.getRepo(any())).thenReturn(fResult)
        val single = testService.getRepos("1998-03-10")
        val expectedResult = kotlin.Result.failure<GitHubRepoList>(exception)
        val result = single.blockingGet()
        assertThat(result.isSuccess).isFalse()
        assertThat(result.getOrNull()).isNull()
        assertThat(result.exceptionOrNull()).isNotNull()
        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `loginToGithub correct username and password`() {
        val mResponseProcessor =
            ResponseProcessor(
                mApp,
                AppActivityLogger(),
                MoshiJsonAdapter()
            )
        val testService = RetroFitService(mService, mLoginService, mResponseProcessor)
        val fResult = Single.just(
            Result.response(
                Response.success(
                    200, GitHubLoginResult(
                        "",
                        GitHubLoginResponse("", "")
                    )
                )
            )
        )
        whenever(mLoginService.loginGithub(any())).thenReturn(fResult)
        val loginArgCap = argumentCaptor<AuthMobileRequestBody>()
        testService.loginToGithub("password", "username")
        verify(mLoginService).loginGithub(loginArgCap.capture())
        assertThat(loginArgCap.firstValue.password).isEqualTo("password")
        assertThat(loginArgCap.firstValue.username).isEqualTo("username")
        assertThat(loginArgCap.firstValue.client_id).isEqualTo("4cc5aa575096c8bcb036")
        assertThat(loginArgCap.firstValue.scopes).isEqualTo(listOf("repo"))
    }

    @Test
    fun `loginToGithub mapping a successful call`(){
        val mResponseProcessor =
            ResponseProcessor(
                mApp,
                AppActivityLogger(),
                MoshiJsonAdapter()
            )
        val testService = RetroFitService(mService, mLoginService, mResponseProcessor)
        val fResult = Single.just(
            Result.response(
                Response.success(
                    200, GitHubLoginResult(
                        "successful",
                        GitHubLoginResponse("access", "auth")
                    )
                )
            )
        )
        whenever(mLoginService.loginGithub(any())).thenReturn(fResult)
        val loginResult = testService.loginToGithub("password","username")
        val successResult = loginResult.blockingGet()
        assertThat(successResult.isSuccess).isTrue()
        assertThat(successResult.exceptionOrNull()).isNull()
        assertThat(successResult.getOrNull()).isNotNull()
        assertThat(successResult.getOrNull()!!.message).isEqualTo("successful")
        assertThat(successResult.getOrNull()!!.response.access_token).isEqualTo("access")
        assertThat(successResult.getOrNull()!!.response.auth_url).isEqualTo("auth")
    }

    @Test
    fun `loginToGithub mapping a failed call`(){
        val mResponseProcessor =
            ResponseProcessor(
                mApp,
                AppActivityLogger(),
                MoshiJsonAdapter()
            )
        val testService = RetroFitService(mService, mLoginService, mResponseProcessor)
        val mResult = Single.just(Result.response(Response.error<GitHubLoginResult>(500, ResponseBody.create(MediaType.parse("error"),"failure"))))
        whenever(mLoginService.loginGithub(any())).thenReturn(mResult)
        whenever(mApp.getString(any())).thenReturn("error")
        val loginResult = testService.loginToGithub("password","username")
        val failResult = loginResult.blockingGet()
        assertThat(failResult.isFailure).isTrue()
        assertThat(failResult.getOrNull()).isNull()
        assertThat(failResult.exceptionOrNull()).isNotNull()
        assertThat(failResult.exceptionOrNull()).isInstanceOf(ServerErrorException::class.java)
    }
}