package com.example.androidtraining.ui_test

import android.content.SharedPreferences
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import com.example.androidtraining.GitHubViewModel
import com.example.androidtraining.MainActivity
import com.example.androidtraining.R
import com.example.androidtraining.service.GitHubLoginResponse
import com.example.androidtraining.service.GitHubLoginResult
import com.example.androidtraining.ui_test.di_test.DiGraphRule
import com.levibostian.recyclerviewmatcher.RecyclerViewMatcher.Companion.recyclerViewWithId
import com.levibostian.teller.repository.OnlineRepository
import com.levibostian.teller.testing.extensions.initState
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

class GitHubLoginFragmentTests: ActivityTestsInterface() {
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
    }

    @After
    fun close(){
        sharedPreferences.edit().clear().commit()
    }

    @Test
    fun testEmptyPassword(){
        val vm = viewModelFactory.create(GitHubViewModel::class.java)
        setTellerStateEmpty(vm)
        ActivityScenario.launch(MainActivity::class.java)
        onView(withId(R.id.login_dest)).perform(click())
        val user = onView(withId(R.id.GitHubLoginUsernameText))
        val pass = onView(withId(R.id.GitHubLoginPasswordText))
        user.perform(typeText("testUserName"))
        closeSoftKeyboard()
        onView(withId(R.id.GitHubLoginButton)).perform(click())
        pass.check(matches(hasFocus()))
        pass.check(matches(hasErrorText("Empty Password")))
    }

    @Test
    fun testEmptyUserName(){
        val vm = viewModelFactory.create(GitHubViewModel::class.java)
        setTellerStateEmpty(vm)
        ActivityScenario.launch(MainActivity::class.java)
        onView(withId(R.id.login_dest)).perform(click())
        val user = onView(withId(R.id.GitHubLoginUsernameText))
        val pass = onView(withId(R.id.GitHubLoginPasswordText))
        pass.perform(typeText("testPassword"))
        closeSoftKeyboard()
        onView(withId(R.id.GitHubLoginButton)).perform(click())
        user.check(matches(hasFocus()))
        user.check(matches(hasErrorText("Empty Username")))
    }

    @Test
    fun testAccountWrong(){
        val vm = viewModelFactory.create(GitHubViewModel::class.java)
        setTellerStateEmpty(vm)
        ActivityScenario.launch(MainActivity::class.java)
        onView(withId(R.id.login_dest)).perform(click())
        mockWebServer.queue(401,GitHubLoginResult("Sorry! The username or password is incorrect.", GitHubLoginResponse("","")))
        val user = onView(withId(R.id.GitHubLoginUsernameText))
        val pass = onView(withId(R.id.GitHubLoginPasswordText))
        user.perform(typeText("testUserName"))
        closeSoftKeyboard()
        pass.perform(typeText("testPassword"))
        closeSoftKeyboard()
        onView(withId(R.id.GitHubLoginButton)).perform(click())
        runBlocking { delay(100) }
        onView(withText("Sorry! The username or password is incorrect.")).check(matches(isDisplayed()))
    }

    @Test
    fun testExtraError(){
        val vm = viewModelFactory.create(GitHubViewModel::class.java)
        setTellerStateEmpty(vm)
        ActivityScenario.launch(MainActivity::class.java)
        onView(withId(R.id.login_dest)).perform(click())
        mockWebServer.queue(403,GitHubLoginResult("Error User Entered Bad Data", GitHubLoginResponse("","")))
        val user = onView(withId(R.id.GitHubLoginUsernameText))
        val pass = onView(withId(R.id.GitHubLoginPasswordText))
        user.perform(typeText("testUserName"))
        closeSoftKeyboard()
        pass.perform(typeText("testPassword"))
        closeSoftKeyboard()
        onView(withId(R.id.GitHubLoginButton)).perform(click())
        runBlocking { delay(100) }
        onView(withText("Sorry! There seems to be an issue. The team has been notified. Try again later.")).check(matches(isDisplayed()))
    }

    @Test
    fun testError500_600error(){
        val vm = viewModelFactory.create(GitHubViewModel::class.java)
        setTellerStateEmpty(vm)
        launchMainActivity()
        onView(withId(R.id.login_dest)).perform(click())
        mockWebServer.queue(521,GitHubLoginResult("error", GitHubLoginResponse("","")))
        val user = onView(withId(R.id.GitHubLoginUsernameText))
        val pass = onView(withId(R.id.GitHubLoginPasswordText))
        user.perform(typeText("testUserName"))
        closeSoftKeyboard()
        pass.perform(typeText("testPassword"))
        closeSoftKeyboard()
        onView(withId(R.id.GitHubLoginButton)).perform(click())
        runBlocking { delay(100) }
        onView(withText("Sorry! There seems to be an error with the system. The team has been notified. Try again later.")).check(
            matches(isDisplayed()))
    }

    @Test
    fun testAccountCorrect(){
        val vm = viewModelFactory.create(GitHubViewModel::class.java)
        setTellerStateEmpty(vm,issue = false)
        OnlineRepository.Testing.initState(vm.repositoryIssue,vm.repositoryIssue.requirements!!){
            cache(listOf(testRepoIssue()))
        }
        ActivityScenario.launch(MainActivity::class.java)
        mockWebServer.queue(200,testGitHubLoginResult())
        onView(withId(R.id.login_dest)).perform(click())
        onView(withId(R.id.GitHubLoginUsernameText)).perform(typeText("testUserName"))
        closeSoftKeyboard()
        onView(withId(R.id.GitHubLoginPasswordText)).perform(typeText("testPassword"))
        closeSoftKeyboard()
        onView(withId(R.id.GitHubLoginButton)).perform(click())
        runBlocking {
            delay(250)
        }
        onView(recyclerViewWithId(R.id.IssueList).viewHolderViewAtPosition(0,R.id.IssueTitle)).check(
            matches(withText("Test Issue Title")))
        onView(recyclerViewWithId(R.id.IssueList).viewHolderViewAtPosition(0,R.id.IssueCommentNum)).check(
            matches(withText("1"))
        )
    }
}