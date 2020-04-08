package com.example.androidtraining

import android.content.SharedPreferences
import com.example.androidtraining.database.GitHubDataBase
import com.example.androidtraining.database.GitHubIssueCommentDAO
import com.example.androidtraining.database.GitHubUser
import com.example.androidtraining.database.teller.ResultPaging
import com.example.androidtraining.database.teller.TellerIssueCommentsOnlineRepository
import com.example.androidtraining.service.GitHubIssueComment
import com.example.androidtraining.service.Service
import com.example.androidtraining.service.error.NoInternetConnectionException
import com.google.common.truth.Truth.assertThat
import com.levibostian.teller.Teller
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class TellerIssueCommentOnlineRepositoryTests {

    @Mock
    lateinit var mService: Service

    @Mock
    lateinit var mDatabase: GitHubDataBase

    @Mock
    lateinit var mPref: SharedPreferences

    @Mock
    lateinit var mDao: GitHubIssueCommentDAO

    lateinit var mTeller: TellerIssueCommentsOnlineRepository
    lateinit var req: TellerIssueCommentsOnlineRepository.GetCommentRequirement


    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        Teller.initTesting(mPref)
        mTeller = TellerIssueCommentsOnlineRepository(mDatabase,mService)
        req = TellerIssueCommentsOnlineRepository.GetCommentRequirement(1,"","",1)
        mTeller.requirements = req
        whenever(mDatabase.gitHubIssueCommentDAO()).thenReturn(mDao)
    }

    @Test
    fun `test fetchFreshCache success Populated List more pages to load`(){
        val user = GitHubUser("TestUser","")
        val body = listOf(GitHubIssueComment(1,user,"TestMessage",1))
        val result = Result.success(body)
        val resultPaging = ResultPaging(true,result)

        whenever(mService.getIssueComments(any(), any(), any(), any())).thenReturn(Single.just(resultPaging))

        val fetch = mTeller.fetchFreshCache(req).blockingGet()
        assertThat(fetch.isSuccessful()).isTrue()
        assertThat(fetch.response).isEqualTo(body)
        assertThat(fetch.failure).isNull()
        assertThat(mTeller.morePagesDataToLoad).isTrue()
    }

    @Test
    fun `test fetchFreshCache success Populated List no pages to load`(){
        val user = GitHubUser("TestUser","")
        val body = listOf(GitHubIssueComment(1,user,"TestMessage",1))
        val result = Result.success(body)
        val resultPaging = ResultPaging(false,result)

        whenever(mService.getIssueComments(any(), any(), any(), any())).thenReturn(Single.just(resultPaging))

        val fetch = mTeller.fetchFreshCache(req).blockingGet()
        assertThat(fetch.isSuccessful()).isTrue()
        assertThat(fetch.response).isEqualTo(body)
        assertThat(fetch.failure).isNull()
        assertThat(mTeller.morePagesDataToLoad).isFalse()
    }

    @Test
    fun `test fetchFreshCache success Empty List more pages to load`(){
        val body = listOf<GitHubIssueComment>()
        val result = Result.success(body)
        val resultPaging = ResultPaging(true,result)

        whenever(mService.getIssueComments(any(), any(), any(), any())).thenReturn(Single.just(resultPaging))

        val fetch = mTeller.fetchFreshCache(req).blockingGet()
        assertThat(fetch.isSuccessful()).isTrue()
        assertThat(fetch.response).isEqualTo(body)
        assertThat(fetch.failure).isNull()
        assertThat(mTeller.morePagesDataToLoad).isTrue()
    }

    @Test
    fun `test fetchFreshCache success Empty List no pages to load`(){
        val body = listOf<GitHubIssueComment>()
        val result = Result.success(body)
        val resultPaging = ResultPaging(false,result)

        whenever(mService.getIssueComments(any(), any(), any(), any())).thenReturn(Single.just(resultPaging))

        val fetch = mTeller.fetchFreshCache(req).blockingGet()
        assertThat(fetch.isSuccessful()).isTrue()
        assertThat(fetch.response).isEqualTo(body)
        assertThat(fetch.failure).isNull()
        assertThat(mTeller.morePagesDataToLoad).isFalse()
    }

    @Test
    fun `test fetchFreshCache failure`(){
        val error = NoInternetConnectionException("")
        val result = Result.failure<List<GitHubIssueComment>>(error)
        val resultPaging = ResultPaging(false,result)

        whenever(mService.getIssueComments(any(), any(), any(), any())).thenReturn(Single.just(resultPaging))

        val fetch = mTeller.fetchFreshCache(req).blockingGet()
        assertThat(fetch.isFailure()).isTrue()
        assertThat(fetch.failure).isEqualTo(error)
        assertThat(fetch.response).isNull()
        assertThat(mTeller.morePagesDataToLoad).isFalse()
    }

    @Test
    fun `test goToNextPage morePages is false`(){
        mTeller.morePagesDataToLoad = false
        mTeller.goToNextPage()
        assertThat(mTeller.pagingRequirements.pageNumber).isEqualTo(1)
    }

    @Test
    fun `test goToNextPage morePages is true`(){
        val body = listOf<GitHubIssueComment>()
        val result = Result.success(body)
        val resultPaging = ResultPaging(true,result)

        whenever(mService.getIssueComments(any(), any(), any(), any())).thenReturn(Single.just(resultPaging))
        mTeller.morePagesDataToLoad = true
        mTeller.goToNextPage()
        assertThat(mTeller.pagingRequirements.pageNumber).isEqualTo(2)
    }

    fun createList(size: Int): MutableList<GitHubIssueComment>{
        val user = GitHubUser("TestUser","")
        val list = mutableListOf<GitHubIssueComment>()
        for(i in 0 until size){
            val testComment = GitHubIssueComment(i,user,"TestMsg",i)
            list.add(testComment)
        }
        return list
    }

    @Test
    fun `test deleteOldCache persistFirstPage and size greater than 30, list size is 60`(){
        val list = createList(60)
        whenever(mDao.getComment(any())).thenReturn(list)
        mTeller.deleteOldCache(req,true).blockingGet()
        verify(mDao, Mockito.times(30)).deleteComment(any())
    }

    @Test
    fun `test deleteOldCache don't persistFirstPage and size greater than 30, list size is 60`(){
        val list = createList(60)
        whenever(mDao.getComment(any())).thenReturn(list)
        mTeller.deleteOldCache(req,false).blockingGet()
        verify(mDao, Mockito.times(60)).deleteComment(any())
    }

    @Test
    fun `test deleteOldCache persistFirstPage and size less than 30, list size is 20`(){
        val list = createList(20)
        whenever(mDao.getComment(any())).thenReturn(list)
        mTeller.deleteOldCache(req,true).blockingGet()
        verify(mDao, Mockito.times(0)).deleteComment(any())
    }

    @Test
    fun `test deleteOldCache don't persistFirstPage and size less than 30, list size is 20`(){
        val list = createList(20)
        whenever(mDao.getComment(any())).thenReturn(list)
        mTeller.deleteOldCache(req,false).blockingGet()
        verify(mDao, Mockito.times(20)).deleteComment(any())
    }

    @Test
    fun `test deleteOldCache persistFirstPage and size is 30`(){
        val list = createList(30)
        whenever(mDao.getComment(any())).thenReturn(list)
        mTeller.deleteOldCache(req,true).blockingGet()
        verify(mDao, Mockito.times(0)).deleteComment(any())
    }

    @Test
    fun `test deleteOldCache don't persistFirstPage and size is 30`(){
        val list = createList(30)
        whenever(mDao.getComment(any())).thenReturn(list)
        mTeller.deleteOldCache(req,false).blockingGet()
        verify(mDao, Mockito.times(30)).deleteComment(any())
    }


}