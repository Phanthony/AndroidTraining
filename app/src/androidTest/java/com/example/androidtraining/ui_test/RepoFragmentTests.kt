package com.example.androidtraining.ui_test

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.swipeDown
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.filters.SdkSuppress
import com.example.androidtraining.MainActivity
import com.example.androidtraining.R
import com.example.androidtraining.database.GitHubRepo
import com.example.androidtraining.database.GitHubRepoList
import com.example.androidtraining.database.GitHubUser
import com.example.androidtraining.ui_test.di_test.DiGraphRule
import com.levibostian.recyclerviewmatcher.RecyclerViewMatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@LargeTest
@SdkSuppress(minSdkVersion = 18)
class RepoFragmentTests {
    @Inject
    lateinit var mockWebServer: MockWebServer

    @get:Rule
    val diGraph = DiGraphRule()

    @Before
    fun setup() {
        diGraph.graph.inject(this)
    }

    @Test
    fun testDisplayRepos(){
        val mUser = GitHubUser("https://media.gettyimages.com/vectors/-vector-id140788495?s=2048x2048","testUser")
        val mRepos = GitHubRepoList(listOf(GitHubRepo("test1",mUser,50,"Test desc",3),
            GitHubRepo("test",mUser,3,null,1)
        ))
        mockWebServer.queue(200,mRepos)
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        onView(withId(R.id.RepoList)).perform(swipeDown())
        onView(RecyclerViewMatcher.recyclerViewWithId(R.id.RepoList).viewHolderViewAtPosition(0,R.id.RepoNameAuthor)).check(
            matches(withText("testUser"))
        )
    }
}