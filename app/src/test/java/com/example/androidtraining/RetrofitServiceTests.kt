package com.example.androidtraining

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
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
import retrofit2.adapter.rxjava2.Result
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.mock.BehaviorDelegate
import retrofit2.mock.Calls
import retrofit2.mock.MockRetrofit
import retrofit2.mock.NetworkBehavior
import java.io.IOException
import java.util.concurrent.TimeUnit

@RunWith(MockitoJUnitRunner::class)
class RetrofitServiceTests {

    class MockGitHubApiFail(private var delegate: BehaviorDelegate<GitHubApi>) : GitHubApi {
        private val mockFail = IOException()
        private var failure: Call<GitHubRepoList> = Calls.failure(mockFail)
        override fun getRepo(q: String): Single<Result<GitHubRepoList>> {
            return delegate.returning(failure).getRepo("")
        }
    }

    class MockGitHubApiResponseFail(private var delegate: BehaviorDelegate<GitHubApi>) : GitHubApi {
        private var failure: Call<GitHubRepoList> = Calls.response(Response.error(400, ResponseBody.create(MediaType.parse(""),"Nothing Found")))
        override fun getRepo(q: String): Single<Result<GitHubRepoList>> {
            return delegate.returning(failure).getRepo("")
        }
    }

    class MockGitHubApiSuccess(private var delegate: BehaviorDelegate<GitHubApi>, private var gitHubRepoList: GitHubRepoList) : GitHubApi{
        override fun getRepo(q: String): Single<Result<GitHubRepoList>> {
            return delegate.returningResponse(gitHubRepoList).getRepo("")
        }
    }

    private fun generateRepoList(): List<GitHubRepo> {
        val test1 = GitHubRepo("test1", GitHubRepoOwner("testLogin1",""), 10, null, 1)
        val test2 = (GitHubRepo("test2", GitHubRepoOwner("testLogin2",""), 1, "testDesc2", 2))
        val test3 = (GitHubRepo("test3", GitHubRepoOwner("testLogin3",""), 89, "testDesc3", 3))
        val test4 = (GitHubRepo("test4", GitHubRepoOwner("testLogin4",""), 53, null, 4))
        val test5 = (GitHubRepo("test5", GitHubRepoOwner("testLogin5",""), 27, "testDesc5", 5))
        return listOf(test1,test2,test3,test4,test5)
    }


    private lateinit var retrofit: Retrofit
    private lateinit var networkBehavior: NetworkBehavior
    private lateinit var mockRetrofit: MockRetrofit
    private lateinit var delegate: BehaviorDelegate<GitHubApi>
    private lateinit var testList: List<GitHubRepo>
    private lateinit var mRetrofitRepoService: RetroFitRepoService


    @Before
    fun setup(){

        testList = generateRepoList()

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

    @Test
    fun `getRepos execute is Successful and Response is successful`() {
        changeNetworkBehaviour(0)
        val testGithubRepoList = GitHubRepoList(testList)
        mRetrofitRepoService = RetroFitRepoService(MockGitHubApiSuccess(delegate,testGithubRepoList))
        val completed = mRetrofitRepoService.getRepos("1993-03-10").blockingGet()
        assertEquals(completed.response()!!.body(),testGithubRepoList)
    }

    @Test
    fun `getRepos execute is Successful and Response is unsuccessful`() {
        changeNetworkBehaviour(0)
        val mRetrofitService = RetroFitRepoService(MockGitHubApiResponseFail(delegate))
        val temp = mRetrofitService.getRepos("1998-03-10").blockingGet()
        assertEquals(temp.isError,false)
        assertEquals(temp.response()!!.code(),400)
    }

    @Test
    fun `getRepos execute is unsuccessful`() {
        changeNetworkBehaviour(100)
        val mRetrofitService = RetroFitRepoService(MockGitHubApiFail(delegate))
        val temp = mRetrofitService.getRepos("1998-03-10").blockingGet()
        assertEquals(temp.isError,true)
    }

    private fun changeNetworkBehaviour(failPercent: Int){
        networkBehavior.setDelay(0, TimeUnit.SECONDS)
        networkBehavior.setVariancePercent(0)
        networkBehavior.setFailurePercent(failPercent)
    }

}