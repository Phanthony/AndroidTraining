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
import com.example.androidtraining.ui_test.di_test.DiGraphRule
import com.levibostian.recyclerviewmatcher.RecyclerViewMatcher.Companion.recyclerViewWithId
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

class IssueFragmentTests : ActivityTestsInterface() {
    @Inject
    lateinit var mockWebServer: MockWebServer

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @get:Rule
    val diGraph = DiGraphRule()

    lateinit var vm: GitHubViewModel

    fun goToIssueFrag() {
        onView(withId(R.id.login_dest)).perform(click())
    }

    @Before
    fun setup() {
        diGraph.graph.inject(this)
        sharedPreferences.edit().putString("access_token", "test token").commit()
        vm = viewModelFactory.create(GitHubViewModel::class.java)
    }

    @Test
    fun testDisplayIssuesFromCache() {
        setTellerStateEmpty(vm, issue = false)
        setTellerIssue(
            vm,
            listOf(
                testRepoIssue(),
                testRepoIssue(issueID = 2, issueTitle = "Test Issue 2", comments = 3)
            )
        )
        launchMainActivity()
        goToIssueFrag()
        onView(
            recyclerViewWithId(R.id.IssueList).viewHolderViewAtPosition(
                0,
                R.id.IssueTitle
            )
        ).check(
            matches(withText("Test Issue Title"))
        )
        onView(
            recyclerViewWithId(R.id.IssueList).viewHolderViewAtPosition(
                0,
                R.id.IssueCommentNum
            )
        ).check(
            matches(withText("1"))
        )
        onView(
            recyclerViewWithId(R.id.IssueList).viewHolderViewAtPosition(
                1,
                R.id.IssueTitle
            )
        ).check(
            matches(withText("Test Issue 2"))
        )
        onView(
            recyclerViewWithId(R.id.IssueList).viewHolderViewAtPosition(
                1,
                R.id.IssueCommentNum
            )
        ).check(
            matches(withText("3"))
        )
    }

    @Test
    fun testDisplayNewIssues() {
        setTellerStateEmpty(vm, issue = false)
        setTellerIssue(
            vm,
            listOf(
                testRepoIssue(),
                testRepoIssue(issueID = 3, issueTitle = "Test Issue 3", comments = 3)
            )
        )
        mockWebServer.queue(
            200, arrayOf(testRepoIssue(issueID = 2, issueTitle = "Test Issue 2", comments = 4))
        )
        launchMainActivity()
        goToIssueFrag()
        onView(
            recyclerViewWithId(R.id.IssueList).viewHolderViewAtPosition(
                1,
                R.id.IssueTitle
            )
        ).check(
            matches(withText("Test Issue 3"))
        )
        onView(
            recyclerViewWithId(R.id.IssueList).viewHolderViewAtPosition(
                1,
                R.id.IssueCommentNum
            )
        ).check(
            matches(withText("3"))
        )
        onView(withId(R.id.IssueList)).perform(swipeDown())
        runBlocking { delay(2050) }
        onView(
            recyclerViewWithId(R.id.IssueList).viewHolderViewAtPosition(
                1,
                R.id.IssueTitle
            )
        ).check(
            matches(withText("Test Issue 2"))
        )
        onView(
            recyclerViewWithId(R.id.IssueList).viewHolderViewAtPosition(
                1,
                R.id.IssueCommentNum
            )
        ).check(
            matches(withText("4"))
        )
    }

    @Test
    fun testDisplayIssuesNoCache() {
        setTellerStateEmpty(vm)
        launchMainActivity()
        mockWebServer.queue(200, arrayOf(testRepoIssue()))
        goToIssueFrag()
        onView(withId(R.id.IssueList)).check(matches(isDisplayed()))
        onView(withId(R.id.IssueList)).perform(swipeDown())
        runBlocking { delay(250) }
        onView(recyclerViewWithId(R.id.IssueList).viewHolderViewAtPosition(0,R.id.IssueTitle)).check(
            matches(withText("Test Issue Title")))
    }
}