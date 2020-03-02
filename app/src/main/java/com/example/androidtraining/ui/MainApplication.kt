package com.example.androidtraining.ui

import android.app.Application
import com.example.androidtraining.di.AndroidModules
import com.example.androidtraining.di.AppGraph
import com.example.androidtraining.di.DaggerAppGraph
import com.levibostian.teller.Teller

open class MainApplication: Application() {

    lateinit var appComponent: AppGraph

    override fun onCreate() {
        super.onCreate()
        Teller.init(this)
        appComponent = initAppComponent()
    }

    open fun initAppComponent(): AppGraph {
        return DaggerAppGraph.builder().androidModules(
            AndroidModules(
                this
            )
        ).build()
    }
}