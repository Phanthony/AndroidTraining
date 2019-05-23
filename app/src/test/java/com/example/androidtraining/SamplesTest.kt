package com.example.androidtraining

import android.content.SharedPreferences
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class SimpleTests {

    @Mock private lateinit var sharedPreferences: SharedPreferences

    fun addTogether(first: Int, second: Int): Int {
        return first + second
    }

    @Test
    fun simpleTest_math() {
        val givenFirst = 2
        val givenSecond = 2
        val expected = 4

        assertThat(addTogether(givenFirst, givenSecond)).isEqualTo(expected)
    }

    fun getStringFromSharedPreferences(sharedPrefs: SharedPreferences, key: String): String? {
        return sharedPrefs.getString(key, null)
    }

    @Test
    fun simpleTest_mocks() {
        val key = "key"
        val expected = "expected-value"

        whenever(sharedPreferences.getString(eq(key), anyOrNull())).thenReturn(expected)

        assertThat(getStringFromSharedPreferences(sharedPreferences, key)).isEqualTo(expected)
    }

}