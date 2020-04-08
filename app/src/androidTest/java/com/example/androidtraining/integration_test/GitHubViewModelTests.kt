package com.example.androidtraining.integration_test

import android.content.SharedPreferences
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.androidtraining.GitHubViewModel
import com.example.androidtraining.di_test.DiGraphRule
import com.example.androidtraining.service.GitHubLoginResponse
import com.example.androidtraining.service.GitHubLoginResult
import com.example.androidtraining.service.error.ServerErrorException
import com.example.androidtraining.service.error.UserEnteredBadDataResponseError
import com.example.androidtraining.ui_test.MockWebServer
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class GitHubViewModelTests {
    @Inject
    lateinit var mockWebServer: MockWebServer

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var viewmodel: GitHubViewModel

    @get:Rule
    val diGraph = DiGraphRule()

    @Before
    fun setup() {
        diGraph.graph.inject(this)
        viewmodel = viewModelFactory.create(GitHubViewModel::class.java)
    }

    @After
    fun close(){
        sharedPreferences.edit().clear().commit()
    }

    @Test
    fun test_loginToGithub_Success(){
        val body = GitHubLoginResult("message", GitHubLoginResponse("apikey","url"))
        mockWebServer.queue(200, body)

        val loginResult = viewmodel.loginToGithub("TestPass","TestUser").blockingGet()
        assertThat(loginResult.isSuccess).isTrue()
        assertThat(loginResult.getOrNull()).isEqualTo(body)
    }

    @Test
    fun test_LoginToGithub_Fail_BadData(){
        val body = UserEnteredBadDataResponseError("wrong password","error")
        mockWebServer.queue(401, body)

        val loginResult = viewmodel.loginToGithub("TestPass","TestUser").blockingGet()
        assertThat(loginResult.isFailure).isTrue()
        assertThat(loginResult.exceptionOrNull()).isInstanceOf(UserEnteredBadDataResponseError::class.java)
    }

    @Test
    fun test_loginToGithub_Fail_Other(){
        val error = ServerErrorException("error")
        mockWebServer.queue(502,error)

        val loginResult = viewmodel.loginToGithub("TestPass","TestUser").blockingGet()
        assertThat(loginResult.isFailure).isTrue()
        assertThat(loginResult.exceptionOrNull()).isInstanceOf(ServerErrorException::class.java)
    }

}