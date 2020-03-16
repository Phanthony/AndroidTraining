package com.example.androidtraining

import java.text.SimpleDateFormat
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject

class DayInformation @Inject constructor() : Day {
    override fun getYesterday(): String {
        val yesterday = Calendar.getInstance().apply {
            add(Calendar.DATE, -1)
        }.time

        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return format.format(yesterday)
    }
}

interface Day {
    fun getYesterday(): String
}