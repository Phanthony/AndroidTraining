package com.example.androidtraining

import android.app.Application
import com.levibostian.teller.Teller

class MainApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        Teller.init(this)
    }
}