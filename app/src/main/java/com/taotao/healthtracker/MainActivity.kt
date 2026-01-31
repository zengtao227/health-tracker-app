package com.taotao.healthtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.taotao.healthtracker.data.AppDatabase
import com.taotao.healthtracker.data.HealthRepository
import com.taotao.healthtracker.ui.HealthTrackerNavHost
import com.taotao.healthtracker.ui.theme.HealthTrackerTheme
import com.taotao.healthtracker.viewmodel.HealthViewModel
import com.taotao.healthtracker.viewmodel.HealthViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val database = AppDatabase.getDatabase(this)
        val repository = HealthRepository(database.healthDao())
        val viewModelFactory = HealthViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, viewModelFactory)[HealthViewModel::class.java]
        
        setContent {
            HealthTrackerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HealthTrackerNavHost(viewModel)
                }
            }
        }
    }
}
