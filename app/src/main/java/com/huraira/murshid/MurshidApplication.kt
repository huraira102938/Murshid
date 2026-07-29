package com.huraira.murshid

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.google.firebase.messaging.FirebaseMessaging
import com.huraira.murshid.data.repository.Repositories
import com.huraira.murshid.sync.MurshidMessagingService
import com.huraira.murshid.sync.SyncWorker
import okio.Path.Companion.toOkioPath
import java.util.concurrent.TimeUnit

class MurshidApplication : Application(), SingletonImageLoader.Factory {

    override fun onCreate() {
        super.onCreate()
        Repositories.init(this)
        createNotificationChannel()
        subscribeToFcmTopics()
        schedulePeriodicSync()
    }

    /**
     * Wallpapers/images never change once published, so a generous, long-lived disk
     * cache means repeat views (grid re-scrolls, reopening the app) never re-hit R2.
     */
    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .components { add(OkHttpNetworkFetcherFactory()) }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("murshid_image_cache").toOkioPath())
                    .maxSizeBytes(300L * 1024 * 1024) // 300MB — generous for a wallpaper-heavy app
                    .build()
            }
            .build()

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            MurshidMessagingService.NOTIFICATION_CHANNEL_ID,
            "Murshid Updates",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "New wallpapers, library content, and announcements."
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun subscribeToFcmTopics() {
        FirebaseMessaging.getInstance().subscribeToTopic(MurshidMessagingService.ALL_USERS_TOPIC)
        FirebaseMessaging.getInstance().subscribeToTopic(MurshidMessagingService.CONTENT_UPDATES_TOPIC)
    }

    private fun schedulePeriodicSync() {
        val request = PeriodicWorkRequestBuilder<SyncWorker>(12, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            )
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "murshid_content_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
