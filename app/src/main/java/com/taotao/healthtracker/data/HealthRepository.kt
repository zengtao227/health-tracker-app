package com.taotao.healthtracker.data

import com.taotao.healthtracker.data.dao.HealthDao
import com.taotao.healthtracker.data.entity.HealthRecord
import com.taotao.healthtracker.data.entity.UserProfile
import com.taotao.healthtracker.data.entity.AlmanacData
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

    fun getAlmanac(date: String): Flow<AlmanacData?> = healthDao.getAlmanacByDate(date)
    
    suspend fun saveAlmanac(almanac: AlmanacData) = healthDao.insertAlmanac(almanac)

    suspend fun deleteProfileWithRecords(userId: Int) {
        healthDao.deleteRecordsByUser(userId)
        healthDao.deleteProfile(userId)
    }

    suspend fun deleteRecord(record: HealthRecord) {
        healthDao.deleteRecord(record)
    }
}
