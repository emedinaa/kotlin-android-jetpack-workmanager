package com.emedinaa.kworkmanager.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.emedinaa.kworkmanager.R
import timber.log.Timber

abstract class BaseWorker(ctx: Context, params: WorkerParameters):Worker(ctx,params) {

    private val VERBOSE_NOTIFICATION_CHANNEL_NAME: CharSequence =
        "Verbose WorkManager Notifications"
    private val VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION =
        "Shows notifications whenever work starts"
    private val NOTIFICATION_TITLE: CharSequence = "WorkRequest Starting"
    private val CHANNEL_ID = "VERBOSE_NOTIFICATION"
    private val NOTIFICATION_ID = 1

    private val DELAY_TIME_MILLIS: Long = 3000
    protected val KEY_IMAGE_URI = "KEY_IMAGE_URI"

    override fun doWork(): Result {
        return Result.failure()
    }

    protected fun sleep() {
        try {
            Thread.sleep(DELAY_TIME_MILLIS, 0)
        } catch (e: InterruptedException) {
            Timber.e(e)
        }

    }
    protected fun makeStatusNotification(message: String, context: Context) {

        // Make a channel if necessary
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            val name = VERBOSE_NOTIFICATION_CHANNEL_NAME
            val description = VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = description

            // Add the channel
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?

            notificationManager?.createNotificationChannel(channel)
        }

        // Create the notification
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(NOTIFICATION_TITLE)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(LongArray(0))

        // Show the notification
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
    }

}