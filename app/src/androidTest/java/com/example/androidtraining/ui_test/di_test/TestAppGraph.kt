package com.example.androidtraining.ui_test.di_test

import com.example.androidtraining.di.AndroidModules
import com.example.androidtraining.di.AppGraph
import com.example.androidtraining.di.ViewModelModule
import com.example.androidtraining.ui_test.MainFragmentTest
import com.example.androidtraining.ui_test.RepoFragmentTests
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AndroidModules::class, ViewModelModule::class, TestDatabaseModules::class, TestNetworkModules::class])
interface TestAppGraph : AppGraph {
    fun inject(mainFragmentTest: MainFragmentTest)
    fun inject(repoFragment: RepoFragmentTests)
}