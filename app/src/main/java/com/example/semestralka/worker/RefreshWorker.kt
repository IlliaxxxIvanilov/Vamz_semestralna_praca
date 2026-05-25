package com.example.semestralka.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.semestralka.repository.PlacesRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject


@HiltWorker
class RefreshWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: PlacesRepository,
    private val notificationHelper: RefreshNotificationHelper
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_TAG = "zilina_guide_refresh"
    }

    override suspend fun doWork(): Result {
        return try {
            repository.refreshPlaces()
            notificationHelper.showRefreshNotification()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}