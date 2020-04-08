package com.example.androidtraining.integration_test

import android.content.SharedPreferences
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.androidtraining.GitHubViewModel
import com.example.androidtraining.database.GitHubIssueDAO
import com.example.androidtraining.di_test.DiGraphRule
import com.example.androidtraining.service.GitHubIssue
import com.example.androidtraining.service.error.ServerErrorException
import com.example.androidtraining.ui_test.ActivityTestsInterface
import com.example.androidtraining.ui_test.MockWebServer
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class TellerIssueTests: ActivityTestsInterface() {
    @Inject
    lateinit var mockWebServer: MockWebServer

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var viewModel: GitHubViewModel
    lateinit var dao: GitHubIssueDAO

    @get:Rule
    val diGraph = DiGraphRule()

    @Before
    fun setup() {
        diGraph.graph.inject(this)
        viewModel = viewModelFactory.create(GitHubViewModel::class.java)
        dao = viewModel.database.gitHubIssueDAO()
    }

    @After
    fun close() {
        sharedPreferences.edit().clear().commit()
        viewModel.database.clearAllTables()
    }

    @Test
    fun test_fetchFreshCache_success(){
        val body = createIssueList(10).toTypedArray()
        mockWebServer.queue(200,body)

        val result = viewModel.repositoryIssue.fetchFreshCache(viewModel.repositoryIssue.requirements!!).blockingGet()
        assertThat(result.isSuccessful()).isTrue()
        assertThat(result.response?.size).isEqualTo(10)
    }

    fun test_fetchFreshCache_failure(){
        val body = arrayOf<GitHubIssue>()
        mockWebServer.queue(502,body)

        val result = viewModel.repositoryIssue.fetchFreshCache(viewModel.repositoryIssue.requirements!!)
            .blockingGet()
        assertThat(result.isFailure()).isTrue()
        assertThat(result.failure).isInstanceOf(ServerErrorException::class.java)
    }

    @Test
    fun test_saveCache_size30(){
        val list = createIssueList(30)
        runBlocking {
            viewModel.repositoryIssue.saveCache(list,viewModel.repositoryIssue.requirements!!)
        }
        assertThat(dao.getIssueCount()).isEqualTo(30)
    }

    @Test
    fun test_saveCache_size100(){
        val list = createIssueList(100)
        runBlocking {
            viewModel.repositoryIssue.saveCache(list,viewModel.repositoryIssue.requirements!!)
        }
        assertThat(dao.getIssueCount()).isEqualTo(100)
    }

    @Test
    fun test_saveCache_size0(){
        val list = listOf<GitHubIssue>()
        runBlocking {
            viewModel.repositoryIssue.saveCache(list,viewModel.repositoryIssue.requirements!!)
        }
        assertThat(dao.getIssueCount()).isEqualTo(0)
    }
}