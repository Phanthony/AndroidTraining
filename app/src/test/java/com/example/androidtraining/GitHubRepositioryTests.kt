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

@RunWith(MockitoJUnitRunner::class)
class RepositoryTests{



    @Mock lateinit var mDatabase: GitHubRepoDataBase
    @Mock lateinit var mService: Service
    @Mock lateinit var mDay: Day

    @Before
    fun setup(){
        MockitoAnnotations.initMocks(this)
        whenever(mDay.getYesterday()).thenReturn("")
    }


    @Test
    fun `getReposDaily no internet connection`(){
       runBlocking{
            whenever(mService.getRepos(any())).thenReturn(null)
            val result = mRepository.getDailyRepos()
            assertEquals(result,null)
        }
    }

    @Test
    fun `getReposDaily successful connection`(){
        runBlocking {
            val mGitHubRepoList = GitHubRepoList(listOf())
            val result = mRepository.getDailyRepos()
            assertEquals(result,mGitHubRepoList)
        }
    }
}