package com.huraira.murshid.data.repository

import android.content.Context
import android.net.Uri
import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.huraira.murshid.data.model.WallpaperItem
import com.huraira.murshid.data.remote.FirestoreSchema
import com.huraira.murshid.data.remote.ImageCompressor
import com.huraira.murshid.data.remote.R2Client
import com.huraira.murshid.data.remote.toFirestoreMap
import com.huraira.murshid.data.remote.toWallpaperItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Abstracts where wallpapers come from / go to. UI code should only ever depend on this
 * interface — [FirestoreWallpaperRepository] is the real implementation (Firestore for
 * metadata, R2 for the actual image bytes).
 */
interface WallpaperRepository {
    suspend fun getAll(): List<WallpaperItem>
    suspend fun upload(title: String, category: String, imageUri: Uri): Result<WallpaperItem>
    suspend fun delete(id: String): Result<Unit>
}

class FirestoreWallpaperRepository(
    private val appContext: Context
) : WallpaperRepository {

    private val collection = Firebase.firestore.collection(FirestoreSchema.WALLPAPERS)

    override suspend fun getAll(): List<WallpaperItem> = withContext(Dispatchers.IO) {
        val snapshot = collection
            .whereEqualTo(FirestoreSchema.FIELD_ACTIVE, true)
            .orderBy(FirestoreSchema.FIELD_CREATED_AT, Query.Direction.DESCENDING)
            .limit(FirestoreSchema.DEFAULT_PAGE_SIZE)
            .get()
            .await()
        snapshot.documents.mapNotNull { it.toWallpaperItem() }
    }

    override suspend fun upload(title: String, category: String, imageUri: Uri): Result<WallpaperItem> =
        withContext(Dispatchers.IO) {
            try {
                val (thumb, full) = ImageCompressor.compressThumbAndFull(appContext, imageUri).getOrElse {
                    return@withContext Result.failure(it)
                }

                val objectId = UUID.randomUUID().toString()
                val thumbUrl = R2Client.upload("wallpapers/thumb/$objectId.webp", thumb.bytes, thumb.contentType)
                    .getOrElse { return@withContext Result.failure(it) }
                val fullUrl = R2Client.upload("wallpapers/full/$objectId.webp", full.bytes, full.contentType)
                    .getOrElse { return@withContext Result.failure(it) }

                val item = WallpaperItem(
                    id = objectId,
                    title = title,
                    imageUrl = fullUrl,
                    thumbnailUrl = thumbUrl,
                    category = category,
                    createdAt = System.currentTimeMillis()
                )
                collection.document(objectId).set(item.toFirestoreMap()).await()
                Result.success(item)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun delete(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val docRef = collection.document(id)
            val snapshot = docRef.get().await()
            val item = snapshot.toWallpaperItem()

            // Delete the actual R2 objects first. If either fails, we still remove the
            // Firestore doc below rather than leaving a zombie entry the admin can't
            // re-delete — a stray R2 object with no doc pointing at it is harmless.
            item?.thumbnailUrl?.let { url ->
                R2Client.keyFromPublicUrl(url)?.let { key -> R2Client.delete(key) }
            }
            item?.imageUrl?.let { url ->
                R2Client.keyFromPublicUrl(url)?.let { key -> R2Client.delete(key) }
            }

            docRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
