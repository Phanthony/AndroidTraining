package com.example.androidtraining.ui_test

import android.content.SharedPreferences
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeDown
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.example.androidtraining.GitHubViewModel
import com.example.androidtraining.MainActivity
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

    @Before
    fun setup() {
        diGraph.graph.inject(this)
        sharedPreferences.edit().putString("access_token", "test token").commit()
        vm = viewModelFactory.create(GitHubViewModel::class.java)
    }

    @Test
    fun testDisplayIssues() {
        setTellerStateEmpty(vm, issue = false)
        setTellerIssue(
            vm,
            listOf(
                testRepoIssue(),
                testRepoIssue(issueID = 2, issueTitle = "Test Issue 2", comments = 3)
            )
        )
        ActivityScenario.launch(MainActivity::class.java)
        onView(withId(R.id.login_dest)).perform(click())
        onView(
            recyclerViewWithId(R.id.IssueList).viewHolderViewAtPosition(
                0,
                R.id.IssueTitle
            )
        ).check(
            matches(ViewMatchers.withText("Test Issue Title"))
        )
        onView(
            recyclerViewWithId(R.id.IssueList).viewHolderViewAtPosition(
                0,
                R.id.IssueCommentNum
            )
        ).check(
            matches(ViewMatchers.withText("1"))
        )
        onView(
            recyclerViewWithId(R.id.IssueList).viewHolderViewAtPosition(
                1,
                R.id.IssueTitle
            )
        ).check(
            matches(ViewMatchers.withText("Test Issue 2"))
        )
        onView(
            recyclerViewWithId(R.id.IssueList).viewHolderViewAtPosition(
                1,
                R.id.IssueCommentNum
            )
        ).check(
            matches(ViewMatchers.withText("3"))
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
        ActivityScenario.launch(MainActivity::class.java)
        onView(withId(R.id.login_dest)).perform(click())
        onView(
            recyclerViewWithId(R.id.IssueList).viewHolderViewAtPosition(
                1,
                R.id.IssueTitle
            )
        ).check(
            matches(ViewMatchers.withText("Test Issue 3"))
        )
        onView(
            recyclerViewWithId(R.id.IssueList).viewHolderViewAtPosition(
                1,
                R.id.IssueCommentNum
            )
        ).check(
            matches(ViewMatchers.withText("3"))
        )
        onView(withId(R.id.IssueList)).perform(swipeDown())
        runBlocking { delay(2050) }
        onView(
            recyclerViewWithId(R.id.IssueList).viewHolderViewAtPosition(
                1,
                R.id.IssueTitle
            )
        ).check(
            matches(ViewMatchers.withText("Test Issue 2"))
        )
        onView(
            recyclerViewWithId(R.id.IssueList).viewHolderViewAtPosition(
                1,
                R.id.IssueCommentNum
            )
        ).check(
            matches(ViewMatchers.withText("4"))
        )
    }
}