package com.example.androidtraining

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.mock
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

@RunWith(AndroidJUnit4::class)
class GItHubViewModelIntegrationTests{

    lateinit var mDB: GitHubRepoDataBase
    lateinit var gitHubRepoDAO: GitHubRepoDAO
    lateinit var dayEntryDataDAO: DayEntryDataDAO

    var mService = Mockito.mock(Service::class.java)!!
    lateinit var model: ReposCompletedDatabase
    lateinit var mRepository: GitHubRepository

    lateinit var gitHubViewModelInjected: GitHubViewModelInjected

    private fun clearDatabase(){
        runBlocking {
            gitHubRepoDAO.deleteAllRepos()
        }
    }
    private fun populateDatabase(){
        runBlocking {
            gitHubRepoDAO.insert(GitHubRepo("test1", GitHubRepoOwner("testLogin1"),10,null,1))
            gitHubRepoDAO.insert(GitHubRepo("test2",GitHubRepoOwner("testLogin2"),1,"testDesc2",2))
            gitHubRepoDAO.insert(GitHubRepo("test3",GitHubRepoOwner("testLogin3"),89,"testDesc3",3))
            gitHubRepoDAO.insert(GitHubRepo("test4",GitHubRepoOwner("testLogin4"),53,null,4))
            gitHubRepoDAO.insert(GitHubRepo("test5",GitHubRepoOwner("testLogin5"),27,"testDesc5",5))
        }
    }

    @Before
    fun setup(){
        MockitoAnnotations.initMocks(this)

        val context = ApplicationProvider.getApplicationContext<Context>()
        mDB = Room.inMemoryDatabaseBuilder(context,GitHubRepoDataBase::class.java).allowMainThreadQueries().build()
        gitHubRepoDAO = mDB.gitHubRepoDAO()
        dayEntryDataDAO = mDB.dayDAO()

        model = ReposCompletedDatabase(mDB)
        mRepository = GitHubRepository(mDB,model,mService)

        gitHubViewModelInjected = GitHubViewModelInjected(mRepository)

        //prevent the initial pull in initialsetup
        populateDatabase()
        runBlocking {
            gitHubViewModelInjected.initialSetup()
        }
    }

    @After
    fun close(){
        clearDatabase()
        mDB.close()
    }

    @Test
    fun testRepoCount1(){
        runBlocking {
            assertEquals(5,gitHubRepoDAO.getRepoCount())
        }
    }

    @Test
    fun testRepoCount2(){
        clearDatabase()
        runBlocking {
            assertEquals(0,gitHubRepoDAO.getRepoCount())
        }
    }

}