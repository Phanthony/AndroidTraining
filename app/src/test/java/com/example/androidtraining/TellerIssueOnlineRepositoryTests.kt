package com.example.androidtraining

import android.content.SharedPreferences
import com.example.androidtraining.database.GitHubDataBase
import com.example.androidtraining.database.GitHubRepo
import com.example.androidtraining.database.GitHubUser
import com.example.androidtraining.database.teller.TellerIssueOnlineRepository
import com.example.androidtraining.service.GitHubIssue
import com.example.androidtraining.service.Service
import com.example.androidtraining.service.error.NoInternetConnectionException
import com.google.common.truth.Truth.assertThat
import com.levibostian.teller.Teller
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class TellerIssueOnlineRepositoryTests {
    @Mock
    lateinit var mService: Service

    @Mock
    lateinit var mDatabase: GitHubDataBase

    @Mock
    lateinit var mPref: SharedPreferences

    lateinit var req: TellerIssueOnlineRepository.GetIssuesRequirement
    lateinit var mTeller: TellerIssueOnlineRepository

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        Teller.initTesting(mPref)
        mTeller = TellerIssueOnlineRepository(mDatabase,mService)
        req = TellerIssueOnlineRepository.GetIssuesRequirement("TestUser")
        mTeller.requirements = req
    }

    @Test
    fun `test FetchFreshCache success Empty List`(){
        val body = listOf<GitHubIssue>()
        val result = Result.success(body)

        whenever(mService.getIssues()).thenReturn(Single.just(result))

        val fetch = mTeller.fetchFreshCache(req).blockingGet()
        assertThat(fetch.isSuccessful()).isTrue()
        assertThat(fetch.response).isEqualTo(body)
        assertThat(fetch.failure).isNull()
    }

    @Test
    fun `test FetchFreshCache success Populated List`(){
        val user = GitHubUser("TestUser","")
        val repo = GitHubRepo("TestRepo",user,1,"TestDesc",1)
        val body = listOf(GitHubIssue(1,1,"open","TestTitle",user,1,"", repo))
        val result = Result.success(body)

        whenever(mService.getIssues()).thenReturn(Single.just(result))

        val fetch = mTeller.fetchFreshCache(req).blockingGet()
        assertThat(fetch.isSuccessful()).isTrue()
        assertThat(fetch.response).isEqualTo(body)
        assertThat(fetch.failure).isNull()
    }

    @Test
    fun `test FetchFreshCache failure`(){
        val error = NoInternetConnectionException("")
        val result = Result.failure<List<GitHubIssue>>(error)

        whenever(mService.getIssues()).thenReturn(Single.just(result))

        val fetch = mTeller.fetchFreshCache(req).blockingGet()
        assertThat(fetch.isFailure()).isTrue()
        assertThat(fetch.failure).isEqualTo(error)
        assertThat(fetch.response).isNull()
    }
}