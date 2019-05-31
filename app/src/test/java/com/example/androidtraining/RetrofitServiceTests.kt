package com.example.androidtraining

import kotlinx.coroutines.runBlocking
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
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

@RunWith(MockitoJUnitRunner::class)
class RetrofitServiceTests {

    class MockGitHubApiFail(var delegate: BehaviorDelegate<GitHubApi>) : GitHubApi {
        val mockFail = IOException()
        var failure: Call<GitHubRepoList> = Calls.failure(mockFail)
        override fun getRepo(q: String): Call<GitHubRepoList> {
            return delegate.returning(failure).getRepo("")
        }

    }

    class MockGitHubApiResponseFail(var delegate: BehaviorDelegate<GitHubApi>) : GitHubApi {
        var failure: Call<GitHubRepoList> = Calls.response(Response.error(400, ResponseBody.create(MediaType.parse(""),"Nothing Found")))
        override fun getRepo(q: String): Call<GitHubRepoList> {
            return delegate.returning(failure).getRepo("")
        }

    }

    class MockGitHubApiSuccess(var delegate: BehaviorDelegate<GitHubApi>, var gitHubRepoList: GitHubRepoList) : GitHubApi{
        override fun getRepo(q: String): Call<GitHubRepoList> {
            return delegate.returningResponse(gitHubRepoList).getRepo("")
        }

    }

    fun generateRepoList(): List<GitHubRepo> {
        val test1 = GitHubRepo("test1", GitHubRepoOwner("testLogin1"), 10, null, 1)
        val test2 = (GitHubRepo("test2", GitHubRepoOwner("testLogin2"), 1, "testDesc2", 2))
        val test3 = (GitHubRepo("test3", GitHubRepoOwner("testLogin3"), 89, "testDesc3", 3))
        val test4 = (GitHubRepo("test4", GitHubRepoOwner("testLogin4"), 53, null, 4))
        val test5 = (GitHubRepo("test5", GitHubRepoOwner("testLogin5"), 27, "testDesc5", 5))
        return listOf(test1,test2,test3,test4,test5)
    }


    lateinit var retrofit: Retrofit
    lateinit var networkBehavior: NetworkBehavior
    lateinit var mockRetrofit: MockRetrofit
    lateinit var delegate: BehaviorDelegate<GitHubApi>
    lateinit var testList: List<GitHubRepo>
    lateinit var mRetrofitService: RetroFitService


    @Before
    fun setup(){
        testList = generateRepoList()

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

    @Test
    fun `getRepos execute is Successful and Response is successful`() {
        changeNetworkBehaviour(0)
        val testGithubRepoList = GitHubRepoList(testList)
        mRetrofitService = RetroFitService(MockGitHubApiSuccess(delegate,testGithubRepoList))
        runBlocking {
            val completed = mRetrofitService.getRepos("1993-03-10")
            assertEquals(testGithubRepoList, completed)
        }
    }

    @Test
    fun `getRepos execute is Successful and Response is unsuccessful`() {
        changeNetworkBehaviour(0)
        val mRetrofitService = RetroFitService(MockGitHubApiResponseFail(delegate))
        runBlocking {
            assertEquals(mRetrofitService.getRepos("1998-03-10"), null)
        }
    }

    @Test
    fun `getRepos execute is unsuccessful`() {
        changeNetworkBehaviour(100)
        val mRetrofitService = RetroFitService(MockGitHubApiFail(delegate))
        runBlocking {
            assertEquals(mRetrofitService.getRepos("1998-03-10"), null)
        }
    }

    private fun changeNetworkBehaviour(failPercent: Int){
        networkBehavior.setDelay(0, TimeUnit.SECONDS)
        networkBehavior.setVariancePercent(0)
        networkBehavior.setFailurePercent(failPercent)
    }

}