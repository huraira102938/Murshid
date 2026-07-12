package com.huraira.murshid.viewmodel

import androidx.lifecycle.ViewModel
import com.huraira.murshid.data.DummyDataProvider
import com.huraira.murshid.data.model.WallpaperItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class WallpapersUiState(
    val allWallpapers: List<WallpaperItem> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String? = null,
    val isLoading: Boolean = false
) {
    /** null selectedCategory means "All". */
    val visibleWallpapers: List<WallpaperItem>
        get() = if (selectedCategory == null) {
            allWallpapers
        } else {
            allWallpapers.filter { it.category == selectedCategory }
        }
}

class WallpapersViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(WallpapersUiState())
    val uiState: StateFlow<WallpapersUiState> = _uiState.asStateFlow()

    init {
        _uiState.value = WallpapersUiState(
            allWallpapers = DummyDataProvider.getWallpapers(),
            categories = DummyDataProvider.getCategories(),
            isLoading = false
        )
    }

    fun selectCategory(category: String?) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun findById(id: String): WallpaperItem? =
        _uiState.value.allWallpapers.firstOrNull { it.id == id }
}
