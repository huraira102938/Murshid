package com.huraira.murshid.data.remote

import com.google.firebase.firestore.DocumentSnapshot
import com.huraira.murshid.data.model.LibraryContentType
import com.huraira.murshid.data.model.LibraryItem
import com.huraira.murshid.data.model.UpdateItem
import com.huraira.murshid.data.model.WallpaperItem

fun DocumentSnapshot.toWallpaperItem(): WallpaperItem? {
    val title = getString("title") ?: return null
    val imageUrl = getString("imageUrl") ?: return null
    return WallpaperItem(
        id = id,
        title = title,
        imageUrl = imageUrl,
        thumbnailUrl = getString("thumbnailUrl") ?: "",
        category = getString("category") ?: "",
        createdAt = getLong("createdAt") ?: 0L
    )
}

fun WallpaperItem.toFirestoreMap(active: Boolean = true): Map<String, Any> = mapOf(
    "title" to title,
    "imageUrl" to imageUrl,
    "thumbnailUrl" to thumbnailUrl,
    "category" to category,
    "createdAt" to createdAt,
    "active" to active
)

fun DocumentSnapshot.toLibraryItem(): LibraryItem? {
    val typeName = getString("type") ?: return null
    val type = runCatching { LibraryContentType.valueOf(typeName) }.getOrNull() ?: return null
    return LibraryItem(
        id = id,
        type = type,
        quoteText = getString("quoteText"),
        author = getString("author"),
        imageUrl = getString("imageUrl"),
        thumbnailUrl = getString("thumbnailUrl"),
        videoThumbnailUrl = getString("videoThumbnailUrl"),
        videoUrl = getString("videoUrl"),
        createdAt = getLong("createdAt") ?: 0L
    )
}

fun LibraryItem.toFirestoreMap(active: Boolean = true): Map<String, Any?> = mapOf(
    "type" to type.name,
    "quoteText" to quoteText,
    "author" to author,
    "imageUrl" to imageUrl,
    "thumbnailUrl" to thumbnailUrl,
    "videoThumbnailUrl" to videoThumbnailUrl,
    "videoUrl" to videoUrl,
    "createdAt" to createdAt,
    "active" to active
)

fun DocumentSnapshot.toUpdateItem(): UpdateItem? {
    val title = getString("title") ?: return null
    val date = getString("date") ?: return null
    val summary = getString("summary") ?: return null
    val fullContent = getString("fullContent") ?: return null
    return UpdateItem(
        id = id,
        title = title,
        date = date,
        thumbnailUrl = getString("thumbnailUrl"),
        summary = summary,
        fullContent = fullContent,
        detailImageUrl = getString("detailImageUrl"),
        youtubeVideoId = getString("youtubeVideoId"),
        createdAt = getLong("createdAt") ?: 0L
    )
}

fun UpdateItem.toFirestoreMap(active: Boolean = true): Map<String, Any?> = mapOf(
    "title" to title,
    "date" to date,
    "thumbnailUrl" to thumbnailUrl,
    "summary" to summary,
    "fullContent" to fullContent,
    "detailImageUrl" to detailImageUrl,
    "youtubeVideoId" to youtubeVideoId,
    "createdAt" to createdAt,
    "active" to active
)
