package com.example.androidtraining.integration_test

import android.content.SharedPreferences
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.androidtraining.GitHubViewModel
import com.example.androidtraining.database.GitHubIssueCommentDAO
import com.example.androidtraining.database.teller.TellerIssueCommentsOnlineRepository
import com.example.androidtraining.di_test.DiGraphRule
import com.example.androidtraining.service.GitHubIssueComment
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
class TellerIssueCommentTests : ActivityTestsInterface() {
    @Inject
    lateinit var mockWebServer: MockWebServer

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var viewModel: GitHubViewModel
    lateinit var dao: GitHubIssueCommentDAO

    @get:Rule
    val diGraph = DiGraphRule()

    @Before
    fun setup() {
        diGraph.graph.inject(this)
        viewModel = viewModelFactory.create(GitHubViewModel::class.java)
        dao = viewModel.database.gitHubIssueCommentDAO()
    }

    @After
    fun close() {
        sharedPreferences.edit().clear().commit()
        viewModel.database.clearAllTables()
    }

    @Test
    fun test_deleteOldCache_size40_persist() {
        mockWebServer.queue(200, arrayOf())
        viewModel.repositoryIssueComment.requirements =
            TellerIssueCommentsOnlineRepository.GetCommentRequirement(0, "Test", "TestUser", 0)
        val req = viewModel.repositoryIssueComment.requirements!!
        val list = createCommentList(40, 0)
        runBlocking {
            for (i in list) {
                dao.insertComment(i)
            }
        }
        viewModel.repositoryIssueComment.deleteOldCache(req, true).blockingGet()
        assertThat(dao.getCommentCount()).isEqualTo(30)
    }

    @Test
    fun test_deleteOldCache_size40_no_persist() {
        mockWebServer.queue(200, arrayOf())
        val req =
            TellerIssueCommentsOnlineRepository.GetCommentRequirement(0, "RepoName", "TestUser", 0)
        viewModel.repositoryIssueComment.requirements = req
        val list = createCommentList(40, 0)
        runBlocking {
            for (i in list) {
                dao.insertComment(i)
            }
        }
        viewModel.repositoryIssueComment.deleteOldCache(req, false).blockingGet()
        assertThat(dao.getCommentCount()).isEqualTo(0)
    }

    @Test
    fun test_deleteOldCache_size20_persist() {
        mockWebServer.queue(200, arrayOf())
        val req =
            TellerIssueCommentsOnlineRepository.GetCommentRequirement(0, "RepoName", "TestUser", 0)
        viewModel.repositoryIssueComment.requirements = req
        val list = createCommentList(20, 0)
        runBlocking {
            for (i in list) {
                dao.insertComment(i)
            }
        }
        viewModel.repositoryIssueComment.deleteOldCache(req, true).blockingGet()
        assertThat(dao.getCommentCount()).isEqualTo(20)
    }

    @Test
    fun test_deleteOldCache_size20_no_persist() {
        mockWebServer.queue(200, arrayOf())
        val req =
            TellerIssueCommentsOnlineRepository.GetCommentRequirement(0, "RepoName", "TestUser", 0)
        viewModel.repositoryIssueComment.requirements = req
        val list = createCommentList(20, 0)
        runBlocking {
            for (i in list) {
                dao.insertComment(i)
            }
        }
        viewModel.repositoryIssueComment.deleteOldCache(req, false).blockingGet()
        assertThat(dao.getCommentCount()).isEqualTo(0)
    }

    @Test
    fun test_deleteOldCache_size30_persist() {
        mockWebServer.queue(200, arrayOf())
        val req =
            TellerIssueCommentsOnlineRepository.GetCommentRequirement(0, "RepoName", "TestUser", 0)
        viewModel.repositoryIssueComment.requirements = req
        val list = createCommentList(30, 0)
        runBlocking {
            for (i in list) {
                dao.insertComment(i)
            }
        }
        viewModel.repositoryIssueComment.deleteOldCache(req, true).blockingGet()
        assertThat(dao.getCommentCount()).isEqualTo(30)
    }

    @Test
    fun test_deleteOldCache_size30_no_persist() {
        mockWebServer.queue(200, arrayOf())
        val req =
            TellerIssueCommentsOnlineRepository.GetCommentRequirement(0, "RepoName", "TestUser", 0)
        viewModel.repositoryIssueComment.requirements = req
        val list = createCommentList(30, 0)
        runBlocking {
            for (i in list) {
                dao.insertComment(i)
            }
        }
        viewModel.repositoryIssueComment.deleteOldCache(req, false).blockingGet()
        assertThat(dao.getCommentCount()).isEqualTo(0)
    }

    @Test
    fun test_saveCache_size20() {
        val list = createCommentList(20)
        runBlocking {
            viewModel.repositoryIssueComment.saveCache(
                list,
                viewModel.repositoryIssueComment.requirements!!,
                viewModel.repositoryIssueComment.pagingRequirements
            )
        }
        assertThat(dao.getCommentCount()).isEqualTo(20)
    }

    @Test
    fun test_saveCahce_size100(){
        val list = createCommentList(100)
        runBlocking {
            viewModel.repositoryIssueComment.saveCache(
                list,
                viewModel.repositoryIssueComment.requirements!!,
                viewModel.repositoryIssueComment.pagingRequirements
            )
        }
        assertThat(dao.getCommentCount()).isEqualTo(100)
    }

    @Test
    fun test_saveCahce_size0(){
        val list = listOf<GitHubIssueComment>()
        runBlocking {
            viewModel.repositoryIssueComment.saveCache(
                list,
                viewModel.repositoryIssueComment.requirements!!,
                viewModel.repositoryIssueComment.pagingRequirements
            )
        }
        assertThat(dao.getCommentCount()).isEqualTo(0)
    }




}