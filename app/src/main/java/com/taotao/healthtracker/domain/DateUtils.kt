package com.taotao.healthtracker.domain

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateUtils {
    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun getCurrentDate(): String {
        return sdf.format(Date())
    }

    fun getDaysDiff(dateStr: String): Long {
        return try {
            val date = sdf.parse(dateStr.take(10)) ?: return 999
            val diff = Date().time - date.time
            TimeUnit.MILLISECONDS.toDays(diff)
        } catch (e: Exception) {
            999
        }
    }

    fun formatTimestamp(timestamp: Long): String {
        return sdf.format(Date(timestamp))
    }
}
