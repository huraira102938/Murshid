package com.huraira.murshid.data.model

enum class LibraryContentType {
    QUOTE,
    IMAGE,
    IMAGE_QUOTE,
    VIDEO
}

data class LibraryItem(
    val id: String,
    val type: LibraryContentType,
    val quoteText: String? = null,
    val author: String? = null,
    val imageUrl: String? = null,
    val videoThumbnailUrl: String? = null,
    val videoUrl: String? = null
)