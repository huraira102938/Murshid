package com.huraira.murshid.data.repository

import android.content.Context
import android.net.Uri
import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.huraira.murshid.data.model.LibraryContentType
import com.huraira.murshid.data.model.LibraryItem
import com.huraira.murshid.data.remote.FirestoreSchema
import com.huraira.murshid.data.remote.ImageCompressor
import com.huraira.murshid.data.remote.R2Client
import com.huraira.murshid.data.remote.toFirestoreMap
import com.huraira.murshid.data.remote.toLibraryItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Abstracts where Library items come from / go to. [imageUri] is a local content:// URI
 * picked by the admin — the repository is responsible for compressing + uploading it to
 * R2 before writing the Firestore doc. Pass null for the Quote-only type.
 */
interface LibraryRepository {
    suspend fun getAll(): List<LibraryItem>
    suspend fun create(
        type: LibraryContentType,
        quoteText: String?,
        author: String?,
        imageUri: Uri?
    ): Result<LibraryItem>
    suspend fun delete(id: String): Result<Unit>
}

class FirestoreLibraryRepository(
    private val appContext: Context
) : LibraryRepository {

    private val collection = Firebase.firestore.collection(FirestoreSchema.LIBRARY_ITEMS)

    override suspend fun getAll(): List<LibraryItem> = withContext(Dispatchers.IO) {
        val snapshot = collection
            .whereEqualTo(FirestoreSchema.FIELD_ACTIVE, true)
            .orderBy(FirestoreSchema.FIELD_CREATED_AT, Query.Direction.DESCENDING)
            .limit(FirestoreSchema.DEFAULT_PAGE_SIZE)
            .get()
            .await()
        snapshot.documents.mapNotNull { it.toLibraryItem() }
    }

    override suspend fun create(
        type: LibraryContentType,
        quoteText: String?,
        author: String?,
        imageUri: Uri?
    ): Result<LibraryItem> = withContext(Dispatchers.IO) {
        try {
            var thumbnailUrl: String? = null
            var imageUrl: String? = null

            if (imageUri != null) {
                val (thumb, full) = ImageCompressor.compressThumbAndFull(appContext, imageUri).getOrElse {
                    return@withContext Result.failure(it)
                }
                val objectId = UUID.randomUUID().toString()
                thumbnailUrl = R2Client.upload("library/thumb/$objectId.webp", thumb.bytes, thumb.contentType)
                    .getOrElse { return@withContext Result.failure(it) }
                imageUrl = R2Client.upload("library/full/$objectId.webp", full.bytes, full.contentType)
                    .getOrElse { return@withContext Result.failure(it) }
            }

            val docId = UUID.randomUUID().toString()
            val item = LibraryItem(
                id = docId,
                type = type,
                quoteText = quoteText,
                author = author,
                imageUrl = imageUrl,
                thumbnailUrl = thumbnailUrl,
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
            val item = snapshot.toLibraryItem()

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
