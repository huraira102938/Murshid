package com.huraira.murshid.data.repository

import com.google.firebase.Firebase
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
 * TODO(hardening): move the password check server-side (e.g. a callable Cloud Function)
 * before wide release — this is only acceptable because the whole admin flow is built
 * out of the public Play Store build, gated by the `// region ADMIN` blocks.
 */
interface CategoryRepository {
    suspend fun getAll(): List<String>
    suspend fun add(name: String): Result<Unit>
    suspend fun delete(name: String, password: String): Result<Unit>
}

class FirestoreCategoryRepository : CategoryRepository {

    private val categoriesCollection = Firebase.firestore.collection(FirestoreSchema.CATEGORIES)
    private val wallpapersCollection = Firebase.firestore.collection(FirestoreSchema.WALLPAPERS)

    override suspend fun getAll(): List<String> = withContext(Dispatchers.IO) {
        categoriesCollection.get().await().documents.map { it.id }.sorted()
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
            docRef.set(mapOf("createdAt" to System.currentTimeMillis())).await()
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

    companion object {
        const val ADMIN_DELETE_PASSWORD = "671245123"
    }
}
