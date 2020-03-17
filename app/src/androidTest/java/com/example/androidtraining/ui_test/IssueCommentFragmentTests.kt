package com.example.androidtraining.ui_test

import android.content.SharedPreferences
import androidx.lifecycle.ViewModelProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.example.androidtraining.GitHubViewModel
import com.example.androidtraining.R
import com.example.androidtraining.ui_test.di_test.DiGraphRule
import com.levibostian.recyclerviewmatcher.RecyclerViewMatcher.Companion.recyclerViewWithId
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

class IssueCommentFragmentTests : ActivityTestsInterface() {
    @Inject
    lateinit var mockWebServer: MockWebServer

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @get:Rule
    val diGraph = DiGraphRule()

    lateinit var vm: GitHubViewModel

    @Before
    fun setup() {
        diGraph.graph.inject(this)
        sharedPreferences.edit().apply {
            putString("access_token", "test token")
            putString("user", "TestUser")
        }.commit()
        vm = viewModelFactory.create(GitHubViewModel::class.java)
    }

    fun goToIssueFrag() {
        onView(withId(R.id.login_dest)).perform(click())
    }

    fun goToIssueComment(position: Int){
        onView(recyclerViewWithId(R.id.IssueList).itemViewAtIndex(position)).perform(
            click())
    }

    @Test
    fun testIssueCommentDisplaysFromCache() {
        setTellerStateEmpty(vm, issue = false, comment = false)
        setTellerIssue(vm, listOf(testRepoIssue()))
        setTellerIssueCommentReq(vm)
        setTellerIssueComment(vm, listOf(testGithubIssueComment(),testGithubIssueComment(commentId = 2,body = "Test Comment Body 2")))
        //mockWebServer.queue(200, arrayOf())
        launchMainActivity()
        goToIssueFrag()
        goToIssueComment(0)
        runBlocking { delay(50000) }
    }
}