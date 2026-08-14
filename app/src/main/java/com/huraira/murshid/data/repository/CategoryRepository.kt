package com.huraira.murshid.data.repository

import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.firestore
import com.huraira.murshid.data.remote.FirestoreSchema
import com.huraira.murshid.data.remote.R2Client
import com.huraira.murshid.data.remote.toWallpaperItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Abstracts wallpaper categories. Categories only apply to the Wallpapers section.
 * Deleting a category is destructive — it deletes every wallpaper tagged with it,
 * including their R2 image files — so it's gated behind a password and refuses to drop
 * the last remaining category.
 *
 * Categories also carry an admin-controlled display order — this is what determines
 * which category chip appears first for end users on the Wallpapers screen.
 *
 * TODO(hardening): move the password check server-side (e.g. a callable Cloud Function)
 * before wide release — this is only acceptable because the whole admin flow is built
 * out of the public Play Store build, gated by the `// region ADMIN` blocks.
 */
interface CategoryRepository {
    suspend fun getAll(): List<String>
    suspend fun add(name: String): Result<Unit>
    suspend fun delete(name: String, password: String): Result<Unit>
    suspend fun moveUp(name: String): Result<Unit>
    suspend fun moveDown(name: String): Result<Unit>
}

class FirestoreCategoryRepository : CategoryRepository {

    private val categoriesCollection = Firebase.firestore.collection(FirestoreSchema.CATEGORIES)
    private val wallpapersCollection = Firebase.firestore.collection(FirestoreSchema.WALLPAPERS)

    /**
     * Sorted client-side (not via Firestore's .orderBy("order")) on purpose — Firestore's
     * orderBy excludes documents missing that field entirely, which would silently drop
     * any category created before this ordering feature existed. Docs without an explicit
     * order fall back to the end, tie-broken by creation time.
     */
    private suspend fun getOrderedDocs(): List<DocumentSnapshot> =
        categoriesCollection.get().await().documents.sortedWith(
            compareBy(
                { it.getLong("order") ?: Long.MAX_VALUE },
                { it.getLong("createdAt") ?: 0L }
            )
        )

    override suspend fun getAll(): List<String> = withContext(Dispatchers.IO) {
        getOrderedDocs().map { it.id }
    }

    override suspend fun add(name: String): Result<Unit> = withContext(Dispatchers.IO) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Category name can't be blank."))
        }
        try {
            val docRef = categoriesCollection.document(trimmed)
            if (docRef.get().await().exists()) {
                return@withContext Result.failure(IllegalArgumentException("That category already exists."))
            }
            val existingCount = categoriesCollection.get().await().documents.size
            docRef.set(
                mapOf(
                    "createdAt" to System.currentTimeMillis(),
                    "order" to existingCount.toLong()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun delete(name: String, password: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (password != ADMIN_DELETE_PASSWORD) {
            return@withContext Result.failure(SecurityException("Incorrect password."))
        }
        try {
            val allCategories = categoriesCollection.get().await().documents
            if (allCategories.size <= 1) {
                return@withContext Result.failure(IllegalStateException("At least one category must remain."))
            }

            // Cascade: delete every wallpaper tagged with this category — R2 objects first,
            // then the Firestore docs (a stray R2 object with no doc is harmless; a doc
            // pointing at a missing R2 object is not).
            val taggedWallpapers = wallpapersCollection
                .whereEqualTo("category", name)
                .whereEqualTo(FirestoreSchema.FIELD_ACTIVE, true)
                .get()
                .await()

            taggedWallpapers.documents.forEach { doc ->
                val item = doc.toWallpaperItem()
                item?.thumbnailUrl?.let { url ->
                    R2Client.keyFromPublicUrl(url)?.let { key -> R2Client.delete(key) }
                }
                item?.imageUrl?.let { url ->
                    R2Client.keyFromPublicUrl(url)?.let { key -> R2Client.delete(key) }
                }
            }

            val batch = Firebase.firestore.batch()
            taggedWallpapers.documents.forEach { doc -> batch.delete(doc.reference) }
            batch.delete(categoriesCollection.document(name))
            batch.commit().await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun moveUp(name: String): Result<Unit> = swapOrder(name, direction = -1)

    override suspend fun moveDown(name: String): Result<Unit> = swapOrder(name, direction = 1)

    private suspend fun swapOrder(name: String, direction: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val ordered = getOrderedDocs()
            val index = ordered.indexOfFirst { it.id == name }
            if (index == -1) {
                return@withContext Result.failure(IllegalStateException("Category not found."))
            }
            val swapIndex = index + direction
            if (swapIndex !in ordered.indices) {
                // Already at the top/bottom — nothing to do, not an error.
                return@withContext Result.success(Unit)
            }

            val currentDoc = ordered[index]
            val swapDoc = ordered[swapIndex]
            // Fall back to the current list position if a doc never had an explicit
            // order field (pre-migration categories) so the swap still makes sense.
            val currentOrder = currentDoc.getLong("order") ?: index.toLong()
            val swapOrder = swapDoc.getLong("order") ?: swapIndex.toLong()

            val batch = Firebase.firestore.batch()
            batch.update(currentDoc.reference, "order", swapOrder)
            batch.update(swapDoc.reference, "order", currentOrder)
            batch.commit().await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        const val ADMIN_DELETE_PASSWORD = "671245123"
    }
}
