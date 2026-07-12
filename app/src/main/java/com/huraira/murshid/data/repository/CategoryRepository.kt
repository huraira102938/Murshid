package com.huraira.murshid.data.repository

import com.huraira.murshid.data.DummyDataProvider

/**
 * Abstracts wallpaper categories. Categories only apply to the Wallpapers section.
 * Deleting a category is destructive — it cascades and deletes every wallpaper tagged
 * with it — so the dummy implementation gates it behind a password and refuses to drop
 * the last remaining category.
 *
 * TODO(Prompt 2): move the password check server-side (e.g. a Cloud Function) instead of
 * comparing against a hardcoded client-side constant — this is only safe here because the
 * whole admin flow is gated out of the Play Store build.
 */
interface CategoryRepository {
    suspend fun getAll(): List<String>
    suspend fun add(name: String): Result<Unit>
    suspend fun delete(name: String, password: String): Result<Unit>
}

class DummyCategoryRepository : CategoryRepository {

    override suspend fun getAll(): List<String> = DummyDataProvider.getCategories()

    override suspend fun add(name: String): Result<Unit> {
        val trimmed = name.trim()
        if (trimmed.isBlank()) {
            return Result.failure(IllegalArgumentException("Category name can't be blank."))
        }
        if (DummyDataProvider.getCategories().any { it.equals(trimmed, ignoreCase = true) }) {
            return Result.failure(IllegalArgumentException("That category already exists."))
        }
        DummyDataProvider.addCategory(trimmed)
        return Result.success(Unit)
    }

    override suspend fun delete(name: String, password: String): Result<Unit> {
        if (password != ADMIN_DELETE_PASSWORD) {
            return Result.failure(SecurityException("Incorrect password."))
        }
        if (DummyDataProvider.getCategories().size <= 1) {
            return Result.failure(IllegalStateException("At least one category must remain."))
        }
        // TODO(Prompt 2): also delete the R2 objects for every wallpaper in this category.
        DummyDataProvider.removeCategory(name)
        return Result.success(Unit)
    }

    companion object {
        const val ADMIN_DELETE_PASSWORD = "671245123"
    }
}
