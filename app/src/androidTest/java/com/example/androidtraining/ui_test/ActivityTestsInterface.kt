package com.example.androidtraining.ui_test

import androidx.test.core.app.ActivityScenario
import com.example.androidtraining.GitHubViewModel
import com.example.androidtraining.MainActivity
import com.example.androidtraining.database.GitHubRepo
import com.example.androidtraining.database.GitHubRepoList
import com.example.androidtraining.database.GitHubUser
import com.example.androidtraining.database.teller.TellerIssueCommentsOnlineRepository
import com.example.androidtraining.service.GitHubIssue
import com.example.androidtraining.service.GitHubIssueComment
import com.example.androidtraining.service.GitHubLoginResponse
import com.example.androidtraining.service.GitHubLoginResult
import com.levibostian.teller.repository.OnlineRepository
import com.levibostian.teller.testing.extensions.initState

open class ActivityTestsInterface {
    open fun setTellerStateEmpty(
        vm: GitHubViewModel,
        repo: Boolean = true,
        issue: Boolean = true,
        comment: Boolean = true
    ) {
        if (repo) {
            setTellerRepo(vm,GitHubRepoList(listOf()))
        }
        if (issue) {
            setTellerIssue(vm, listOf())
        }
        if (comment) {
            setTellerIssueComment(vm, listOf())
        }
    }

    open fun setTellerIssue(vm: GitHubViewModel, list: List<GitHubIssue>) {
        OnlineRepository.Testing.initState(vm.repositoryIssue,vm.repositoryIssue.requirements!!){
            cache(list)
        }
    }

    open fun setTellerRepo(vm: GitHubViewModel, list: GitHubRepoList) {
        OnlineRepository.Testing.initState(vm.repositoryRepo,vm.repositoryRepo.requirements!!){
            cache(list)
        }
    }

    open fun setTellerIssueComment(vm: GitHubViewModel, list: List<GitHubIssueComment>) {
        OnlineRepository.Testing.initState(vm.repositoryIssueComment,vm.repositoryIssueComment.requirements!!){
            cache(list)
        }
    }

    open fun setTellerIssueCommentReq(vm: GitHubViewModel, requirements: TellerIssueCommentsOnlineRepository.GetCommentRequirement = testIssueCommentRequirements()){
        vm.repositoryIssueComment.requirements = requirements
    }

    open fun testIssueCommentRequirements(issueNum: Int = 1, issueName: String = "Test Repo", user: String = "TestUser", issueId: Int = 1): TellerIssueCommentsOnlineRepository.GetCommentRequirement{
        return TellerIssueCommentsOnlineRepository.GetCommentRequirement(issueNum,issueName,user,issueId)
    }

    open fun testGitHubUser(login: String = "TestUser"): GitHubUser {
        return GitHubUser(
            login,
            "https://media.gettyimages.com/vectors/-vector-id140788495?s=2048x2048"
        )
    }

    open fun testGitHubRepo(
        name: String = "Test Repo",
        user: GitHubUser,
        stars: Int = 1,
        desc: String? = "Test Description",
        id: Int = 1
    ): GitHubRepo {
        return GitHubRepo(name, user, stars, desc, id)
    }

    open fun testGitHubRepoList(list: List<GitHubRepo>): GitHubRepoList {
        return GitHubRepoList(list)
    }

    open fun testRepoIssue(
        issueID: Int = 1,
        issueNum: Int = 1,
        issueState: String = "open",
        issueTitle: String = "Test Issue Title",
        user: GitHubUser = testGitHubUser(),
        comments: Int = 1, updated: String = "1/1/2020",
        repo: GitHubRepo = testGitHubRepo(user = testGitHubUser())
    ): GitHubIssue {
        return GitHubIssue(issueID, issueNum, issueState, issueTitle, user, comments, updated, repo)
    }

    open fun testGitHubLoginResult(
        msg: String = "Test Message",
        response: GitHubLoginResponse = testGithubLoginResponse()
    ): GitHubLoginResult {
        return GitHubLoginResult(msg, response)
    }

    open fun testGithubLoginResponse(
        acs_token: String = "Test Token",
        url: String = "Test Auth"
    ): GitHubLoginResponse {
        return GitHubLoginResponse(acs_token, url)
    }

    open fun testGithubIssueComment(
        commentId: Int = 1,
        user: GitHubUser = testGitHubUser(),
        body: String = "Test Comment Body",
        issueId: Int = 1
    ): GitHubIssueComment {
        return GitHubIssueComment(commentId,user,body,issueId)
    }

    open fun launchMainActivity(){
        ActivityScenario.launch(MainActivity::class.java)
    }
}