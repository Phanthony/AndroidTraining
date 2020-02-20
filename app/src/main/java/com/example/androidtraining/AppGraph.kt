package com.example.androidtraining

import com.example.androidtraining.ui.IssueCommentFragment
import com.example.androidtraining.ui.RepoFragment
import dagger.Component
import javax.inject.Singleton


@Singleton
@Component(modules = [Modules::class])
interface AppGraph {
    fun inject(fragment: IssueCommentFragment)

    fun inject(fragment: RepoFragment)
}