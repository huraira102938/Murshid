package com.huraira.murshid.data.repository

import com.huraira.murshid.data.DummyDataProvider
import com.huraira.murshid.data.model.LibraryItem

/**
 * Abstracts where Library items come from / go to. See [WallpaperRepository] for the
 * rationale — the dummy implementation just mutates [DummyDataProvider]'s in-memory list.
 */
interface LibraryRepository {
    suspend fun getAll(): List<LibraryItem>
    suspend fun create(item: LibraryItem): Result<LibraryItem>
    suspend fun delete(id: String): Result<Unit>
}

class DummyLibraryRepository : LibraryRepository {

    override suspend fun getAll(): List<LibraryItem> = DummyDataProvider.getLibraryItems()

    override suspend fun create(item: LibraryItem): Result<LibraryItem> {
        // TODO(Prompt 2): persist to Firestore, and upload any local image URI to R2 first.
        val withId = item.copy(id = item.id.ifBlank { "l_${System.currentTimeMillis()}" })
        DummyDataProvider.addLibraryItem(withId)
        return Result.success(withId)
    }

    override suspend fun delete(id: String): Result<Unit> {
        // TODO(Prompt 2): remove from Firestore (and R2 if it has an image).
        DummyDataProvider.removeLibraryItem(id)
        return Result.success(Unit)
    }
}
