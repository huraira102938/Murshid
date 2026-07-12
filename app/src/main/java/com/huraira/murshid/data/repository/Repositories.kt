package com.huraira.murshid.data.repository

/**
 * Single place that wires concrete repository implementations to their interfaces.
 * Swapping the dummy implementations for real Firestore/R2/FCM-backed ones later is a
 * one-line change here — no UI or ViewModel code needs to change.
 */
object Repositories {
    val wallpaper: WallpaperRepository = DummyWallpaperRepository()
    val library: LibraryRepository = DummyLibraryRepository()
    val updates: UpdatesRepository = DummyUpdatesRepository()
    val notification: NotificationRepository = DummyNotificationRepository()
    val category: CategoryRepository = DummyCategoryRepository()
}
