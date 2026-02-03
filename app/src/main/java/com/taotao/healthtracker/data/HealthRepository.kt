package com.taotao.healthtracker.data

import com.taotao.healthtracker.data.entity.HealthRecord
import kotlinx.coroutines.flow.Flow

class HealthRepository(private val healthDao: HealthDao) {
    val allRecords: Flow<List<HealthRecord>> = healthDao.getAllRecords()

    suspend fun saveRecord(record: HealthRecord) {
        healthDao.insertRecord(record)
    }
    
    suspend fun getRecordByDate(date: String): HealthRecord? {
        return healthDao.getRecordByDate(date)
    }
    
    suspend fun bulkImport(records: List<HealthRecord>) {
        healthDao.bulkInsert(records)
    }
}
