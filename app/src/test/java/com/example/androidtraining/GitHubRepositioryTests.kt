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
    @Mock lateinit var mModel: ReposCompleted

    lateinit var mRepository: GitHubRepository

    @Before
    fun setup(){
        MockitoAnnotations.initMocks(this)
        mRepository = GitHubRepository(mDatabase,mModel,mService)
        whenever(mModel.saveRepos(any())).then {  }
    }


    @Test
    fun `getReposDaily no internet connection`(){
       runBlocking{
            whenever(mService.getRepos(any())).thenReturn(null)
            val result = mRepository.getDailyRepos()
            assertEquals(result,1)
        }
    }

    @Test
    fun `getReposDaily successful connection`(){
        runBlocking {
            whenever(mService.getRepos(any())).thenReturn(listOf())
            val result = mRepository.getDailyRepos()
            assertEquals(result,2)
        }
    }

}