package com.huraira.murshid.data.repository

import android.content.Context
import android.net.Uri
import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.huraira.murshid.data.model.UpdateItem
import com.huraira.murshid.data.remote.FirestoreSchema
import com.huraira.murshid.data.remote.ImageCompressor
import com.huraira.murshid.data.remote.R2Client
import com.huraira.murshid.data.remote.toFirestoreMap
import com.huraira.murshid.data.remote.toUpdateItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Abstracts where Updates come from / go to. [detailImageUri] is a local content:// URI
 * picked by the admin (optional) — compressed + uploaded to R2 before the Firestore write.
 */
interface UpdatesRepository {
    suspend fun getAll(): List<UpdateItem>
    suspend fun create(
        title: String,
        date: String,
        summary: String,
        fullContent: String,
        detailImageUri: Uri?,
        youtubeVideoId: String?
    ): Result<UpdateItem>
    suspend fun delete(id: String): Result<Unit>
}

class FirestoreUpdatesRepository(
    private val appContext: Context
) : UpdatesRepository {

    private val collection = Firebase.firestore.collection(FirestoreSchema.UPDATES)

    override suspend fun getAll(): List<UpdateItem> = withContext(Dispatchers.IO) {
        val snapshot = collection
            .whereEqualTo(FirestoreSchema.FIELD_ACTIVE, true)
            .orderBy(FirestoreSchema.FIELD_CREATED_AT, Query.Direction.DESCENDING)
            .limit(FirestoreSchema.DEFAULT_PAGE_SIZE)
            .get()
            .await()
        snapshot.documents.mapNotNull { it.toUpdateItem() }
    }

    override suspend fun create(
        title: String,
        date: String,
        summary: String,
        fullContent: String,
        detailImageUri: Uri?,
        youtubeVideoId: String?
    ): Result<UpdateItem> = withContext(Dispatchers.IO) {
        try {
            var thumbnailUrl: String? = null
            var detailImageUrl: String? = null

            if (detailImageUri != null) {
                val (thumb, full) = ImageCompressor.compressThumbAndFull(appContext, detailImageUri).getOrElse {
                    return@withContext Result.failure(it)
                }
                val objectId = UUID.randomUUID().toString()
                thumbnailUrl = R2Client.upload("updates/thumb/$objectId.webp", thumb.bytes, thumb.contentType)
                    .getOrElse { return@withContext Result.failure(it) }
                detailImageUrl = R2Client.upload("updates/full/$objectId.webp", full.bytes, full.contentType)
                    .getOrElse { return@withContext Result.failure(it) }
            }

            val docId = UUID.randomUUID().toString()
            val item = UpdateItem(
                id = docId,
                title = title,
                date = date,
                thumbnailUrl = thumbnailUrl,
                summary = summary,
                fullContent = fullContent,
                detailImageUrl = detailImageUrl,
                youtubeVideoId = youtubeVideoId,
                createdAt = System.currentTimeMillis()
            )
            collection.document(docId).set(item.toFirestoreMap()).await()
            Result.success(item)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun delete(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val docRef = collection.document(id)
            val snapshot = docRef.get().await()
            val item = snapshot.toUpdateItem()

            item?.thumbnailUrl?.let { url ->
                R2Client.keyFromPublicUrl(url)?.let { key -> R2Client.delete(key) }
            }
            item?.detailImageUrl?.let { url ->
                R2Client.keyFromPublicUrl(url)?.let { key -> R2Client.delete(key) }
            }

            docRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
