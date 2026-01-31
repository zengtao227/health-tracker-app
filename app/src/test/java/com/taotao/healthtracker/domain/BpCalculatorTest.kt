package com.taotao.healthtracker.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class BpCalculatorTest {

    @Test
    fun calculateMedian_standardCase() {
        val readings = listOf(
            BpReading(120, 80, 70),
            BpReading(130, 85, 75),
            BpReading(125, 82, 72)
        )
        // Sorted Sys: 120, 125, 130 -> Median 125
        // Sorted Dia: 80, 82, 85 -> Median 82
        // Sorted HR: 70, 72, 75 -> Median 72
        val result = BpCalculator.calculateMedian(readings)
        assertEquals(125, result.systolic)
        assertEquals(82, result.diastolic)
        assertEquals(72, result.heartRate)
    }

    @Test
    fun calculateMedian_withOutlier() {
        val readings = listOf(
            BpReading(150, 90, 100), // Anxiety spike (First)
            BpReading(120, 80, 70),
            BpReading(122, 81, 71)
        )
        // Sorted Sys: 120, 122, 150 -> Median 122 (Ignores 150)
        val result = BpCalculator.calculateMedian(readings)
        assertEquals(122, result.systolic)
        assertEquals(81, result.diastolic)
    }

    @Test
    fun calculateAverage_standardCase() {
        val readings = listOf(
            BpReading(120, 80, 70),
            BpReading(130, 85, 75),
            BpReading(125, 82, 72) // Sum: 375, 247, 217 -> Avg: 125, 82.3->82, 72.3->72
        )
        val result = BpCalculator.calculateAverage(readings)
        assertEquals(125, result.systolic)
        assertEquals(82, result.diastolic)
    }
}
