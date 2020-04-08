package com.example.androidtraining.integration_test

import android.content.SharedPreferences
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.androidtraining.GitHubViewModel
import com.example.androidtraining.di_test.DiGraphRule
import com.example.androidtraining.ui_test.ActivityTestsInterface
import com.example.androidtraining.ui_test.MockWebServer
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class TellerRepoTests: ActivityTestsInterface() {
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
    fun close() {
        sharedPreferences.edit().clear().commit()
        viewmodel.database.clearAllTables()
    }

    @Test
    fun test_fetchFreshCache_success(){
        val user = testGitHubUser()
        val body = testGitHubRepoList(listOf(testGitHubRepo(user = user)))
        mockWebServer.queue(200,body)

        val result = viewmodel.repositoryRepo.fetchFreshCache(viewmodel.repositoryRepo.requirements!!).blockingGet()
        assertThat(result.isSuccessful()).isTrue()
        assertThat(result.response).isEqualTo(body)
    }

    @Test
    fun test_fetchFreshCache_failure(){
        val user = testGitHubUser()
        val body = testGitHubRepoList(listOf(testGitHubRepo(user = user)))
        mockWebServer.queue(502,body)

        val result = viewmodel.repositoryRepo.fetchFreshCache(viewmodel.repositoryRepo.requirements!!).blockingGet()
        assertThat(result.isFailure()).isTrue()
    }

    @Test
    fun test_saveCache(){
        val user = testGitHubUser()
        val body = testGitHubRepoList(listOf(testGitHubRepo(user = user),testGitHubRepo(user = user,id = 2),testGitHubRepo(user = user,id = 3)))

        viewmodel.repositoryRepo.saveCache(body,viewmodel.repositoryRepo.requirements!!)
        assertThat(viewmodel.database.gitHubRepoDAO().getRepoCount().blockingGet()).isEqualTo(3)
    }
}