package com.huraira.murshid.data.repository

import android.content.Context

/**
 * Single place that wires concrete repository implementations to their interfaces.
 * Must be initialized once via [init] (done in MurshidApplication.onCreate) before any
 * repository is accessed, since the Firestore/R2-backed implementations need an
 * application Context to read picked images off content:// URIs.
 */
object Repositories {

    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    val wallpaper: WallpaperRepository by lazy { FirestoreWallpaperRepository(appContext) }
    val library: LibraryRepository by lazy { FirestoreLibraryRepository(appContext) }
    val updates: UpdatesRepository by lazy { FirestoreUpdatesRepository(appContext) }
    val category: CategoryRepository by lazy { FirestoreCategoryRepository() }
    val notification: NotificationRepository by lazy { FirestoreNotificationRepository() }
}
