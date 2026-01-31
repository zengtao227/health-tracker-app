package com.taotao.healthtracker.domain

data class BpReading(
    val systolic: Int, 
    val diastolic: Int, 
    val heartRate: Int
)

object BpCalculator {
    /**
     * Option A: Drop Max and Min (effectively Median for 3 readings).
     * Good for outlier removal.
     */
    fun calculateMedian(readings: List<BpReading>): BpReading {
        if (readings.isEmpty()) return BpReading(0, 0, 0)
        // Independent median for each component
        val sys = readings.map { it.systolic }.sorted().let { list ->
            if (list.size % 2 == 1) list[list.size / 2] else (list[list.size / 2] + list[list.size / 2 - 1]) / 2
        }
        val dia = readings.map { it.diastolic }.sorted().let { list ->
            if (list.size % 2 == 1) list[list.size / 2] else (list[list.size / 2] + list[list.size / 2 - 1]) / 2
        }
        val hr = readings.map { it.heartRate }.sorted().let { list ->
            if (list.size % 2 == 1) list[list.size / 2] else (list[list.size / 2] + list[list.size / 2 - 1]) / 2
        }
        return BpReading(sys, dia, hr)
    }

    /**
     * Option B: Simple Average.
     */
    fun calculateAverage(readings: List<BpReading>): BpReading {
        if (readings.isEmpty()) return BpReading(0, 0, 0)
        val sys = readings.map { it.systolic }.average().toInt()
        val dia = readings.map { it.diastolic }.average().toInt()
        val hr = readings.map { it.heartRate }.average().toInt()
        return BpReading(sys, dia, hr)
    }

    /**
     * Option C: International Standard (AHA/ESC Guidelines).
     * Discard the 1st reading, take the average of the 2nd and 3rd.
     * Requires at least 2 readings (if 2, usually avg both; if >=3, drop first).
     * Here we assume fixed 3 readings logic: Drop first, avg rest.
     */
    fun calculateInternationalStandard(readings: List<BpReading>): BpReading {
        if (readings.size < 2) return calculateAverage(readings)
        
        // Drop the first reading
        val validReadings = readings.drop(1)
        
        val sys = validReadings.map { it.systolic }.average().toInt()
        val dia = validReadings.map { it.diastolic }.average().toInt()
        val hr = validReadings.map { it.heartRate }.average().toInt()
        
        return BpReading(sys, dia, hr)
    }
}
