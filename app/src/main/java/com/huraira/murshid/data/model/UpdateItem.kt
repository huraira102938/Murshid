package com.huraira.murshid.data.model

data class UpdateItem(
    val id: String,
    val title: String,
    val date: String,
    val thumbnailUrl: String? = null,
    val summary: String,
    val fullContent: String,
    val detailImageUrl: String? = null,
    val youtubeVideoId: String? = null
)