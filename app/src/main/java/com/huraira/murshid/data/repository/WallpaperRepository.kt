package com.huraira.murshid.data.repository

import android.net.Uri
import com.huraira.murshid.data.DummyDataProvider
import com.huraira.murshid.data.model.WallpaperItem

/**
 * Abstracts where wallpapers come from / go to. The dummy implementation below operates
 * on [DummyDataProvider]'s in-memory list so the admin flow is fully testable before the
 * real Cloudflare R2 wiring lands. UI code should only ever depend on this interface.
 */
interface WallpaperRepository {
    suspend fun getAll(): List<WallpaperItem>
    suspend fun upload(title: String, category: String, imageUri: Uri): Result<WallpaperItem>
    suspend fun delete(id: String): Result<Unit>
}

class DummyWallpaperRepository : WallpaperRepository {

    override suspend fun getAll(): List<WallpaperItem> = DummyDataProvider.getWallpapers()

    override suspend fun upload(title: String, category: String, imageUri: Uri): Result<WallpaperItem> {
        // TODO(Prompt 2): replace with a real upload to Cloudflare R2 and persist the
        // resulting public URL instead of the local content:// URI.
        val item = WallpaperItem(
            id = "w_${System.currentTimeMillis()}",
            title = title,
            imageUrl = imageUri.toString(),
            category = category
        )
        DummyDataProvider.addWallpaper(item)
        return Result.success(item)
    }

    override suspend fun delete(id: String): Result<Unit> {
        // TODO(Prompt 2): also delete the underlying object from Cloudflare R2.
        DummyDataProvider.removeWallpaper(id)
        return Result.success(Unit)
    }
}
