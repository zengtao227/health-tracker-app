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
    val currentAlmanac = _currentDate.flatMapLatest { date ->
        repository.getAlmanac(date).map { dbData ->
            if (dbData != null) {
                com.taotao.healthtracker.domain.LunarUtils.AlmanacResult(dbData.lunarDate, dbData.yi, dbData.ji)
            } else {
                com.taotao.healthtracker.domain.LunarUtils.getLocalAlmanac(date)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.taotao.healthtracker.domain.LunarUtils.getLocalAlmanac(_currentDate.value))

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
            val nextId = (userProfiles.value.maxOfOrNull { it.id } ?: 0) + 1
            repository.saveProfile(UserProfile(id = nextId, name = name))
            _currentUserId.value = nextId
        }
    }

    // Export Logic: Return CSV String
    fun getExportCsv(): String {
        val header = "Date,SBP,DBP,HR,Weight\n"
        val rows = allRecords.value.joinToString("\n") { r ->
            "${r.date},${r.sbp ?: ""},${r.dbp ?: ""},${r.hr ?: ""},${r.weight ?: ""}"
        }
        return header + rows
    }

    // Import Logic: Parse CSV String
    fun importCsv(content: String) {
        viewModelScope.launch {
            try {
                content.lineSequence().drop(1).forEach { line -> // Skip header
                    if (line.isNotBlank()) {
                        val parts = line.split(",")
                        if (parts.isNotEmpty()) {
                            // Basic parsing, assuming standard order: Date,SBP,DBP,HR,Weight
                            val date = parts.getOrNull(0)?.trim() ?: ""
                            val sbp = parts.getOrNull(1)?.trim()?.toIntOrNull()
                            val dbp = parts.getOrNull(2)?.trim()?.toIntOrNull()
                            val hr = parts.getOrNull(3)?.trim()?.toIntOrNull()
                            val wt = parts.getOrNull(4)?.trim()?.toFloatOrNull()
                            
                            if (date.isNotEmpty()) {
                                repository.saveRecord(HealthRecord(
                                    userId = _currentUserId.value,
                                    date = date, sbp = sbp, dbp = dbp, hr = hr, weight = wt
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
}

class HealthViewModelFactory(private val repository: HealthRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HealthViewModel::class.java)) return HealthViewModel(repository) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
