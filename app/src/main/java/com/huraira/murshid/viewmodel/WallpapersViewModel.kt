package com.huraira.murshid.viewmodel

import androidx.lifecycle.ViewModel
import com.huraira.murshid.data.DummyDataProvider
import com.huraira.murshid.data.model.WallpaperItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class WallpapersUiState(
    val wallpapers: List<WallpaperItem> = emptyList(),
    val isLoading: Boolean = false
)

class WallpapersViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(WallpapersUiState())
    val uiState: StateFlow<WallpapersUiState> = _uiState.asStateFlow()

    init {
        _uiState.value = WallpapersUiState(
            wallpapers = DummyDataProvider.getWallpapers(),
            isLoading = false
        )
    }

    fun findById(id: String): WallpaperItem? =
        _uiState.value.wallpapers.firstOrNull { it.id == id }
}