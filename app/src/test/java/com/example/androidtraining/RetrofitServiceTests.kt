package com.example.androidtraining

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import java.io.IOException

@RunWith(MockitoJUnitRunner::class)
class RetrofitServiceTest{

    @Mock lateinit var mRetrofit: Retrofit
    @Mock lateinit var mService: GitHubApi
    @Mock lateinit var mResponse: Response<GitHubRepoList>
    @Mock lateinit var mCallback: Call<GitHubRepoList>

    lateinit var mRetrofitService: RetroFitService

    @Before
    fun setup(){
        MockitoAnnotations.initMocks(this)
        whenever(mRetrofit.create(GitHubApi::class.java)).thenReturn(mService)
        mRetrofitService = RetroFitService(mRetrofit)
    }

    @Test
    fun `getRepos execute is Successful and Response is successful`(){
        val testList = listOf<GitHubRepo>()
        val testGithubRepoList = GitHubRepoList(testList)
        whenever(mResponse.isSuccessful).thenReturn(true)
        whenever(mResponse.body()).thenReturn(testGithubRepoList)
        whenever(mCallback.execute()).thenReturn(mResponse)
        runBlocking {
            whenever(mService.getRepo(any())).thenReturn(mCallback)
            assertEquals(mRetrofitService.getRepos(any()),testList)
        }
    }

    @Test
    fun `getRepos execute is Successful and Response is unsuccessful`(){
        whenever(mResponse.isSuccessful).thenReturn(false)
        whenever(mCallback.execute()).thenReturn(mResponse)
        runBlocking {
            whenever(mService.getRepo(any())).thenReturn(mCallback)
            assertEquals(mRetrofitService.getRepos(any()),null)
        }
    }

    @Test
    fun `getRepos execute is unsuccessful`(){
        whenever(mCallback.execute()).then{throw IOException()}
        runBlocking {
            whenever(mService.getRepo(any())).thenReturn(mCallback)
            assertEquals(mRetrofitService.getRepos(any()),null)
        }
    }
    @Test
    fun `getRepos execute is unsuccessful2`(){
        runBlocking {
            whenever(mService.getRepo(any())).then{throw IOException()}
            assertEquals(mRetrofitService.getRepos(any()),null)
        }
    }

}