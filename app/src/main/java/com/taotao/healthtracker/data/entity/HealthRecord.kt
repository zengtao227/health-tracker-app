package com.taotao.healthtracker.data.entity

import androidx.room.*

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "Me",
    val birthYear: Int = 1990,
    val birthMonth: Int = 1,
    val birthDay: Int = 1,
    val height: Float = 175f,
    val language: String = "zh",
    val insightLanguage: String = "zh" // Independent lang for the bottom card
)

@Entity(tableName = "almanac_data")
data class AlmanacData(
    @PrimaryKey val date: String, // YYYY-MM-DD
    val yi: String,
    val ji: String,
    val lunarDate: String
)

@Entity(tableName = "health_records")
data class HealthRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Int,
    val date: String,
    val sbp: Int?,
    val dbp: Int?,
    val hr: Int?,
    val weight: Float?
)
