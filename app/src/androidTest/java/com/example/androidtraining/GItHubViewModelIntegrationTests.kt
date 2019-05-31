package com.example.androidtraining

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
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
            //prepopulate the day part of the database
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
            whenever(mDay.getYesterday()).thenReturn("1998-03-10")
            mRepository.checkYesterday()
            assertEquals(5, gitHubRepoDAO.getRepoCount())
        }
    }

    @Test
    fun testcheckYesterday2() {
        runBlocking {
            whenever(mDay.getYesterday()).thenReturn("2019-03-10")
            mRepository.checkYesterday()
            assertEquals(0, gitHubRepoDAO.getRepoCount())
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
        changeNetworkBehaviour(100)
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
        changeNetworkBehaviour(100)
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
    fun testNewDayAndGrabReposSuccess(){
        //reset getYesterday to produce a different day
        whenever(mDay.getYesterday()).thenReturn("1998-03-15")
        changeNetworkBehaviour(0)
        val mGithubReposList = GitHubRepoList(generateRepoList())
        mGitHubApi = MockGitHubApiSuccess(delegate,mGithubReposList)
        val mRetrofitService = RetroFitService(mGitHubApi)
        //Reset Repository since we are injecting a Mocked Retrofit.
        mRepository = GitHubRepository(mDB, model, mRetrofitService,mDay)
        val mGitHubViewModelInjected = GitHubViewModelInjected(mRepository)
        //set up observers
        mGitHubViewModelInjected.getErrorCode().observeForever(codeObserver)
        mGitHubViewModelInjected.getRepoList()?.observeForever(listObserver)
        runBlocking {
            assertEquals(5, gitHubRepoDAO.getRepoCount())
            mGitHubViewModelInjected.initialSetup()
            mGitHubViewModelInjected.getRepos()
            assertEquals(5, gitHubRepoDAO.getRepoCount())
        }
        assertEquals(2,mGitHubViewModelInjected.getErrorCode().value)
        assertEquals(5,mGitHubViewModelInjected.getRepoList()?.value?.size)
    }
}