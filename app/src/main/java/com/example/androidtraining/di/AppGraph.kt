package com.example.androidtraining.di

import com.example.androidtraining.MainActivity
import com.example.androidtraining.ui.GitHubLoginFragment
import com.example.androidtraining.ui.IssueCommentFragment
import com.example.androidtraining.ui.IssuesFragment
import com.example.androidtraining.ui.RepoFragment
import dagger.Component
import javax.inject.Singleton


@Singleton
@Component(modules = [AndroidModules::class, ViewModelModule::class, DatabaseModules::class, NetworkModules::class])
interface AppGraph {
    fun inject(fragment: IssueCommentFragment)

    fun inject(activity: MainActivity)

    fun inject(fragment: RepoFragment)

    fun inject(fragment: GitHubLoginFragment)

    fun inject(fragment: IssuesFragment)
}