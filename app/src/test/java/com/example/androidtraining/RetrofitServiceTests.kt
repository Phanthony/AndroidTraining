package com.example.androidtraining

import com.example.androidtraining.database.GitHubRepo
import com.example.androidtraining.database.GitHubRepoList
import com.example.androidtraining.database.GitHubUser
import com.example.androidtraining.service.*
import com.example.androidtraining.service.error.*
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import okhttp3.Headers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations.initMocks
import org.mockito.junit.MockitoJUnitRunner
import retrofit2.Response
import retrofit2.adapter.rxjava2.Result

@RunWith(MockitoJUnitRunner::class)
class RetrofitServiceTests {
    @Mock
    lateinit var mGitHubApi: GitHubApi

    @Mock
    lateinit var mDevApi: DevApi

    @Mock
    lateinit var mResponseProcessor: ResponseProcessor

    lateinit var mRetrofit: RetrofitService

    @Before
    fun setup() {
        initMocks(this)
        mRetrofit = RetrofitService(mGitHubApi, mDevApi, mResponseProcessor)
    }

    @Test
    fun `test loginToGithub success`() {
        val body = GitHubLoginResult("Success", GitHubLoginResponse("fake_token", "fake_url"))
        val response = Result.response(Response.success(200, body))
        whenever(mDevApi.loginGithub(any())).thenReturn(Single.just(response))
        whenever(mResponseProcessor.process(any<Result<GitHubLoginResult>>(), any())).thenAnswer {
            ResponseProcessor.ProcessedResult(
                null,
                null,
                body
            )
        }
        val res = mRetrofit.loginToGithub("TestUser", "TestPass").blockingGet()
        verify(mDevApi).loginGithub(
            AuthMobileRequestBody(
                listOf("repo"),
                "4cc5aa575096c8bcb036",
                "TestUser",
                "TestPass"
            )
        )
        assertThat(res.isSuccess).isTrue()
        assertThat(res.getOrNull()!!).isEqualTo(body)
    }

    @Test
    fun `test loginToGithub failure Unauthorized`(){
        val error = UnauthorizedException("Bad Authorization")
        val response = Result.error<GitHubLoginResult>(error)
        whenever(mDevApi.loginGithub(any())).thenReturn(Single.just(response))
        whenever(mResponseProcessor.process(any<Result<GitHubLoginResult>>(), any())).thenAnswer {
            ResponseProcessor.ProcessedResult(error,"error",null)
        }
        val res = mRetrofit.loginToGithub("TestUser", "TestPass").blockingGet()
        verify(mDevApi).loginGithub(
            AuthMobileRequestBody(
                listOf("repo"),
                "4cc5aa575096c8bcb036",
                "TestUser",
                "TestPass"
            )
        )
        assertThat(res.isFailure).isTrue()
        assertThat(res.exceptionOrNull()).isEqualTo(error)
    }

    @Test
    fun `test loginToGithub failure NetworkConnectionIssue`(){
        val error = NetworkConnectionIssueException("")
        val response = Result.error<GitHubLoginResult>(error)
        whenever(mDevApi.loginGithub(any())).thenReturn(Single.just(response))
        whenever(mResponseProcessor.process(any<Result<GitHubLoginResult>>(), any())).thenAnswer {
            ResponseProcessor.ProcessedResult(error,"error",null)
        }
        val res = mRetrofit.loginToGithub("TestUser", "TestPass").blockingGet()
        verify(mDevApi).loginGithub(
            AuthMobileRequestBody(
                listOf("repo"),
                "4cc5aa575096c8bcb036",
                "TestUser",
                "TestPass"
            )
        )
        assertThat(res.isFailure).isTrue()
        assertThat(res.exceptionOrNull()).isEqualTo(error)
    }

    fun `test loginToGithub failure NoInternetConnection`(){
        val error = NoInternetConnectionException("")
        val response = Result.error<GitHubLoginResult>(error)
        whenever(mDevApi.loginGithub(any())).thenReturn(Single.just(response))
        whenever(mResponseProcessor.process(any<Result<GitHubLoginResult>>(), any())).thenAnswer {
            ResponseProcessor.ProcessedResult(error,"error",null)
        }
        val res = mRetrofit.loginToGithub("TestUser", "TestPass").blockingGet()
        verify(mDevApi).loginGithub(
            AuthMobileRequestBody(
                listOf("repo"),
                "4cc5aa575096c8bcb036",
                "TestUser",
                "TestPass"
            )
        )
        assertThat(res.isFailure).isTrue()
        assertThat(res.exceptionOrNull()).isEqualTo(error)
    }

    @Test
    fun `test loginToGithub failure ServerError`(){
        val error = ServerErrorException("")
        val response = Result.error<GitHubLoginResult>(error)
        whenever(mDevApi.loginGithub(any())).thenReturn(Single.just(response))
        whenever(mResponseProcessor.process(any<Result<GitHubLoginResult>>(), any())).thenAnswer {
            ResponseProcessor.ProcessedResult(error,"error",null)
        }
        val res = mRetrofit.loginToGithub("TestUser", "TestPass").blockingGet()
        verify(mDevApi).loginGithub(
            AuthMobileRequestBody(
                listOf("repo"),
                "4cc5aa575096c8bcb036",
                "TestUser",
                "TestPass"
            )
        )
        assertThat(res.isFailure).isTrue()
        assertThat(res.exceptionOrNull()).isEqualTo(error)
    }

    @Test
    fun `test loginToGithub failure UnhandledHttpResult`(){
        val error = UnhandledHttpResultException("")
        val response = Result.error<GitHubLoginResult>(error)
        whenever(mDevApi.loginGithub(any())).thenReturn(Single.just(response))
        whenever(mResponseProcessor.process(any<Result<GitHubLoginResult>>(), any())).thenAnswer {
            ResponseProcessor.ProcessedResult(error,"error",null)
        }
        val res = mRetrofit.loginToGithub("TestUser", "TestPass").blockingGet()
        verify(mDevApi).loginGithub(
            AuthMobileRequestBody(
                listOf("repo"),
                "4cc5aa575096c8bcb036",
                "TestUser",
                "TestPass"
            )
        )
        assertThat(res.isFailure).isTrue()
        assertThat(res.exceptionOrNull()).isEqualTo(error)
    }

    @Test
    fun `test loginToGithub failure UserEnteredBadData`(){
        val error = UserEnteredBadDataResponseError("","")
        val response = Result.error<GitHubLoginResult>(error)
        whenever(mDevApi.loginGithub(any())).thenReturn(Single.just(response))
        whenever(mResponseProcessor.process(any<Result<GitHubLoginResult>>(), any())).thenAnswer {
            ResponseProcessor.ProcessedResult(error,"error",null)
        }
        val res = mRetrofit.loginToGithub("TestUser", "TestPass").blockingGet()
        verify(mDevApi).loginGithub(
            AuthMobileRequestBody(
                listOf("repo"),
                "4cc5aa575096c8bcb036",
                "TestUser",
                "TestPass"
            )
        )
        assertThat(res.isFailure).isTrue()
        assertThat(res.exceptionOrNull()).isEqualTo(error)
    }

    @Test
    fun `test getIssues success`(){
        val body = listOf(GitHubIssue(1,1,"open","TestIssue", GitHubUser("TestUser",""),1,"", GitHubRepo("TestRepo",GitHubUser("TestUser",""),1,"TestRepoDesc",1)))
        val response = Result.response(Response.success(200,body))
        whenever(mGitHubApi.getIssues()).thenReturn(Single.just(response))
        whenever(mResponseProcessor.process(any<Result<List<GitHubIssue>>>(), anyOrNull())).thenAnswer {
            ResponseProcessor.ProcessedResult(null,"success",body)
        }
        val res = mRetrofit.getIssues().blockingGet()
        verify(mGitHubApi).getIssues()
        assertThat(res.isSuccess).isTrue()
        assertThat(res.getOrNull()).isEqualTo(body)
    }

    @Test
    fun `test getIssues failure ServerError`(){
        val error = ServerErrorException("Server Error")
        val response = Result.error<List<GitHubIssue>>(error)
        whenever(mGitHubApi.getIssues()).thenReturn(Single.just(response))
        whenever(mResponseProcessor.process(any<Result<List<GitHubIssue>>>(), anyOrNull())).thenAnswer {
            ResponseProcessor.ProcessedResult(error,"error",null)
        }
        val res = mRetrofit.getIssues().blockingGet()
        verify(mGitHubApi).getIssues()
        assertThat(res.isFailure).isTrue()
        assertThat(res.exceptionOrNull()).isEqualTo(error)
    }

    fun `test getIssues failure NetworkConnectionIssue`(){
        val error = NetworkConnectionIssueException("")
        val response = Result.error<List<GitHubIssue>>(error)
        whenever(mGitHubApi.getIssues()).thenReturn(Single.just(response))
        whenever(mResponseProcessor.process(any<Result<List<GitHubIssue>>>(), anyOrNull())).thenAnswer {
            ResponseProcessor.ProcessedResult(error,"error",null)
        }
        val res = mRetrofit.getIssues().blockingGet()
        verify(mGitHubApi).getIssues()
        assertThat(res.isFailure).isTrue()
        assertThat(res.exceptionOrNull()).isEqualTo(error)
    }

    fun `test getIssues failure NoInternetConnection`(){
        val error = NoInternetConnectionException("")
        val response = Result.error<List<GitHubIssue>>(error)
        whenever(mGitHubApi.getIssues()).thenReturn(Single.just(response))
        whenever(mResponseProcessor.process(any<Result<List<GitHubIssue>>>(), anyOrNull())).thenAnswer {
            ResponseProcessor.ProcessedResult(error,"error",null)
        }
        val res = mRetrofit.getIssues().blockingGet()
        verify(mGitHubApi).getIssues()
        assertThat(res.isFailure).isTrue()
        assertThat(res.exceptionOrNull()).isEqualTo(error)
    }

    fun `test getIssues failure Unauthorized`(){
        val error = UnauthorizedException("")
        val response = Result.error<List<GitHubIssue>>(error)
        whenever(mGitHubApi.getIssues()).thenReturn(Single.just(response))
        whenever(mResponseProcessor.process(any<Result<List<GitHubIssue>>>(), anyOrNull())).thenAnswer {
            ResponseProcessor.ProcessedResult(error,"error",null)
        }
        val res = mRetrofit.getIssues().blockingGet()
        verify(mGitHubApi).getIssues()
        assertThat(res.isFailure).isTrue()
        assertThat(res.exceptionOrNull()).isEqualTo(error)
    }

    fun `test getIssues failure UnhandledHttpResult`(){
        val error = UnhandledHttpResultException("")
        val response = Result.error<List<GitHubIssue>>(error)
        whenever(mGitHubApi.getIssues()).thenReturn(Single.just(response))
        whenever(mResponseProcessor.process(any<Result<List<GitHubIssue>>>(), anyOrNull())).thenAnswer {
            ResponseProcessor.ProcessedResult(error,"error",null)
        }
        val res = mRetrofit.getIssues().blockingGet()
        verify(mGitHubApi).getIssues()
        assertThat(res.isFailure).isTrue()
        assertThat(res.exceptionOrNull()).isEqualTo(error)
    }

    @Test
    fun `test getRepos Success`(){
        val body = GitHubRepoList(listOf(GitHubRepo("TestRepo",GitHubUser("TestUser",""),1,"TestRepoDesc",1)))
        val response = Result.response(Response.success(200,body))
        whenever(mGitHubApi.getRepo(any())).thenReturn(Single.just(response))
        whenever(mResponseProcessor.process(any<Result<GitHubRepoList>>(), anyOrNull())).thenAnswer {
            ResponseProcessor.ProcessedResult(null,"success",body)
        }
        val res = mRetrofit.getRepos("test").blockingGet()
        verify(mGitHubApi).getRepo("created:%3Etest+language:kotlin+stars:%3E0")
        assertThat(res.isSuccess).isTrue()
        assertThat(res.getOrNull()).isEqualTo(body)
    }

    @Test
    fun `test getRepos Failure NetworkConnectionIssue`(){
        val error = NetworkConnectionIssueException("")
        val response = Result.error<GitHubRepoList>(error)
        whenever(mGitHubApi.getRepo(any())).thenReturn(Single.just(response))
        whenever(mResponseProcessor.process(any<Result<GitHubRepoList>>(), anyOrNull())).thenAnswer {
            ResponseProcessor.ProcessedResult(error,"error",null)
        }
        val res = mRetrofit.getRepos("test").blockingGet()
        verify(mGitHubApi).getRepo("created:%3Etest+language:kotlin+stars:%3E0")
        assertThat(res.isFailure).isTrue()
        assertThat(res.exceptionOrNull()).isEqualTo(error)
    }

    @Test
    fun `test getRepos Failure NoInternet`(){
        val error = NoInternetConnectionException("No Internet")
        val response = Result.error<GitHubRepoList>(error)
        whenever(mGitHubApi.getRepo(any())).thenReturn(Single.just(response))
        whenever(mResponseProcessor.process(any<Result<GitHubRepoList>>(), anyOrNull())).thenAnswer {
            ResponseProcessor.ProcessedResult(error,"error",null)
        }
        val res = mRetrofit.getRepos("test").blockingGet()
        verify(mGitHubApi).getRepo("created:%3Etest+language:kotlin+stars:%3E0")
        assertThat(res.isFailure).isTrue()
        assertThat(res.exceptionOrNull()).isEqualTo(error)
    }

    @Test
    fun `test getRepos Failure ServerError`(){
        val error = ServerErrorException("")
        val response = Result.error<GitHubRepoList>(error)
        whenever(mGitHubApi.getRepo(any())).thenReturn(Single.just(response))
        whenever(mResponseProcessor.process(any<Result<GitHubRepoList>>(), anyOrNull())).thenAnswer {
            ResponseProcessor.ProcessedResult(error,"error",null)
        }
        val res = mRetrofit.getRepos("test").blockingGet()
        verify(mGitHubApi).getRepo("created:%3Etest+language:kotlin+stars:%3E0")
        assertThat(res.isFailure).isTrue()
        assertThat(res.exceptionOrNull()).isEqualTo(error)
    }

    @Test
    fun `test getRepos Failure Unauthorized`(){
        val error = UnauthorizedException("")
        val response = Result.error<GitHubRepoList>(error)
        whenever(mGitHubApi.getRepo(any())).thenReturn(Single.just(response))
        whenever(mResponseProcessor.process(any<Result<GitHubRepoList>>(), anyOrNull())).thenAnswer {
            ResponseProcessor.ProcessedResult(error,"error",null)
        }
        val res = mRetrofit.getRepos("test").blockingGet()
        verify(mGitHubApi).getRepo("created:%3Etest+language:kotlin+stars:%3E0")
        assertThat(res.isFailure).isTrue()
        assertThat(res.exceptionOrNull()).isEqualTo(error)
    }

    @Test
    fun `test getRepos Failure UnhandledHttpResult`(){
        val error = UnhandledHttpResultException("")
        val response = Result.error<GitHubRepoList>(error)
        whenever(mGitHubApi.getRepo(any())).thenReturn(Single.just(response))
        whenever(mResponseProcessor.process(any<Result<GitHubRepoList>>(), anyOrNull())).thenAnswer {
            ResponseProcessor.ProcessedResult(error,"error",null)
        }
        val res = mRetrofit.getRepos("test").blockingGet()
        verify(mGitHubApi).getRepo("created:%3Etest+language:kotlin+stars:%3E0")
        assertThat(res.isFailure).isTrue()
        assertThat(res.exceptionOrNull()).isEqualTo(error)
    }

    @Test
    fun `test getIssueComments Success more pages to load`(){
        val headerMap = mutableMapOf(Pair("Link","rel=\"next"))
        val body = listOf(GitHubIssueComment(1, GitHubUser("TestUser",""),"",1))
        val response = Result.response(Response.success(body, Headers.of(headerMap)))
        whenever(mGitHubApi.getIssueComment(any(), any(), any())).thenReturn(Single.just(response))
        whenever(mResponseProcessor.process(any<Result<List<GitHubIssueComment>>>(), anyOrNull())).thenAnswer {
            ResponseProcessor.ProcessedResult(null,"success",body)
        }
        val res = mRetrofit.getIssueComments(1,"name","user",1).blockingGet()
        verify(mGitHubApi).getIssueComment("1","user","name")
        assertThat(res.result.isSuccess).isTrue()
        assertThat(res.result.getOrNull()).isEqualTo(body)
        assertThat(res.morePagesToLoad).isTrue()
    }

    @Test
    fun `test getIssueComments Success no more pages to load`(){
        val headerMap = mutableMapOf(Pair("Link",""))
        val body = listOf(GitHubIssueComment(1, GitHubUser("TestUser",""),"",1))
        val response = Result.response(Response.success(body, Headers.of(headerMap)))
        whenever(mGitHubApi.getIssueComment(any(), any(), any())).thenReturn(Single.just(response))
        whenever(mResponseProcessor.process(any<Result<List<GitHubIssueComment>>>(), anyOrNull())).thenAnswer {
            ResponseProcessor.ProcessedResult(null,"success",body)
        }
        val res = mRetrofit.getIssueComments(1,"name","user",1).blockingGet()
        verify(mGitHubApi).getIssueComment("1","user","name")
        assertThat(res.result.isSuccess).isTrue()
        assertThat(res.result.getOrNull()).isEqualTo(body)
        assertThat(res.morePagesToLoad).isFalse()
    }

    @Test
    fun `test getIssueComments NetworkConnectionIssue`(){
        val error = NetworkConnectionIssueException("")
        val response = Result.error<List<GitHubIssueComment>>(error)
        whenever(mGitHubApi.getIssueComment(any(), any(), any())).thenReturn(Single.just(response))
        whenever(mResponseProcessor.process(any<Result<List<GitHubIssueComment>>>(), anyOrNull())).thenAnswer {
            ResponseProcessor.ProcessedResult(error,"error",null)
        }
        val res = mRetrofit.getIssueComments(1,"name","user",1).blockingGet()
        verify(mGitHubApi).getIssueComment("1","user","name")
        assertThat(res.result.isFailure).isTrue()
        assertThat(res.result.exceptionOrNull()).isEqualTo(error)
        assertThat(res.morePagesToLoad).isFalse()
    }

    @Test
    fun `test getIssueComments NoInternetConnection`(){
        val error = NoInternetConnectionException("")
        val response = Result.error<List<GitHubIssueComment>>(error)
        whenever(mGitHubApi.getIssueComment(any(), any(), any())).thenReturn(Single.just(response))
        whenever(mResponseProcessor.process(any<Result<List<GitHubIssueComment>>>(), anyOrNull())).thenAnswer {
            ResponseProcessor.ProcessedResult(error,"error",null)
        }
        val res = mRetrofit.getIssueComments(1,"name","user",1).blockingGet()
        verify(mGitHubApi).getIssueComment("1","user","name")
        assertThat(res.result.isFailure).isTrue()
        assertThat(res.result.exceptionOrNull()).isEqualTo(error)
        assertThat(res.morePagesToLoad).isFalse()
    }

    @Test
    fun `test getIssueComments ServerError`(){
        val error = ServerErrorException("")
        val response = Result.error<List<GitHubIssueComment>>(error)
        whenever(mGitHubApi.getIssueComment(any(), any(), any())).thenReturn(Single.just(response))
        whenever(mResponseProcessor.process(any<Result<List<GitHubIssueComment>>>(), anyOrNull())).thenAnswer {
            ResponseProcessor.ProcessedResult(error,"error",null)
        }
        val res = mRetrofit.getIssueComments(1,"name","user",1).blockingGet()
        verify(mGitHubApi).getIssueComment("1","user","name")
        assertThat(res.result.isFailure).isTrue()
        assertThat(res.result.exceptionOrNull()).isEqualTo(error)
        assertThat(res.morePagesToLoad).isFalse()
    }

    @Test
    fun `test getIssueComments Unauthorized`(){
        val error = UnauthorizedException("")
        val response = Result.error<List<GitHubIssueComment>>(error)
        whenever(mGitHubApi.getIssueComment(any(), any(), any())).thenReturn(Single.just(response))
        whenever(mResponseProcessor.process(any<Result<List<GitHubIssueComment>>>(), anyOrNull())).thenAnswer {
            ResponseProcessor.ProcessedResult(error,"error",null)
        }
        val res = mRetrofit.getIssueComments(1,"name","user",1).blockingGet()
        verify(mGitHubApi).getIssueComment("1","user","name")
        assertThat(res.result.isFailure).isTrue()
        assertThat(res.result.exceptionOrNull()).isEqualTo(error)
        assertThat(res.morePagesToLoad).isFalse()
    }

    @Test
    fun `test getIssueComments UnhandledHttpResult`(){
        val error = UnhandledHttpResultException("error")
        val response = Result.error<List<GitHubIssueComment>>(error)
        whenever(mGitHubApi.getIssueComment(any(), any(), any())).thenReturn(Single.just(response))
        whenever(mResponseProcessor.process(any<Result<List<GitHubIssueComment>>>(), anyOrNull())).thenAnswer {
            ResponseProcessor.ProcessedResult(error,"error",null)
        }
        val res = mRetrofit.getIssueComments(1,"name","user",1).blockingGet()
        verify(mGitHubApi).getIssueComment("1","user","name")
        assertThat(res.result.isFailure).isTrue()
        assertThat(res.result.exceptionOrNull()).isEqualTo(error)
        assertThat(res.morePagesToLoad).isFalse()
    }

}