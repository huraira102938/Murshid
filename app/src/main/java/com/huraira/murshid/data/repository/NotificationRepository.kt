package com.huraira.murshid.data.repository

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.huraira.murshid.data.remote.FirestoreSchema
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Abstracts sending a push notification to app users.
 *
 * A mobile client can never hold real FCM "send" credentials safely (that requires the
 * Firebase Admin SDK / a service account, which must never ship inside an APK). So this
 * repository just writes a request document to Firestore — a small Cloud Function
 * (see /functions in the repo root) triggers on create and does the actual FCM send to
 * the "all_users" topic. This keeps zero send-capable secrets on the device.
 */
interface NotificationRepository {
    suspend fun send(title: String, body: String): Result<Unit>
}

class FirestoreNotificationRepository : NotificationRepository {

    private val collection = Firebase.firestore.collection(FirestoreSchema.NOTIFICATION_REQUESTS)

    override suspend fun send(title: String, body: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            collection.add(
                mapOf(
                    "title" to title,
                    "body" to body,
                    "createdAt" to System.currentTimeMillis(),
                    "status" to "pending"
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
