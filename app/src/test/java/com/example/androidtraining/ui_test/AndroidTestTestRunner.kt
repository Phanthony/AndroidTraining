package com.example.androidtraining.ui_test

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import com.example.androidtraining.ui.MainApplication

class AndroidTestTestRunner : AndroidJUnitRunner() {

    override fun newApplication(cl: ClassLoader?, className: String?, context: Context?): Application {
        return super.newApplication(cl, MainApplication::class.java.name, context)
    }

}