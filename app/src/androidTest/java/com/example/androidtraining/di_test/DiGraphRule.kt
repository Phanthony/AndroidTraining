package com.example.androidtraining.di_test

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.androidtraining.ui_test.TestMainApplication
import org.junit.rules.TestWatcher
import org.junit.runner.Description

class DiGraphRule : TestWatcher() {

    lateinit var graph: TestAppGraph
        private set

    override fun starting(description: Description?) {
        val app = ApplicationProvider.getApplicationContext<Context>() as TestMainApplication
        graph = app.appComponent as TestAppGraph
        super.starting(description)
    }

}