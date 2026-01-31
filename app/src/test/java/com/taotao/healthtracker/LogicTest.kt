package com.taotao.healthtracker

import com.taotao.healthtracker.domain.RawMeasurement
import com.taotao.healthtracker.domain.RecordCalculator
import org.junit.Assert.assertEquals
import org.junit.Test

class LogicTest {

    @Test
    fun testOneMeasurement() {
        val m1 = RawMeasurement(120, 80, 70, 70f)
        val result = RecordCalculator.calculateFinalResult(listOf(m1))
        assertEquals(m1, result)
    }

    @Test
    fun testTwoMeasurements() {
        val m1 = RawMeasurement(130, 90, 80, 71f) // Should drop
        val m2 = RawMeasurement(120, 80, 70, 70f) // Should keep
        val result = RecordCalculator.calculateFinalResult(listOf(m1, m2))
        assertEquals(m2, result)
    }

    @Test
    fun testThreeMeasurements() {
        val m1 = RawMeasurement(150, 100, 100, 75f) // Should drop
        val m2 = RawMeasurement(120, 80, 70, 70f)
        val m3 = RawMeasurement(122, 82, 72, 72f)
        
        // Avg of m2 and m3
        // SBP: (120+122)/2 = 121
        // DBP: (80+82)/2 = 81
        // HR: (70+72)/2 = 71
        // Wt: (70+72)/2 = 71
        
        val result = RecordCalculator.calculateFinalResult(listOf(m1, m2, m3))
        assertEquals(121, result?.sbp)
        assertEquals(81, result?.dbp)
        assertEquals(71, result?.hr)
        assertEquals(71f, result?.weight)
    }
}
