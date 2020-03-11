package com.example.androidtraining.ui_test

import android.content.SharedPreferences
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeDown
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import com.example.androidtraining.GitHubViewModel
import com.example.androidtraining.MainActivity
import com.example.androidtraining.R
import com.example.androidtraining.database.GitHubRepo
import com.example.androidtraining.database.GitHubRepoList
import com.example.androidtraining.database.GitHubUser
import com.example.androidtraining.ui_test.di_test.DiGraphRule
import com.levibostian.recyclerviewmatcher.RecyclerViewMatcher
import com.levibostian.teller.repository.OnlineRepository
import com.levibostian.teller.testing.extensions.initState
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 18)
class RepoFragmentTests: ActivityTestsInterface() {

    @Inject
    lateinit var mockWebServer: MockWebServer

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @get:Rule
    val diGraph = DiGraphRule()

    @Before
    fun setup() {
        diGraph.graph.inject(this)
        ActivityScenario.launch(MainActivity::class.java)
    }

    @After
    fun close(){
        val vm = viewModelFactory.create(GitHubViewModel::class.java)
        vm.database.clearAllTables()
    }

    @Test
    fun testDisplayRepos() {
        val mUser = GitHubUser(
            "testUser",
            "https://media.gettyimages.com/vectors/-vector-id140788495?s=2048x2048"
        )
        val mRepos = GitHubRepoList(
            listOf(
                GitHubRepo("test1", mUser, 50, "Test desc", 3),
                GitHubRepo("test", mUser, 3, null, 1)
            )
        )
        mockWebServer.queue(200, mRepos)
        val vm = viewModelFactory.create(GitHubViewModel::class.java)
        setTellerStateEmpty(vm)
        onView(withId(R.id.RepoList)).perform(swipeDown())
        runBlocking {
            delay(200)
        }
        onView(
            RecyclerViewMatcher.recyclerViewWithId(R.id.RepoList)
                .viewHolderViewAtPosition(0, R.id.RepoNameAuthor)
        ).check(matches(withText("testUser / test1")))
        onView(
            RecyclerViewMatcher.recyclerViewWithId(R.id.RepoList)
                .viewHolderViewAtPosition(0, R.id.RepoDescription)
        ).check(matches(withText("Test desc")))
        onView(
            RecyclerViewMatcher.recyclerViewWithId(R.id.RepoList)
                .viewHolderViewAtPosition(1, R.id.RepoNameAuthor)
        ).check(matches((withText("testUser / test"))))
        onView(
            RecyclerViewMatcher.recyclerViewWithId(R.id.RepoList)
                .viewHolderViewAtPosition(1, R.id.RepoDescription)
        ).check(matches(withText("No Description Available")))
    }

    @Test
    fun testDisplayOldRepos(){
        val mUser = GitHubUser(
            "testUser",
            "https://media.gettyimages.com/vectors/-vector-id140788495?s=2048x2048"
        )
        val mRepos = GitHubRepoList(
            listOf(
                GitHubRepo("test1", mUser, 50, "Test desc", 3),
                GitHubRepo("test", mUser, 3, null, 1)
            )
        )
        val vm = viewModelFactory.create(GitHubViewModel::class.java)
        setTellerStateEmpty(vm,repo = false)
        OnlineRepository.Testing.initState(vm.repositoryRepo,vm.repositoryRepo.requirements!!){
            cache(mRepos)
        }
        //ActivityScenario.launch(MainActivity::class.java)
        onView(
            RecyclerViewMatcher.recyclerViewWithId(R.id.RepoList)
                .viewHolderViewAtPosition(0, R.id.RepoNameAuthor)
        ).check(matches(withText("testUser / test1")))
        onView(
            RecyclerViewMatcher.recyclerViewWithId(R.id.RepoList)
                .viewHolderViewAtPosition(0, R.id.RepoDescription)
        ).check(matches(withText("Test desc")))
        onView(
            RecyclerViewMatcher.recyclerViewWithId(R.id.RepoList)
                .viewHolderViewAtPosition(1, R.id.RepoNameAuthor)
        ).check(matches((withText("testUser / test"))))
        onView(
            RecyclerViewMatcher.recyclerViewWithId(R.id.RepoList)
                .viewHolderViewAtPosition(1, R.id.RepoDescription)
        ).check(matches(withText("No Description Available")))
    }

    @Test
    fun testErrorPopsUpNoInternet(){
        runBlocking {
            // let the ui load
            delay(250)
        }
        val vm = viewModelFactory.create(GitHubViewModel::class.java)
        setTellerStateEmpty(vm)
        onView(withId(R.id.RepoList)).perform(swipeDown())
        runBlocking {
            delay(5050)
        }
        onView(withText("OK")).perform(click())
    }

    @Test
    fun testAddMoreRepos(){
        val mUser = GitHubUser(
            "testUser",
            "https://media.gettyimages.com/vectors/-vector-id140788495?s=2048x2048"
        )
        val mRepos = GitHubRepoList(
            listOf(
                GitHubRepo("test1", mUser, 50, "Test desc", 3),
                GitHubRepo("test", mUser, 3, null, 1)
            )
        )
        val newRepos = GitHubRepoList(listOf(GitHubRepo("test 3",mUser,80,"Test Description 3",9)))
        val vm = viewModelFactory.create(GitHubViewModel::class.java)
        setTellerStateEmpty(vm,repo = false)
        OnlineRepository.Testing.initState(vm.repositoryRepo,vm.repositoryRepo.requirements!!){
            cache(mRepos)
        }
        mockWebServer.queue(200,newRepos)
        //ActivityScenario.launch(MainActivity::class.java)
        onView(RecyclerViewMatcher.recyclerViewWithId(R.id.RepoList).viewHolderViewAtPosition(0,R.id.RepoNameAuthor)).check(
            matches(withText("testUser / test1")))
        onView(RecyclerViewMatcher.recyclerViewWithId(R.id.RepoList).viewHolderViewAtPosition(0,R.id.RepoDescription)).check(
            matches(withText("Test desc")))
        onView(withId(R.id.RepoList)).perform(swipeDown())
        runBlocking {
            delay(100)
        }
        onView(RecyclerViewMatcher.recyclerViewWithId(R.id.RepoList).viewHolderViewAtPosition(0,R.id.RepoNameAuthor)).check(
            matches(withText("testUser / test 3")))
        onView(RecyclerViewMatcher.recyclerViewWithId(R.id.RepoList).viewHolderViewAtPosition(0,R.id.RepoDescription)).check(
            matches(withText("Test Description 3")))
        onView(RecyclerViewMatcher.recyclerViewWithId(R.id.RepoList).viewHolderViewAtPosition(1,R.id.RepoNameAuthor)).check(
            matches(withText("testUser / test1")))
        onView(RecyclerViewMatcher.recyclerViewWithId(R.id.RepoList).viewHolderViewAtPosition(1,R.id.RepoDescription)).check(
            matches(withText("Test desc")))
    }

    fun serverExceptionErrorPopsUp(){
        val mUser = GitHubUser(
            "testUser",
            "https://media.gettyimages.com/vectors/-vector-id140788495?s=2048x2048"
        )
        val mRepos = GitHubRepoList(
            listOf(
                GitHubRepo("test1", mUser, 50, "Test desc", 3),
                GitHubRepo("test", mUser, 3, null, 1)
            )
        )
        val newRepos = GitHubRepoList(listOf(GitHubRepo("test 3",mUser,80,"Test Description 3",9)))
        val vm = viewModelFactory.create(GitHubViewModel::class.java)
        setTellerStateEmpty(vm,repo = false)
        OnlineRepository.Testing.initState(vm.repositoryRepo,vm.repositoryRepo.requirements!!){
            cache(mRepos)
        }
        mockWebServer.queue(510,newRepos)
        onView(withId(R.id.RepoList)).perform(swipeDown())
        // Ask Levi how to check text from dialog builder
    }

    fun extraErrorPopsUp(){
        val mUser = GitHubUser(
            "testUser",
            "https://media.gettyimages.com/vectors/-vector-id140788495?s=2048x2048"
        )
        val mRepos = GitHubRepoList(
            listOf(
                GitHubRepo("test1", mUser, 50, "Test desc", 3),
                GitHubRepo("test", mUser, 3, null, 1)
            )
        )
        val newRepos = GitHubRepoList(listOf(GitHubRepo("test 3",mUser,80,"Test Description 3",9)))
        val vm = viewModelFactory.create(GitHubViewModel::class.java)
        setTellerStateEmpty(vm,repo = false)
        OnlineRepository.Testing.initState(vm.repositoryRepo,vm.repositoryRepo.requirements!!){
            cache(mRepos)
        }
        mockWebServer.queue(430,newRepos)
        onView(withId(R.id.RepoList)).perform(swipeDown())
        // Ask Levi how to check text from dialog builder
    }

}