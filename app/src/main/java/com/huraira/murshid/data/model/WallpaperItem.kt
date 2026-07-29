package com.huraira.murshid.data.model

data class WallpaperItem(
    val id: String,
    val title: String,
    val imageUrl: String,
    val thumbnailUrl: String = "",
    val category: String = "Resilience",
    val createdAt: Long = 0L
)