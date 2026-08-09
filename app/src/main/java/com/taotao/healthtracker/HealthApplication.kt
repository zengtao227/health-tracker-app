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
        // AlmanacWorker only computes lunar calendar data locally (lunar-java) and
        // writes to the local Room database — it never touches the network, so it
        // must not require one. The former NetworkType.CONNECTED constraint just
        // pulled in the ACCESS_NETWORK_STATE permission for no reason.
        val syncRequest = PeriodicWorkRequestBuilder<AlmanacWorker>(
            1, TimeUnit.DAYS // Check once a day
        )
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
