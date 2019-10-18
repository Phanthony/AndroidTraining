package com.example.androidtraining.ui

import android.app.Application
import com.levibostian.teller.Teller

class MainApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        Teller.init(this)
    }
}