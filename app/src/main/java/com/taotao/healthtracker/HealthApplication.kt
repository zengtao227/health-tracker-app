package com.taotao.healthtracker

import android.app.Application
import androidx.work.*
import com.taotao.healthtracker.worker.AlmanacWorker
import java.util.concurrent.TimeUnit

class HealthApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        setupBackgroundSync()
    }

    private fun setupBackgroundSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // Only sync when online
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<AlmanacWorker>(
            1, TimeUnit.DAYS // Check once a day
        )
        .setConstraints(constraints)
        .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "AlmanacSync",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
        
        // Trigger immediate 'OneTime' sync for demo purposes
        val oneTime = OneTimeWorkRequestBuilder<AlmanacWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()
        WorkManager.getInstance(this).enqueue(oneTime)
    }
}
