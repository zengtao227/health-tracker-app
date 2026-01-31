package com.taotao.healthtracker.domain

import com.taotao.healthtracker.data.entity.HealthRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class RawMeasurement(
    val sbp: Int,
    val dbp: Int,
    val hr: Int,
    val weight: Float
)

object RecordCalculator {

    fun calculateFinalResult(measurements: List<RawMeasurement>): RawMeasurement? {
        if (measurements.isEmpty()) return null

        return when (measurements.size) {
            1 -> measurements[0]
            2 -> measurements[1]
            else -> {
                // 3 or more: Drop 1st, Average 2nd and 3rd
                val second = measurements[1]
                val third = measurements[2]
                
                RawMeasurement(
                    sbp = (second.sbp + third.sbp) / 2,
                    dbp = (second.dbp + third.dbp) / 2,
                    hr = (second.hr + third.hr) / 2,
                    weight = (second.weight + third.weight) / 2f
                )
            }
        }
    }
}
