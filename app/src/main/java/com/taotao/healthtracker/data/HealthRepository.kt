package com.taotao.healthtracker.data

import com.taotao.healthtracker.data.dao.HealthDao
import com.taotao.healthtracker.data.entity.HealthRecord
import com.taotao.healthtracker.data.entity.UserProfile
import kotlinx.coroutines.flow.Flow

class HealthRepository(private val healthDao: HealthDao) {

    fun getRecordsByUser(userId: Int): Flow<List<HealthRecord>> = healthDao.getRecordsByUser(userId)
    
    fun getAllProfiles(): Flow<List<UserProfile>> = healthDao.getAllProfiles()
    
    suspend fun saveRecord(record: HealthRecord) {
        healthDao.insertRecord(record)
    }
    
    suspend fun saveProfile(profile: UserProfile) {
        healthDao.insertProfile(profile)
    }
    
    suspend fun bulkImport(records: List<HealthRecord>) {
        healthDao.insertAll(records)
    }

    fun getAlmanac(date: String) = healthDao.getAlmanacByDate(date)
    suspend fun saveAlmanac(almanac: com.taotao.healthtracker.data.entity.AlmanacData) = healthDao.insertAlmanac(almanac)

    suspend fun deleteProfileWithRecords(userId: Int) {
        healthDao.deleteRecordsByUser(userId)
        healthDao.deleteProfile(userId)
    }
}
