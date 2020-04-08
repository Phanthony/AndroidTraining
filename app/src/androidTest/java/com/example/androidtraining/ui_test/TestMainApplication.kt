package com.example.androidtraining.ui_test

import com.example.androidtraining.di.AndroidModules
import com.example.androidtraining.di.AppGraph
import com.example.androidtraining.di_test.DaggerTestAppGraph
import com.example.androidtraining.ui.MainApplication

class TestMainApplication: MainApplication() {

    override fun initAppComponent(): AppGraph {
        return DaggerTestAppGraph
            .builder()
            .androidModules(AndroidModules(this))
            .build()
    }
}