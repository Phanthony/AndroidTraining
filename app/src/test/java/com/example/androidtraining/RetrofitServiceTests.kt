package com.example.androidtraining

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class RetrofitServiceTests {

    //Use Mockito

    @Mock
    lateinit var mRepoService: GitHubApi
    @Mock
    lateinit var mLoginSerivce: DevApi
    @Mock
    lateinit var mResponseProcessor: ResponseProcessor

    lateinit var testService: RetroFitService

    @Before
    fun setup(){
        testService = RetroFitService(mRepoService, mLoginSerivce,mResponseProcessor)
    }

    @Test
    fun `getRepos correct String`() {
        val singleResult = Single.just(Result.success(GitHubRepoList(listOf())))
        whenever(testService.getRepos("created:%3E1998-03-10+language:kotlin+stars:%3E0")).thenReturn(
            singleResult
        )
        val testString = "1998-03-10"
        val getRepoArgumentCaptor = argumentCaptor<String>()
        testService.getRepos(testString)
        verify(mRepoService).getRepo(getRepoArgumentCaptor.capture())
        assertThat(getRepoArgumentCaptor.firstValue).isEqualTo("created:%3E1998-03-10+language:kotlin+stars:%3E0")
    }

    fun `getRepos mapping`() {
        val testUser = "testuser7891"
        val testPass = "goldcatchadmit72"
        val getLoginArgumentCaptor = argumentCaptor<AuthMobileRequestBody>()
        testService.loginToGithub(testPass, testUser)
        verify(mLoginSerivce).loginGithub(getLoginArgumentCaptor.capture())
        assertThat(getLoginArgumentCaptor.firstValue.username).isEqualTo(testUser)
        assertThat(getLoginArgumentCaptor.firstValue.password).isEqualTo(testPass)
    }

}