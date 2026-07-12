package com.huraira.murshid.data.repository

import com.huraira.murshid.data.DummyDataProvider
import com.huraira.murshid.data.model.UpdateItem

/**
 * Abstracts where Updates come from / go to. See [WallpaperRepository] for the rationale.
 */
interface UpdatesRepository {
    suspend fun getAll(): List<UpdateItem>
    suspend fun create(item: UpdateItem): Result<UpdateItem>
    suspend fun delete(id: String): Result<Unit>
}

class DummyUpdatesRepository : UpdatesRepository {

    override suspend fun getAll(): List<UpdateItem> = DummyDataProvider.getUpdates()

    override suspend fun create(item: UpdateItem): Result<UpdateItem> {
        // TODO(Prompt 2): persist to Firestore, and upload any local detail image to R2 first.
        val withId = item.copy(id = item.id.ifBlank { "u_${System.currentTimeMillis()}" })
        DummyDataProvider.addUpdate(withId)
        return Result.success(withId)
    }

    override suspend fun delete(id: String): Result<Unit> {
        // TODO(Prompt 2): remove from Firestore (and R2 if it has a detail image).
        DummyDataProvider.removeUpdate(id)
        return Result.success(Unit)
    }
}
