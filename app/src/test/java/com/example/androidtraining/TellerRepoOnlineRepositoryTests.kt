package com.example.androidtraining

import android.content.SharedPreferences
import com.example.androidtraining.database.GitHubDataBase
import com.example.androidtraining.database.GitHubRepo
import com.example.androidtraining.database.GitHubRepoList
import com.example.androidtraining.database.GitHubUser
import com.example.androidtraining.database.teller.TellerRepoOnlineRepository
import com.example.androidtraining.service.Service
import com.example.androidtraining.service.error.NoInternetConnectionException
import com.google.common.truth.Truth.assertThat
import com.levibostian.teller.Teller
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class TellerRepoOnlineRepositoryTests {
    @Mock
    lateinit var mService: Service

    @Mock
    lateinit var mDatabase: GitHubDataBase

    @Mock
    lateinit var mPref: SharedPreferences

    @Mock
    lateinit var mDay: Day

    lateinit var req: TellerRepoOnlineRepository.GetReposRequirement
    lateinit var mTeller: TellerRepoOnlineRepository

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        Teller.initTesting(mPref)
        mTeller = TellerRepoOnlineRepository(mDatabase,mService)
        req = TellerRepoOnlineRepository.GetReposRequirement(mDay)
        mTeller.requirements = req
        whenever(mDay.getYesterday()).thenReturn("1-1-2020")
    }

    @Test
    fun `test FetchFreshCache success empty list`(){
        val body = GitHubRepoList(listOf())
        val result = Result.success(body)
        whenever(mService.getRepos(any())).thenReturn(Single.just(result))
        val fetch = mTeller.fetchFreshCache(req).blockingGet()

        assertThat(fetch.isSuccessful()).isTrue()
        assertThat(fetch.response).isEqualTo(body)
    }

    @Test
    fun `test FetchFreshCache success populated list`(){
        val body = GitHubRepoList(listOf(GitHubRepo("TestRepo", GitHubUser("TestUser",""),1,"TestDesc",1)))
        val result = Result.success(body)
        whenever(mService.getRepos(any())).thenReturn(Single.just(result))
        val fetch = mTeller.fetchFreshCache(req).blockingGet()

        assertThat(fetch.isSuccessful()).isTrue()
        assertThat(fetch.failure).isNull()
        assertThat(fetch.response).isEqualTo(body)
    }

    @Test
    fun `test FetchFreshCache failure`(){
        val error = NoInternetConnectionException("")
        val result = Result.failure<GitHubRepoList>(error)
        whenever(mService.getRepos(any())).thenReturn(Single.just(result))
        val fetch = mTeller.fetchFreshCache(req).blockingGet()

        assertThat(fetch.isFailure()).isTrue()
        assertThat(fetch.response).isNull()
        assertThat(fetch.failure).isEqualTo(error)


    }




}