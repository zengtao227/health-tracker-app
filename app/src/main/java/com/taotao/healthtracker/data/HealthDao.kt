package com.taotao.healthtracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

import com.taotao.healthtracker.data.entity.HealthRecord

@Dao
interface HealthDao {
    @Query("SELECT * FROM health_records ORDER BY date DESC")
    fun getAllRecords(): Flow<List<HealthRecord>>

    @Query("SELECT * FROM health_records WHERE date = :date LIMIT 1")
    suspend fun getRecordByDate(date: String): HealthRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: HealthRecord)

    @Query("DELETE FROM health_records")
    suspend fun deleteAll()

    @Transaction
    suspend fun bulkInsert(records: List<HealthRecord>) {
        records.forEach { insertRecord(it) }
    }
}
