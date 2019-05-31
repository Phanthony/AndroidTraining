package com.example.androidtraining

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.room.Index
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.mock.BehaviorDelegate
import retrofit2.mock.Calls
import retrofit2.mock.MockRetrofit
import retrofit2.mock.NetworkBehavior
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.test.*
import org.junit.Rule

@RunWith(AndroidJUnit4::class)
class GitHubViewModelIntegrationTests {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private fun generateRepoList(): List<GitHubRepo> {
        val test1 = GitHubRepo("test1", GitHubRepoOwner("testLogin1"), 10, null, 1)
        val test2 = (GitHubRepo("test2", GitHubRepoOwner("testLogin2"), 1, "testDesc2", 6))
        val test3 = (GitHubRepo("test3", GitHubRepoOwner("testLogin3"), 89, "testDesc3", 7))
        val test4 = (GitHubRepo("test4", GitHubRepoOwner("testLogin4"), 53, null, 8))
        val test5 = (GitHubRepo("test5", GitHubRepoOwner("testLogin5"), 27, "testDesc5", 9))
        return listOf(test1, test2, test3, test4, test5)
    }

    private fun changeNetworkBehaviour(failPercent: Int) {
        networkBehavior.setDelay(0, TimeUnit.SECONDS)
        networkBehavior.setVariancePercent(0)
        networkBehavior.setFailurePercent(failPercent)
    }

    class MockGitHubApiFail(var delegate: BehaviorDelegate<GitHubApi>) : GitHubApi {
        private val mockFail = IOException("Failed")
        var failure: Call<GitHubRepoList> = Calls.failure(mockFail)
        override fun getRepo(q: String): Call<GitHubRepoList> {
            return delegate.returning(failure).getRepo("")
        }
    }

    class MockGitHubApiResponseFail(var delegate: BehaviorDelegate<GitHubApi>) : GitHubApi {
        var failure: Call<GitHubRepoList> =
            Calls.response(Response.error(400, ResponseBody.create(MediaType.parse(""), "")))

        override fun getRepo(q: String): Call<GitHubRepoList> {
            return delegate.returning(failure).getRepo("")
        }
    }

    class MockGitHubApiSuccess(var delegate: BehaviorDelegate<GitHubApi>, var gitHubRepoList: GitHubRepoList) : GitHubApi {
        override fun getRepo(q: String): Call<GitHubRepoList> {
            return delegate.returningResponse(gitHubRepoList).getRepo("")
        }
    }

    private lateinit var retrofit: Retrofit
    private lateinit var networkBehavior: NetworkBehavior
    private lateinit var mockRetrofit: MockRetrofit
    private lateinit var delegate: BehaviorDelegate<GitHubApi>
    lateinit var mGitHubApi: GitHubApi

    private lateinit var mDB: GitHubRepoDataBase
    private lateinit var gitHubRepoDAO: GitHubRepoDAO
    private lateinit var dayEntryDataDAO: DayEntryDataDAO

    @Mock lateinit var mDay: Day
    @Mock lateinit var listObserver: Observer<List<GitHubRepo>>
    @Mock lateinit var codeObserver: Observer<Int>
    @Mock lateinit var mService: Service
    lateinit var model: ReposCompletedDatabase
    lateinit var mRepository: GitHubRepository

    lateinit var gitHubViewModelInjected: GitHubViewModelInjected

    private fun clearDatabase() {
        runBlocking {
            gitHubRepoDAO.deleteAllRepos()
        }
    }

    private fun populateDatabase() {
        runBlocking {
            gitHubRepoDAO.insert(GitHubRepo("test1", GitHubRepoOwner("testLogin1"), 10, null, 1))
            gitHubRepoDAO.insert(GitHubRepo("test2", GitHubRepoOwner("testLogin2"), 1, "testDesc2", 2))
            gitHubRepoDAO.insert(GitHubRepo("test3", GitHubRepoOwner("testLogin3"), 89, "testDesc3", 3))
            gitHubRepoDAO.insert(GitHubRepo("test4", GitHubRepoOwner("testLogin4"), 53, null, 4))
            gitHubRepoDAO.insert(GitHubRepo("test5", GitHubRepoOwner("testLogin5"), 27, "testDesc5", 5))
        }
    }

    @ExperimentalCoroutinesApi
    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        whenever(mDay.getYesterday()).thenReturn("1998-03-10")
        //Set up mock database
        val context = ApplicationProvider.getApplicationContext<Context>()
        mDB = Room.inMemoryDatabaseBuilder(context, GitHubRepoDataBase::class.java).allowMainThreadQueries().build()
        gitHubRepoDAO = mDB.gitHubRepoDAO()
        dayEntryDataDAO = mDB.dayDAO()
        //Set up Repository with mock objects
        model = ReposCompletedDatabase(mDB)
        mRepository = GitHubRepository(mDB, model, mService,mDay)
        //Set up viewmodel with mock objects
        gitHubViewModelInjected = GitHubViewModelInjected(mRepository)
        //prevent the initial pull in initialsetup by pre populating the database
        populateDatabase()
        runBlocking {
            //prepopulating the database
            dayEntryDataDAO.insertDay(DayEntry("1998-03-10", 1))
            gitHubViewModelInjected.initialSetup()
        }
        //Set up mock retrofit
        retrofit = Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        networkBehavior = NetworkBehavior.create()
        mockRetrofit = MockRetrofit.Builder(retrofit)
            .networkBehavior(networkBehavior)
            .build()

        delegate = mockRetrofit.create(GitHubApi::class.java)
    }

    @After
    fun close() {
        mDB.close()
    }

    @Test
    fun testRepoCount1() {
        runBlocking {
            assertEquals(5, gitHubRepoDAO.getRepoCount())
        }
    }

    @Test
    fun testRepoCount2() {
        clearDatabase()
        runBlocking {
            assertEquals(0, gitHubRepoDAO.getRepoCount())
        }
    }

    @Test
    fun testRepoCount3() {
        runBlocking {
            gitHubRepoDAO.insert(GitHubRepo("title", GitHubRepoOwner("fakeName"), 1, null, 7))
            assertEquals(6, gitHubRepoDAO.getRepoCount())
        }
    }

    @Test
    fun testInsertReplace() {
        runBlocking {
            gitHubRepoDAO.insert(GitHubRepo("title", GitHubRepoOwner("fakeName"), 1, null, 1))
            assertEquals(5, gitHubRepoDAO.getRepoCount())
        }
    }

    @Test
    fun testcheckYesterday1() {
        runBlocking {
            mRepository.checkYesterday("1998-03-10")
            assertEquals(5, gitHubRepoDAO.getRepoCount())
        }
    }

    @Test
    fun testcheckYesterday2() {
        runBlocking {
            mRepository.checkYesterday("2019-03-10")
            assertEquals(0, gitHubRepoDAO.getRepoCount())
        }
    }

    @Test
    fun testgetReposDaily1() {
        val testRepo1 = GitHubRepo("test10", GitHubRepoOwner("testLogin10"), 100, "desc", 1)
        val testRepo2 = GitHubRepo("test9000", GitHubRepoOwner("testLogin16"), 90, null, 10)
        val testRepo3 = GitHubRepo("test14", GitHubRepoOwner("testLogin10"), 810, "desc", 810)
        val testRepo4 = GitHubRepo("test81", GitHubRepoOwner("testLogin112"), 4, null, 92)

        runBlocking {
            whenever(mService.getRepos(any())).thenReturn(
                GitHubRepoList(
                    listOf(
                        testRepo1,
                        testRepo2,
                        testRepo3,
                        testRepo4
                    )
                )
            )
            val code = mRepository.getDailyRepos()
            assertEquals(2, code)
            assertEquals(8, gitHubRepoDAO.getRepoCount())
        }
    }

    @Test
    fun testgetReposDaily2() {
        runBlocking {
            whenever(mService.getRepos(any())).thenReturn(null)
            val code = mRepository.getDailyRepos()
            assertEquals(1, code)
            assertEquals(5, gitHubRepoDAO.getRepoCount())
        }
    }

    @Test
    fun testretrofitReturnsList() {
        changeNetworkBehaviour(0)
        val mGithubReposList = GitHubRepoList(generateRepoList())
        mGitHubApi = MockGitHubApiSuccess(delegate, mGithubReposList)
        val mRetrofitService = RetroFitService(mGitHubApi)
        runBlocking {
            val completed = mRetrofitService.getRepos("1998-03-10")
            assertEquals(mGithubReposList, completed)
        }
    }

    @Test
    fun testretrofitReturnsIOException() {
        changeNetworkBehaviour(100)
        mGitHubApi = MockGitHubApiFail(delegate)
        val mRetrofitService = RetroFitService(mGitHubApi)
        runBlocking {
            val completed = mRetrofitService.getRepos("1998-03-10")
            assertEquals(null, completed)
        }
    }

    @Test
    fun testretrofitReturnsUnsuccessfulResponse() {
        changeNetworkBehaviour(0)
        mGitHubApi = MockGitHubApiResponseFail(delegate)
        val mRetrofitService = RetroFitService(mGitHubApi)
        runBlocking {
            val completed = mRetrofitService.getRepos("1998-03-10")
            assertEquals(null, completed)
        }
    }

    @Test
    fun testGrabsReposAndStoresIntoDatabase() {
        changeNetworkBehaviour(0)
        val mGithubReposList = GitHubRepoList(generateRepoList())
        mGitHubApi = MockGitHubApiSuccess(delegate, mGithubReposList)
        val mRetrofitService = RetroFitService(mGitHubApi)
        //Reset Repository since we are injecting a Mocked Retrofit.
        mRepository = GitHubRepository(mDB, model, mRetrofitService,mDay)
        val mGitHubViewModelInjected = GitHubViewModelInjected(mRepository)
        //set up observers
        mGitHubViewModelInjected.getErrorCode().observeForever(codeObserver)
        mGitHubViewModelInjected.getRepoList()?.observeForever(listObserver)
        runBlocking {
            mGitHubViewModelInjected.initialSetup()
            mGitHubViewModelInjected.getRepos()
            assertEquals(9, gitHubRepoDAO.getRepoCount())
        }
        assertEquals(2,mGitHubViewModelInjected.getErrorCode().value)
        assertEquals(9,mGitHubViewModelInjected.getRepoList()?.value?.size)
    }

    @Test
    fun testGrabsReposAndFails() {
        changeNetworkBehaviour(0)
        mGitHubApi = MockGitHubApiFail(delegate)
        val mRetrofitService = RetroFitService(mGitHubApi)
        //Reset Repository since we are injecting a Mocked Retrofit.
        mRepository = GitHubRepository(mDB, model, mRetrofitService,mDay)
        val mGitHubViewModelInjected = GitHubViewModelInjected(mRepository)
        //set up observers
        mGitHubViewModelInjected.getErrorCode().observeForever(codeObserver)
        mGitHubViewModelInjected.getRepoList()?.observeForever(listObserver)
        runBlocking {
            mGitHubViewModelInjected.initialSetup()
            mGitHubViewModelInjected.getRepos()
            assertEquals(5, gitHubRepoDAO.getRepoCount())
        }
        assertEquals(1,mGitHubViewModelInjected.getErrorCode().value)
        assertEquals(5,mGitHubViewModelInjected.getRepoList()?.value?.size)
    }

    @Test
    fun testNewDayAndGrabReposFails(){
        //reset getYesterday to produce a different day
        whenever(mDay.getYesterday()).thenReturn("1998-03-15")
        changeNetworkBehaviour(0)
        mGitHubApi = MockGitHubApiFail(delegate)
        val mRetrofitService = RetroFitService(mGitHubApi)
        //Reset Repository since we are injecting a Mocked Retrofit.
        mRepository = GitHubRepository(mDB, model, mRetrofitService,mDay)
        val mGitHubViewModelInjected = GitHubViewModelInjected(mRepository)
        runBlocking {
            mGitHubViewModelInjected.initialSetup()
            mGitHubViewModelInjected.getRepos()
        }
    }

}