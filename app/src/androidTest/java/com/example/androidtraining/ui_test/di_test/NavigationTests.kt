package com.example.androidtraining.ui_test.di_test

import android.content.SharedPreferences
import androidx.lifecycle.ViewModelProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import com.example.androidtraining.GitHubViewModel
import com.example.androidtraining.R
import com.example.androidtraining.ui_test.ActivityTestsInterface
import com.example.androidtraining.ui_test.MockWebServer
import com.levibostian.recyclerviewmatcher.RecyclerViewMatcher.Companion.recyclerViewWithId
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

class NavigationTests: ActivityTestsInterface() {
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
        vm = viewModelFactory.create(GitHubViewModel::class.java)
    }

    @Test
    fun RepoToRepoTest(){
        setTellerStateEmpty(vm)
        launchMainActivity()
        onView(withId(R.id.RepoList)).check(matches(isDisplayed()))
        onView(withId(R.id.repo_dest)).perform(click())
        onView(withId(R.id.RepoList)).check(matches(isDisplayed()))
    }

    @Test
    fun RepoToLogin(){
        setTellerStateEmpty(vm)
        launchMainActivity()
        onView(withId(R.id.RepoList)).check(matches(isDisplayed()))
        onView(withId(R.id.login_dest)).perform(click())
        onView(withId(R.id.gitHubLoginDisplayText)).check(matches(withText("Log into GitHub")))
        onView(withId(R.id.userPassLayout)).check(matches(isDisplayed()))
    }

    @Test
    fun RepoToIssues(){
        setTellerStateEmpty(vm)
        sharedPreferences.edit().putString("access_token", "test token").commit()
        launchMainActivity()
        onView(withId(R.id.RepoList)).check(matches(isDisplayed()))
        onView(withId(R.id.login_dest)).perform(click())
        onView(withId(R.id.IssueList)).check(matches(isDisplayed()))
        sharedPreferences.edit().clear().commit()
    }

    @Test
    fun RepoToIssueComment(){
        setTellerStateEmpty(vm,issue = false)
        setTellerIssue(vm, listOf(testRepoIssue()))
        sharedPreferences.edit().putString("access_token", "test token").commit()
        launchMainActivity()
        onView(withId(R.id.RepoList)).check(matches(isDisplayed()))
        onView(withId(R.id.login_dest)).perform(click())
        onView(recyclerViewWithId(R.id.IssueList).itemViewAtIndex(0)).perform(
            click())
        onView(withId(R.id.IssueCommentList)).check(matches(isDisplayed()))
        sharedPreferences.edit().clear().commit()
    }
}