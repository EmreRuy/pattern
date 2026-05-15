package com.example.pattern.notifications

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Lead Expert Implementation:
 * DailyEngagementWorker handles periodic notifications using WorkManager.
 * This is the battery-efficient way to send "sometimes" notifications.
 */
@HiltWorker
class DailyEngagementWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val notificationHelper: NotificationHelper,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val tips = listOf(
            "Consistency is key. Even 5 minutes counts!" to "Check in on your habits today.",
            "Track your progress" to "Seeing your streak grow is the best motivation.",
            "Small wins lead to big changes" to "Keep going, you're doing great!",
            "Habit Tip" to "Try 'Habit Stacking' by linking a new habit to an old one."
        )

        val randomTip = tips.random()
        
        notificationHelper.showGeneralNotification(
            title = randomTip.first,
            message = randomTip.second
        )

        return Result.success()
    }
}
