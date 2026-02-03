package com.taotao.healthtracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.taotao.healthtracker.data.HealthRepository
import com.taotao.healthtracker.data.entity.HealthRecord
import com.taotao.healthtracker.data.entity.UserProfile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HealthViewModel(private val repository: HealthRepository) : ViewModel() {

    private val _currentUserId = MutableStateFlow(1)
    val currentUserId: StateFlow<Int> = _currentUserId

    @OptIn(ExperimentalCoroutinesApi::class)
    val allRecords: StateFlow<List<HealthRecord>> = _currentUserId
        .flatMapLatest { id -> repository.getRecordsByUser(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfiles: StateFlow<List<UserProfile>> = repository.getAllProfiles()
        .onEach { profiles ->
            if (profiles.isEmpty()) {
                createNewUser("P1") // Auto-create default user
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentUserProfile: StateFlow<UserProfile?> = combine(userProfiles, _currentUserId) { profiles, id ->
        profiles.find { it.id == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Real Almanac Data Flow (Reactive)
    private val _currentDate = MutableStateFlow(java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()))
    val currentAlmanac = _currentDate.map { date ->
        // 彻底弃用数据库旧缓存，直接使用最新算法
        val parsedDate = try { java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).parse(date) } catch(e: Exception) { null }
        com.taotao.healthtracker.domain.LunarUtils.getLocalAlmanac(parsedDate ?: java.util.Date())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.taotao.healthtracker.domain.LunarUtils.getLocalAlmanac())

    fun switchUser(userId: Int) {
        _currentUserId.value = userId
    }

    fun saveRecord(record: HealthRecord) {
        viewModelScope.launch {
            repository.saveRecord(record.copy(userId = _currentUserId.value))
        }
    }
    
    fun saveProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.saveProfile(profile)
        }
    }

    fun createNewUser(name: String) {
        viewModelScope.launch {
            val profiles = repository.getAllProfiles().first()
            val nextId = (profiles.maxOfOrNull { it.id } ?: 0) + 1
            val newUser = UserProfile(id = nextId, name = name)
            repository.saveProfile(newUser)
            // 提交后立即强制切换当前 ID，防止 UI 跳回
            _currentUserId.emit(nextId)
        }
    }

    fun deleteProfile(userId: Int) {
        viewModelScope.launch {
            repository.deleteProfileWithRecords(userId)
            // 自动切换到另一个可用用户，或重新创建 P1
            val remaining = userProfiles.value.filter { it.id != userId }
            if (remaining.isNotEmpty()) {
                _currentUserId.value = remaining.first().id
            } else {
                createNewUser("P1")
            }
        }
    }

    // Export Logic: Return CSV String
    fun getExportCsv(): String {
        val header = "Date,SBP,DBP,HR,Weight,Glucose,UricAcid\n"
        val rows = allRecords.value.joinToString("\n") { r ->
            "${r.date},${r.sbp ?: ""},${r.dbp ?: ""},${r.hr ?: ""},${r.weight ?: ""},${r.bloodGlucose ?: ""},${r.uricAcid ?: ""}"
        }
        return header + rows
    }

    // Import Logic: Parse CSV String with Auto-Detection
    fun importCsv(content: String) {
        viewModelScope.launch {
            try {
                // Determine format
                val firstLine = content.lineSequence().firstOrNull()?.lowercase() ?: return@launch
                val isFitbit = firstLine.contains("weight grams")
                val isBP = firstLine.contains("sys") && firstLine.contains("dia") && firstLine.contains("pul")
                
                content.lineSequence().forEachIndexed { index, line ->
                    if (index == 0) return@forEachIndexed // Skip header
                    
                    if (line.isNotBlank()) {
                        val parts = line.split(",")
                        if (parts.isNotEmpty()) {
                            val lowerLine = line.lowercase()
                            // Skip re-headers
                            if (lowerLine.contains("date") && lowerLine.contains("sbp")) return@forEachIndexed
                            if (isFitbit && lowerLine.contains("weight grams")) return@forEachIndexed
                            
                            var date = ""
                            var sbp: Int? = null
                            var dbp: Int? = null
                            var hr: Int? = null
                            var wt: Float? = null
                            var glu: Float? = null
                            var uric: Float? = null
                            
                            if (isBP) {
                                // BP Format: DATE,TIME,SYS,DIA,PUL
                                // 01/23/2026,09:12,128,93,59
                                if (parts.size >= 5) {
                                    val d = parts[0].trim()
                                    val t = parts[1].trim()
                                    
                                    // Combine Date Time -> ISO or YYYY-MM-DD
                                    // Input: MM/dd/yyyy
                                    try {
                                        val inputFormat = java.text.SimpleDateFormat("MM/dd/yyyy HH:mm", java.util.Locale.US)
                                        val outputFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                                        val parsed = inputFormat.parse("$d $t")
                                        if (parsed != null) {
                                            date = outputFormat.format(parsed)
                                        }
                                    } catch(e: Exception) {
                                        // Fallback or ignore
                                    }
                                    
                                    sbp = parts[2].trim().toIntOrNull()
                                    dbp = parts[3].trim().toIntOrNull()
                                    hr = parts[4].trim().toIntOrNull()
                                }
                            } else if (isFitbit) {
                                // Fitbit: timestamp,weight grams,data source
                                // 2022-10-20T21:59:59Z,74000,...
                                if (parts.size >= 2) {
                                    val rawDate = parts[0].trim() // ISO
                                    if (rawDate.length >= 10) {
                                        date = rawDate.substring(0, 10)
                                    }
                                    
                                    val grams = parts[1].trim().toDoubleOrNull()
                                    if (grams != null) {
                                        wt = (grams / 1000.0).toFloat()
                                    }
                                }
                            } else {
                                // Standard: Date,SBP,DBP,HR,Weight,Glucose,UricAcid
                                date = parts.getOrNull(0)?.trim() ?: ""
                                if (date.length > 10) date = date.substring(0, 10)
                                
                                sbp = parts.getOrNull(1)?.trim()?.toIntOrNull()
                                dbp = parts.getOrNull(2)?.trim()?.toIntOrNull()
                                hr = parts.getOrNull(3)?.trim()?.toIntOrNull()
                                wt = parts.getOrNull(4)?.trim()?.toFloatOrNull()
                                glu = parts.getOrNull(5)?.trim()?.toFloatOrNull()
                                uric = parts.getOrNull(6)?.trim()?.toFloatOrNull()
                            }
                            
                            if (date.isNotEmpty()) {
                                repository.saveRecord(HealthRecord(
                                    userId = _currentUserId.value,
                                    date = date, sbp = sbp, dbp = dbp, hr = hr, weight = wt,
                                    bloodGlucose = glu, uricAcid = uric
                                ))
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun deleteRecords(records: List<HealthRecord>) {
        viewModelScope.launch {
            records.forEach { repository.deleteRecord(it) }
        }
    }
}

class HealthViewModelFactory(private val repository: HealthRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HealthViewModel::class.java)) return HealthViewModel(repository) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
