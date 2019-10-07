package com.example.androidtraining

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.androidtraining.service.logger.AppActivityLogger
import com.google.common.truth.Truth.assertThat
import com.levibostian.teller.Teller
import com.levibostian.teller.cachestate.OnlineCacheState
import com.levibostian.teller.repository.OnlineRepository
import com.levibostian.teller.testing.extensions.cache
import com.levibostian.teller.testing.extensions.initState
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.runner.RunWith
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.Result
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
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
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val tellerInit = TellerInitRule()

    private fun generateRepoList(): List<GitHubRepo> {
        val test1 = GitHubRepo(
            "test1",
            GitHubRepoOwner("testLogin1", "https://avatars1.githubusercontent.com/u/930751?v=4"),
            10,
            null,
            1
        )
        val test2 = (GitHubRepo(
            "test2",
            GitHubRepoOwner("testLogin2", "https://avatars1.githubusercontent.com/u/930751?v=4"),
            1,
            "testDesc2",
            6
        ))
        val test3 = (GitHubRepo(
            "test3",
            GitHubRepoOwner("testLogin3", "https://avatars1.githubusercontent.com/u/930751?v=4"),
            89,
            "testDesc3",
            7
        ))
        val test4 = (GitHubRepo(
            "test4",
            GitHubRepoOwner("testLogin4", "https://avatars1.githubusercontent.com/u/930751?v=4"),
            53,
            null,
            8
        ))
        val test5 = (GitHubRepo(
            "test5",
            GitHubRepoOwner("testLogin5", "https://avatars1.githubusercontent.com/u/930751?v=4"),
            27,
            "testDesc5",
            9
        ))
        return listOf(test1, test2, test3, test4, test5)
    }

    private fun changeNetworkBehaviour(failPercent: Int) {
        networkBehavior.setDelay(0, TimeUnit.SECONDS)
        networkBehavior.setVariancePercent(0)
        networkBehavior.setFailurePercent(failPercent)
    }

    class MockGitHubApiFail(private var delegate: BehaviorDelegate<GitHubApi>) : GitHubApi {
        private val mockFail = IOException("Failed")
        private var failure: Call<GitHubRepoList> = Calls.failure(mockFail)
        override fun getRepo(q: String): Single<Result<GitHubRepoList>> {
            return delegate.returning(failure).getRepo("")
        }
    }

    class MockGitHubApiResponseFail(private var delegate: BehaviorDelegate<GitHubApi>) : GitHubApi {
        private var failure: Call<GitHubRepoList> =
            Calls.response(Response.error(400, ResponseBody.create(MediaType.parse("This is an error"), "This is an error, bad data")))

        override fun getRepo(q: String): Single<Result<GitHubRepoList>> {
            return delegate.returning(failure).getRepo("")
        }
    }

    class MockGitHubApiResponseFail500Error(private var delegate: BehaviorDelegate<GitHubApi>) : GitHubApi {
        private var failure: Call<GitHubRepoList> =
            Calls.response(Response.error(500, ResponseBody.create(MediaType.parse(""), "")))

        override fun getRepo(q: String): Single<Result<GitHubRepoList>> {
            return delegate.returning(failure).getRepo("")
        }
    }

    class MockGitHubApiSuccess(
        private var delegate: BehaviorDelegate<GitHubApi>,
        private var gitHubRepoList: GitHubRepoList
    ) : GitHubApi {
        override fun getRepo(q: String): Single<Result<GitHubRepoList>> {
            return delegate.returningResponse(gitHubRepoList).getRepo("")
        }
    }

    private fun createRepository(service: GitHubApi) {
        onlineRepository = TellerOnlineRepository(mDB, RetroFitRepoService(service), ResponseProcessor(ApplicationProvider.getApplicationContext<Context>(), AppActivityLogger(), MoshiJsonAdapter()))
        gitHubViewModelInjected = GitHubViewModelInjected(onlineRepository, mday)
    }

    private var mday = DayInformation()
    private var requirements = TellerOnlineRepository.GetReposRequirement(mday)
    private lateinit var retrofit: Retrofit
    private lateinit var networkBehavior: NetworkBehavior
    private lateinit var mockRetrofit: MockRetrofit
    private lateinit var delegate: BehaviorDelegate<GitHubApi>
    private lateinit var mGitHubApi: GitHubApi

    private lateinit var mDB: GitHubRepoDataBase
    private lateinit var gitHubRepoDAO: GitHubRepoDAO

    private lateinit var onlineRepository: TellerOnlineRepository

    private lateinit var gitHubViewModelInjected: GitHubViewModelInjected

    @Before
    fun setup() {

        //Set up mock database
        val context = ApplicationProvider.getApplicationContext<Context>()
        mDB = Room.inMemoryDatabaseBuilder(context, GitHubRepoDataBase::class.java).allowMainThreadQueries().fallbackToDestructiveMigration().build()
        gitHubRepoDAO = mDB.gitHubRepoDAO()
        //Set up mock retrofit
        retrofit = Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(MoshiConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .build()
        networkBehavior = NetworkBehavior.create()
        mockRetrofit = MockRetrofit.Builder(retrofit)
            .networkBehavior(networkBehavior)
            .build()

        delegate = mockRetrofit.create(GitHubApi::class.java)
    }

    @After
    fun close() {
        mDB.clearAllTables()
        onlineRepository.dispose()
    }

    @Test
    fun testInsertReplace() {
        mGitHubApi = MockGitHubApiFail(delegate)
        createRepository(mGitHubApi)
        gitHubRepoDAO.insert(GitHubRepo("title", GitHubRepoOwner("fakeName", ""), 1, null, 1))
        gitHubRepoDAO.insert(GitHubRepo("title", GitHubRepoOwner("fakeName", ""), 10, "fake description", 1))
        assertEquals(1, gitHubRepoDAO.getRepoCount().blockingGet())
    }

    @Test
    fun testOnlineRepositoryFetchSuccess() {
        changeNetworkBehaviour(0)
        val mGithubReposList = GitHubRepoList(generateRepoList())
        mGitHubApi = MockGitHubApiSuccess(delegate, mGithubReposList)
        createRepository(mGitHubApi)
        val test = onlineRepository.fetchFreshCache(requirements).blockingGet()
        assertEquals(true, test.isSuccessful())
        assertEquals(mGithubReposList, test.response)

    }

    @Test
    fun testOnlineRepositoryFetchSuccessErrorCodeJsonError() {
        changeNetworkBehaviour(0)
        mGitHubApi = MockGitHubApiResponseFail(delegate)
        createRepository(mGitHubApi)
        val test = onlineRepository.fetchFreshCache(requirements).blockingGet()
        assertEquals(false, test.isSuccessful())
    }

    @Test
    fun testOnlineRepositoryFetchSuccessErrorCodeUnhandledError() {
        changeNetworkBehaviour(0)
        mGitHubApi = MockGitHubApiResponseFail500Error(delegate)
        createRepository(mGitHubApi)
        val test = onlineRepository.fetchFreshCache(requirements).blockingGet()
        assertEquals(false, test.isSuccessful())
        assertEquals(true, (test.failure is UnhandledError))
    }

    @Test
    fun testOnlineRepositoryFetchFailureIOException() {
        changeNetworkBehaviour(100)
        mGitHubApi = MockGitHubApiFail(delegate)
        createRepository(mGitHubApi)
        val test = onlineRepository.fetchFreshCache(requirements).blockingGet()
        assertEquals(false, test.isSuccessful())
        assertEquals(true, test.failure is NetworkError)
    }

    @Test
    fun testOnlineRepositoryEmptyCache() {
        changeNetworkBehaviour(0)
        val mGithubReposList = GitHubRepoList(generateRepoList())
        mGitHubApi = MockGitHubApiSuccess(delegate, mGithubReposList)
        createRepository(mGitHubApi)

        val setValues = OnlineRepository.Testing.initState(onlineRepository,requirements){
            cacheEmpty {
                cacheNotTooOld()
            }
        }
        val expectedValue = OnlineCacheState.Testing.cache<List<GitHubRepo>>(requirements,setValues.lastFetched!!)

        onlineRepository.requirements = requirements
        assertThat(gitHubViewModelInjected.getAllReposObservable().blockingFirst()).isEqualTo(expectedValue)
    }

    @Test
    fun testOnlineRepositoryEmptyCacheTooOld(){
        changeNetworkBehaviour(0)
        val mGithubReposList = GitHubRepoList(generateRepoList())
        mGitHubApi = MockGitHubApiSuccess(delegate, mGithubReposList)
        createRepository(mGitHubApi)

        val setValue = OnlineRepository.Testing.initState(onlineRepository,requirements){
            cacheEmpty {
                cacheTooOld()
            }
        }
        val expectedValue = OnlineCacheState.Testing.cache<List<GitHubRepo>>(requirements,setValue.lastFetched!!){
            fetching()
        }

        onlineRepository.requirements = requirements

        assertThat(gitHubViewModelInjected.getAllReposObservable().blockingFirst()).isEqualTo(expectedValue)
    }

    @Test
    fun testOnlineRepositoryNonEmptyCacheTooOld(){
        changeNetworkBehaviour(0)
        val mGithubReposList = GitHubRepoList(generateRepoList())
        mGitHubApi = MockGitHubApiSuccess(delegate, mGithubReposList)
        createRepository(mGitHubApi)

        val setValues = OnlineRepository.Testing.initState(onlineRepository,requirements){
            cache(GitHubRepoList(generateRepoList())){
                cacheTooOld()
            }
        }

        val expectedValue = OnlineCacheState.Testing.cache<List<GitHubRepo>>(requirements,setValues.lastFetched!!){
            fetching()
        }

        onlineRepository.requirements = requirements
        assertThat(gitHubViewModelInjected.getAllReposObservable().blockingFirst()).isEqualTo(expectedValue)
    }

    @Test
    fun testOnlineRepositoryCacheTooOldGetsReplaced(){
        changeNetworkBehaviour(0)
        val mGithubReposList = GitHubRepoList(generateRepoList())
        mGitHubApi = MockGitHubApiSuccess(delegate, mGithubReposList)
        createRepository(mGitHubApi)

        val fakeData = GitHubRepoList(listOf(GitHubRepo("title", GitHubRepoOwner("Owner","url"),9,"desc",18),GitHubRepo("title2", GitHubRepoOwner("Owner2","url2"),12,"desc2",15)))

        val setValues = OnlineRepository.Testing.initState(onlineRepository,requirements){
            cache(fakeData){
                cacheTooOld()
            }
        }

        val expectedValue = OnlineCacheState.Testing.cache<List<GitHubRepo>>(requirements,setValues.lastFetched!!){
            fetching()
        }

        assertThat(gitHubRepoDAO.getRepoCount().blockingGet()).isEqualTo(2)

        onlineRepository.requirements = requirements

        val repoCacheStateInitial = gitHubViewModelInjected.getAllReposObservable().blockingFirst()
        Thread.sleep(150)
        val repoCacheStateGetCache = gitHubViewModelInjected.getAllReposObservable().blockingFirst()

        val expectedValue2 = OnlineCacheState.Testing.cache<List<GitHubRepo>>(requirements,repoCacheStateGetCache.lastSuccessfulFetch!!){
            cache(generateRepoList().sortedByDescending { it.getStargazers_count() })
        }

        assertThat(repoCacheStateInitial).isEqualTo(expectedValue)
        assertThat(repoCacheStateGetCache).isEqualTo(expectedValue2)

        assertThat(gitHubRepoDAO.getRepoCount().blockingGet()).isEqualTo(5)
    }

}

class TellerInitRule : ExternalResource() {

    override fun before() {
        super.before()
        InstrumentationRegistry.getInstrumentation().targetContext.applicationContext.let { application ->
            val sharedPrefs = application.getSharedPreferences("teller-testing", Context.MODE_PRIVATE)
            Teller.initTesting(sharedPrefs)
            Teller.shared.clear()
        }
    }
}
