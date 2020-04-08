package com.example.androidtraining.ui_test

import android.content.SharedPreferences
import androidx.lifecycle.ViewModelProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeDown
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import com.example.androidtraining.GitHubViewModel
import com.example.androidtraining.R
import com.example.androidtraining.di_test.DiGraphRule
import com.levibostian.recyclerviewmatcher.RecyclerViewMatcher.Companion.recyclerViewWithId
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
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

    @After
    fun close(){
        vm.database.clearAllTables()
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
        onView(recyclerViewWithId(R.id.IssueCommentList).viewHolderViewAtPosition(0,R.id.IssueCommentBody)).check(matches(
            withText("Test Comment Body")))
        onView(recyclerViewWithId(R.id.IssueCommentList).viewHolderViewAtPosition(0,R.id.IssueCommentDesc)).check(matches(
            withText("TestUser")))
        onView(recyclerViewWithId(R.id.IssueCommentList).viewHolderViewAtPosition(0,R.id.IssueCommentUserImage)).check(matches(
            isDisplayed()))
        onView(recyclerViewWithId(R.id.IssueCommentList).viewHolderViewAtPosition(1,R.id.IssueCommentBody)).check(matches(
            withText("Test Comment Body 2")))
        onView(recyclerViewWithId(R.id.IssueCommentList).viewHolderViewAtPosition(1,R.id.IssueCommentDesc)).check(matches(
            withText("TestUser")))
        onView(recyclerViewWithId(R.id.IssueCommentList).viewHolderViewAtPosition(1,R.id.IssueCommentUserImage)).check(matches(
            isDisplayed()))
    }

    @Test
    fun testIssueCommentNoCache(){
        setTellerStateEmpty(vm,issue = false)
        setTellerIssue(vm, listOf(testRepoIssue()))
        setTellerIssueCommentReq(vm)
        //mockWebServer.queue(200, arrayOf())
        launchMainActivity()
        goToIssueFrag()
        goToIssueComment(0)
        onView(withId(R.id.nothing_to_show)).check(matches(isDisplayed()))
    }

    @Test
    fun testIssueCommentDisplaysMoreComments(){
        setTellerStateEmpty(vm,issue = false)
        setTellerIssue(vm, listOf(testRepoIssue()))
        setTellerIssueCommentReq(vm)
        setTellerIssueComment(vm, listOf(testGithubIssueComment()))
        //mockWebServer.queue(200, arrayOf())
        launchMainActivity()
        goToIssueFrag()
        goToIssueComment(0)
        onView(recyclerViewWithId(R.id.IssueCommentList).viewHolderViewAtPosition(0,R.id.IssueCommentBody)).check(matches(
            withText("Test Comment Body")))
        onView(recyclerViewWithId(R.id.IssueCommentList).viewHolderViewAtPosition(0,R.id.IssueCommentDesc)).check(matches(
            withText("TestUser")))
        onView(recyclerViewWithId(R.id.IssueCommentList).viewHolderViewAtPosition(0,R.id.IssueCommentUserImage)).check(matches(
            isDisplayed()))
        mockWebServer.queue(200, arrayOf(testGithubIssueComment(commentId = 2,body = "Test Comment Body 2")))
        onView(withId(R.id.IssueCommentList)).perform(swipeDown())
        runBlocking { delay(250) }
        onView(recyclerViewWithId(R.id.IssueCommentList).viewHolderViewAtPosition(1,R.id.IssueCommentBody)).check(matches(
            withText("Test Comment Body 2")))
        onView(recyclerViewWithId(R.id.IssueCommentList).viewHolderViewAtPosition(1,R.id.IssueCommentDesc)).check(matches(
            withText("TestUser")))
        onView(recyclerViewWithId(R.id.IssueCommentList).viewHolderViewAtPosition(1,R.id.IssueCommentUserImage)).check(matches(
            isDisplayed()))
    }

    @Test
    fun testIssueCommentNoCacheDisplayMore(){
        setTellerStateEmpty(vm,issue = false)
        setTellerIssue(vm, listOf(testRepoIssue()))
        setTellerIssueCommentReq(vm)
        //mockWebServer.queue(200, arrayOf())
        launchMainActivity()
        goToIssueFrag()
        goToIssueComment(0)
        onView(withId(R.id.nothing_to_show)).check(matches(isDisplayed()))
        mockWebServer.queue(200, arrayOf(testGithubIssueComment(),testGithubIssueComment(commentId = 2,body = "Test Comment Body 2")))
        onView(withId(R.id.nothing_to_show)).perform(swipeDown())
        runBlocking { delay(550) }
        onView(recyclerViewWithId(R.id.IssueCommentList).viewHolderViewAtPosition(0,R.id.IssueCommentBody)).check(matches(
            withText("Test Comment Body")))
        onView(recyclerViewWithId(R.id.IssueCommentList).viewHolderViewAtPosition(0,R.id.IssueCommentDesc)).check(matches(
            withText("TestUser")))
        onView(recyclerViewWithId(R.id.IssueCommentList).viewHolderViewAtPosition(0,R.id.IssueCommentUserImage)).check(matches(
            isDisplayed()))
        onView(recyclerViewWithId(R.id.IssueCommentList).viewHolderViewAtPosition(1,R.id.IssueCommentBody)).check(matches(
            withText("Test Comment Body 2")))
        onView(recyclerViewWithId(R.id.IssueCommentList).viewHolderViewAtPosition(1,R.id.IssueCommentDesc)).check(matches(
            withText("TestUser")))
        onView(recyclerViewWithId(R.id.IssueCommentList).viewHolderViewAtPosition(1,R.id.IssueCommentUserImage)).check(matches(
            isDisplayed()))
    }
}