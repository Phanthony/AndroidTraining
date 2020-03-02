package com.example.androidtraining.ui_test

import com.example.androidtraining.di.AndroidModules
import com.example.androidtraining.di.AppGraph
import com.example.androidtraining.ui.MainApplication
import com.example.androidtraining.ui_test.di_test.DaggerTestAppGraph

class TestMainApplication: MainApplication() {

    override fun initAppComponent(): AppGraph {
        return DaggerTestAppGraph
            .builder()
            .androidModules(AndroidModules(this))
            .build()
    }
}