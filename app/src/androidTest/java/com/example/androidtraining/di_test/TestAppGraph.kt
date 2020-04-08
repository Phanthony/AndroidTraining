package com.example.androidtraining.di_test

import com.example.androidtraining.di.AndroidModules
import com.example.androidtraining.di.AppGraph
import com.example.androidtraining.di.ViewModelModule
import com.example.androidtraining.integration_test.GitHubViewModelTests
import com.example.androidtraining.integration_test.TellerIssueCommentTests
import com.example.androidtraining.integration_test.TellerIssueTests
import com.example.androidtraining.integration_test.TellerRepoTests
import com.example.androidtraining.ui_test.*
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AndroidModules::class, ViewModelModule::class, TestDatabaseModules::class, TestNetworkModules::class])
interface TestAppGraph : AppGraph {
    // UI Tests
    fun inject(mainFragmentTest: MainFragmentTest)
    fun inject(repoFragment: RepoFragmentTests)
    fun inject(issueFragment: IssueFragmentTests)
    fun inject(loginFragment: GitHubLoginFragmentTests)
    fun inject(commentFragment: IssueCommentFragmentTests)
    fun inject(navigation: NavigationTests)

    // Integration Tests
    fun inject(viewmodel: GitHubViewModelTests)
    fun inject(tellerRepo: TellerRepoTests)
    fun inject(tellerComment: TellerIssueCommentTests)
    fun inject(tellerIssue: TellerIssueTests)
}