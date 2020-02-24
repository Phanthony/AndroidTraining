package com.example.androidtraining.ui

import android.app.Application
import com.example.androidtraining.AndroidModules
import com.example.androidtraining.AppGraph
import com.example.androidtraining.DaggerAppGraph
import com.levibostian.teller.Teller

class MainApplication: Application() {

    lateinit var appComponent: AppGraph

    override fun onCreate() {
        super.onCreate()
        Teller.init(this)
        appComponent = initAppComponent()
    }

    fun initAppComponent(): AppGraph {
        return DaggerAppGraph.builder().androidModules(AndroidModules(this)).build()
    }
}