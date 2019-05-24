package com.example.androidtraining

import androidx.lifecycle.MutableLiveData
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class TimeInformationTests{

    lateinit var mTime: TimeInformation
    var minsSince = MutableLiveData<Int>()

    @Before
    fun setup(){
        mTime = TimeInformation(minsSince)
    }


    @Test
    fun `timePassed minutes 1`(){
        //Format of Time is hour:minute:day of year
        val initialTime = "12:36:182"
        val currentTime = "12:40:182"
        val expectedTime = 4

        assertEquals(mTime.timePassed(initialTime,currentTime),expectedTime)
    }

    @Test
    fun `timePassed minutes 2`(){
        //Format of Time is hour:minute:day of year
        val initialTime = "12:16:182"
        val currentTime = "12:49:182"
        val expectedTime = 33

        assertEquals(mTime.timePassed(initialTime,currentTime),expectedTime)
    }

    @Test
    fun `timePassed minutes 3`(){
        //Format of Time is hour:minute:day of year
        val initialTime = "12:40:182"
        val currentTime = "12:40:182"
        val expectedTime = 0

        assertEquals(mTime.timePassed(initialTime,currentTime),expectedTime)
    }

    @Test
    fun `timePassed hours 1`(){
        //Format of Time is hour:minute:day of year
        val initialTime = "11:36:182"
        val currentTime = "12:40:182"
        val expectedTime = 64

        assertEquals(mTime.timePassed(initialTime,currentTime),expectedTime)
    }

    @Test
    fun `timePassed hours 2`(){
        //Format of Time is hour:minute:day of year
        val initialTime = "9:40:182"
        val currentTime = "12:40:182"
        val expectedTime = 180

        assertEquals(mTime.timePassed(initialTime,currentTime),expectedTime)
    }

    @Test
    fun `timePassed hours 3`(){
        //Format of Time is hour:minute:day of year
        val initialTime = "10:36:182"
        val currentTime = "12:20:182"
        val expectedTime = 104

        assertEquals(mTime.timePassed(initialTime,currentTime),expectedTime)
    }

    @Test
    fun `timePassed days 1`(){
        //Format of Time is hour:minute:day of year
        val initialTime = "12:36:182"
        val currentTime = "12:40:190"
        val expectedTime = 11524

        assertEquals(mTime.timePassed(initialTime,currentTime),expectedTime)
    }

    @Test
    fun `timePassed days 2`(){
        //Format of Time is hour:minute:day of year
        val initialTime = "9:36:82"
        val currentTime = "12:40:190"
        val expectedTime = 155704

        assertEquals(mTime.timePassed(initialTime,currentTime),expectedTime)
    }

    @Test
    fun `timePassed days 3`(){
        //Format of Time is hour:minute:day of year
        val initialTime = "18:57:67"
        val currentTime = "12:17:190"
        val expectedTime = 176720

        assertEquals(mTime.timePassed(initialTime,currentTime),expectedTime)
    }

}