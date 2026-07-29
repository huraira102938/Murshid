package com.huraira.murshid.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import coil3.SingletonImageLoader
import coil3.request.ImageRequest
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.huraira.murshid.data.remote.FirestoreSchema
import com.huraira.murshid.data.remote.LastSyncStore
import kotlinx.coroutines.tasks.await

/**
 * Periodic background sync. Instead of the app hitting Firestore fresh on every launch,
 * this runs on a schedule (or can be triggered by an FCM data message — see
 * MurshidMessagingService) and only asks for documents newer than the last time each
 * collection was checked. New thumbnails get prefetched into Coil's disk cache so the
 * next time the user opens the app, images are already local.
 */
class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val lastSyncStore = LastSyncStore(applicationContext)

    override suspend fun doWork(): Result {
        return try {
            syncCollection(FirestoreSchema.WALLPAPERS)
            syncCollection(FirestoreSchema.LIBRARY_ITEMS)
            syncCollection(FirestoreSchema.UPDATES)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private suspend fun syncCollection(collectionName: String) {
        val since = lastSyncStore.get(collectionName)

        // Bounded query: only docs newer than last sync, capped at 50 — never unbounded.
        val snapshot = Firebase.firestore.collection(collectionName)
            .whereEqualTo(FirestoreSchema.FIELD_ACTIVE, true)
            .whereGreaterThan(FirestoreSchema.FIELD_CREATED_AT, since)
            .limit(50)
            .get()
            .await()

        val imageLoader = SingletonImageLoader.get(applicationContext)
        snapshot.documents.forEach { doc ->
            val thumbUrl = doc.getString("thumbnailUrl")
            if (!thumbUrl.isNullOrBlank()) {
                try {
                    val request = ImageRequest.Builder(applicationContext).data(thumbUrl).build()
                    imageLoader.execute(request)
                } catch (e: Exception) {
                    // A single bad prefetch shouldn't fail the whole sync pass.
                }
            }
        }

        lastSyncStore.set(collectionName, System.currentTimeMillis())
    }
}
