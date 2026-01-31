package com.taotao.healthtracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blood_pressure_readings")
data class BloodPressure(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val systolic: Int,
    val diastolic: Int,
    val heartRate: Int? = null,
    val note: String? = null
)

@Entity(tableName = "body_metrics")
data class BodyMetric(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val weight: Float? = null,
    val height: Float? = null,
    val note: String? = null
)

enum class GlucoseUnit { MGDL, MMOL }

@Entity(tableName = "glucose_readings")
data class GlucoseReading(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val glucoseValue: Float,
    val unit: GlucoseUnit,
    val note: String? = null
)
