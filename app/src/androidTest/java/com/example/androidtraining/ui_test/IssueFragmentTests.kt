package com.example.androidtraining.ui_test

import android.content.SharedPreferences
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import com.example.androidtraining.MainActivity
import com.example.androidtraining.ui_test.di_test.DiGraphRule
import org.junit.Before
import org.junit.Rule
import javax.inject.Inject

class IssueFragmentTests {
    @Inject
    lateinit var mockWebServer: MockWebServer

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @get:Rule
    val diGraph = DiGraphRule()

    @Before
    fun setup() {
        diGraph.graph.inject(this)
        ActivityScenario.launch(MainActivity::class.java)
    }
}