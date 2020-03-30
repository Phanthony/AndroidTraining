package com.example.androidtraining

import android.app.Application
import android.content.SharedPreferences
import com.example.androidtraining.database.GitHubDataBase
import com.example.androidtraining.database.teller.TellerIssueCommentsOnlineRepository
import com.example.androidtraining.database.teller.TellerIssueOnlineRepository
import com.example.androidtraining.database.teller.TellerRepoOnlineRepository
import com.example.androidtraining.service.Service
import com.google.common.truth.Truth.assertThat
import com.levibostian.teller.Teller
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations.initMocks
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class GitHubViewModelTests {

    @Mock
    lateinit var mApplication: Application
    @Mock
    lateinit var mDay: Day
    @Mock
    lateinit var mService: Service
    @Mock
    lateinit var mDatabase: GitHubDataBase
    @Mock
    lateinit var mPref: SharedPreferences

    lateinit var mRepoTeller: TellerRepoOnlineRepository
    lateinit var mIssueTeller: TellerIssueOnlineRepository
    lateinit var mIssueCommentTeller: TellerIssueCommentsOnlineRepository
    lateinit var mViewModel: GitHubViewModel

    @Before
    fun setup(){
        initMocks(this)
        Teller.initTesting(mPref)
        mRepoTeller = TellerRepoOnlineRepository(mDatabase,mService)
        mIssueTeller = TellerIssueOnlineRepository(mDatabase,mService)
        mIssueCommentTeller = TellerIssueCommentsOnlineRepository(mDatabase,mService)
        mViewModel = GitHubViewModel(mApplication,mRepoTeller,mDay,mService,mIssueTeller,mIssueCommentTeller,mDatabase)
    }

    @Test
    fun `test viewmodel does initial setup`(){
        assertThat(mRepoTeller.requirements!!.tag).isEqualTo("Trending Kotlin repos")
        assertThat(mIssueTeller.requirements!!.tag).isEqualTo("All Issues for user null")
        assertThat(mIssueCommentTeller.requirements!!.tag).isEqualTo("Comments for Repo: , Issue#${Int.MIN_VALUE}, by ")
    }

    @Test
    fun `test changeIssueUser`(){
        mViewModel.changeIssueUser("TestUser")
        assertThat(mIssueTeller.requirements!!.tag).isEqualTo("All Issues for user TestUser")
    }

    @Test
    fun `test changeIssueUser username with spaces`(){
        mViewModel.changeIssueUser("Test User")
        assertThat(mIssueTeller.requirements!!.tag).isEqualTo("All Issues for user Test User")
    }

    @Test
    fun `test changeIssueUser long username`(){
        mViewModel.changeIssueUser("this_is_a_really_long_username_that_I_don't_even_know_if_github_allows")
        assertThat(mIssueTeller.requirements!!.tag).isEqualTo("All Issues for user this_is_a_really_long_username_that_I_don't_even_know_if_github_allows")
    }

    @Test
    fun `test changeIssueUser username with numbers`(){
        mViewModel.changeIssueUser("TestUser123")
        assertThat(mIssueTeller.requirements!!.tag).isEqualTo("All Issues for user TestUser123")
    }

    @Test
    fun `test changeIssueComment`(){
        mViewModel.changeIssueComment(2,"TestIssue2","TestUser2",2)
        assertThat(mIssueCommentTeller.requirements!!.tag).isEqualTo("Comments for Repo: TestIssue2, Issue#2, by TestUser2")
    }

    @Test
    fun `test changeIssueComment long username`(){
        mViewModel.changeIssueComment(2,"TestIssue2","this_is_a_really_long_username_that_I_don't_even_know_if_github_allows",2)
        assertThat(mIssueCommentTeller.requirements!!.tag).isEqualTo("Comments for Repo: TestIssue2, Issue#2, by this_is_a_really_long_username_that_I_don't_even_know_if_github_allows")
    }

    @Test
    fun `test changeIssueComment long Repo`(){
        mViewModel.changeIssueComment(2,"this_is_a_test_repo_name_that_is_very_long_even_longer_than_normal","TestUser",1)
        assertThat(mIssueCommentTeller.requirements!!.tag).isEqualTo("Comments for Repo: this_is_a_test_repo_name_that_is_very_long_even_longer_than_normal, Issue#2, by TestUser")
    }

    @Test
    fun `test changeIssueComment big issue number`(){
        mViewModel.changeIssueComment(Int.MAX_VALUE,"TestRepo","TestUser",1)
        assertThat(mIssueCommentTeller.requirements!!.tag).isEqualTo("Comments for Repo: TestRepo, Issue#${Int.MAX_VALUE}, by TestUser")
    }

    @Test
    fun `test changeIssueComment username with numbers`(){
        mViewModel.changeIssueComment(1,"TestRepo","TestUser123",1)
        assertThat(mIssueCommentTeller.requirements!!.tag).isEqualTo("Comments for Repo: TestRepo, Issue#1, by TestUser123")
    }

    @Test
    fun `test changeIssueComment username with spaces`(){
        mViewModel.changeIssueComment(1,"TestRepo","Test User",1)
        assertThat(mIssueCommentTeller.requirements!!.tag).isEqualTo("Comments for Repo: TestRepo, Issue#1, by Test User")
    }

    @Test
    fun `test changeIssueComment repo with numbers`(){
        mViewModel.changeIssueComment(1,"TestRepo123","TestUser",1)
        assertThat(mIssueCommentTeller.requirements!!.tag).isEqualTo("Comments for Repo: TestRepo123, Issue#1, by TestUser")
    }

    @Test
    fun `test changeIssueComment repo with spaces`(){
        mViewModel.changeIssueComment(1,"Test Repo","TestUser",1)
        assertThat(mIssueCommentTeller.requirements!!.tag).isEqualTo("Comments for Repo: Test Repo, Issue#1, by TestUser")
    }

    @Test
    fun `test loginToGithub`(){
        mViewModel.loginToGithub("TestUser","TestPass")
        verify(mService).loginToGithub("TestUser","TestPass")
    }

    @Test
    fun `test loginToGithub long Username`(){
        mViewModel.loginToGithub("this_is_a_really_long_username_that_I_don't_even_know_if_github_allows","TestPass")
        verify(mService).loginToGithub("this_is_a_really_long_username_that_I_don't_even_know_if_github_allows","TestPass")
    }

    @Test
    fun `test loginToGithub Username with spaces`(){
        mViewModel.loginToGithub("Test User","TestPass")
        verify(mService).loginToGithub("Test User", "TestPass")
    }

    @Test
    fun `test loginToGithub Username with numbers`(){
        mViewModel.loginToGithub("TestUser123","TestPass")
        verify(mService).loginToGithub("TestUser123","TestPass")
    }

    @Test
    fun `test loginToGithub Username with special chars`(){
        mViewModel.loginToGithub("`~!@#$%^&*()-_=+<,>./?[{]}","TestPass")
        verify(mService).loginToGithub("`~!@#$%^&*()-_=+<,>./?[{]}","TestPass")
    }

    @Test
    fun `test loginToGithub long password`(){
        mViewModel.loginToGithub("TestUser", "This_is_a_long_password_that_is_very_secure_and_no_one_should_guess_it")
        verify(mService).loginToGithub("TestUser","This_is_a_long_password_that_is_very_secure_and_no_one_should_guess_it")
    }

    @Test
    fun `test loginToGithub password with spaces`(){
        mViewModel.loginToGithub("TestUser","Test Pass")
        verify(mService).loginToGithub("TestUser","Test Pass")
    }

    @Test
    fun `test loginToGithub password with numbers`(){
        mViewModel.loginToGithub("TestUser","TestPass123")
        verify(mService).loginToGithub("TestUser","TestPass123")
    }

    @Test
    fun `test loginToGithub password with special chars`(){
        mViewModel.loginToGithub("TestUser","`~!@#$%^&*()-_=+<,>./?[{]}")
        verify(mService).loginToGithub("TestUser","`~!@#$%^&*()-_=+<,>./?[{]}")
    }
}