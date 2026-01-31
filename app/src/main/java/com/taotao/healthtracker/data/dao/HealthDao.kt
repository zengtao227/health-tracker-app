package com.taotao.healthtracker.data.dao

import androidx.room.*
import com.taotao.healthtracker.data.entity.HealthRecord
import com.taotao.healthtracker.data.entity.UserProfile
import com.taotao.healthtracker.data.entity.AlmanacData
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthDao {
    @Query("SELECT * FROM health_records WHERE userId = :userId ORDER BY date DESC")
    fun getRecordsByUser(userId: Int): Flow<List<HealthRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: HealthRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<HealthRecord>)

    // Profiles
    @Query("SELECT * FROM user_profiles ORDER BY id ASC")
    fun getAllProfiles(): Flow<List<UserProfile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfile)
    
    @Query("DELETE FROM user_profiles WHERE id = :id")
    suspend fun deleteProfile(id: Int)

    // Almanac (Real Huangli) - Reactive Flow
    @Query("SELECT * FROM almanac_data WHERE date = :date LIMIT 1")
    fun getAlmanacByDate(date: String): Flow<AlmanacData?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlmanac(almanac: AlmanacData)
}
