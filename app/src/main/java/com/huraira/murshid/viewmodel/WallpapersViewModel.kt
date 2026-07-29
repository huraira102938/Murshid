package com.huraira.murshid.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huraira.murshid.data.model.WallpaperItem
import com.huraira.murshid.data.repository.CategoryRepository
import com.huraira.murshid.data.repository.Repositories
import com.huraira.murshid.data.repository.WallpaperRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WallpapersUiState(
    val allWallpapers: List<WallpaperItem> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    /** null selectedCategory means "All". */
    val visibleWallpapers: List<WallpaperItem>
        get() = if (selectedCategory == null) {
            allWallpapers
        } else {
            allWallpapers.filter { it.category == selectedCategory }
        }
}

class WallpapersViewModel(
    private val wallpaperRepository: WallpaperRepository = Repositories.wallpaper,
    private val categoryRepository: CategoryRepository = Repositories.category
) : ViewModel() {

    private val _uiState = MutableStateFlow(WallpapersUiState())
    val uiState: StateFlow<WallpapersUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val wallpapers = wallpaperRepository.getAll()
                val categories = categoryRepository.getAll()
                _uiState.update {
                    it.copy(
                        allWallpapers = wallpapers,
                        categories = categories,
                        selectedCategory = it.selectedCategory?.takeIf { c -> c in categories },
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Couldn't load wallpapers.") }
            }
        }
    }

    fun selectCategory(category: String?) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun findById(id: String): WallpaperItem? =
        _uiState.value.allWallpapers.firstOrNull { it.id == id }
}
