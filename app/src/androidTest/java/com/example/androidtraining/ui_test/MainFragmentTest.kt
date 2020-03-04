package com.example.androidtraining.ui_test

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import com.example.androidtraining.ui_test.di_test.DiGraphRule
import com.google.common.truth.Truth
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 18)
class MainFragmentTest {

    @Inject
    lateinit var mockWebServer: MockWebServer

    @get:Rule
    val diGraph = DiGraphRule()

    @Before
    fun setup() {
        diGraph.graph.inject(this)
    }

    @Test
    fun test_simpleTest() {
        mockWebServer.queue(200, "")
        Truth.assertThat("").isEqualTo("")
    }

}