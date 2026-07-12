package com.huraira.murshid.viewmodel.admin

import android.net.Uri
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

data class AdminWallpapersUiState(
    val allWallpapers: List<WallpaperItem> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String? = null,
    val isSubmitting: Boolean = false
) {
    /** null selectedCategory means "All". */
    val visibleWallpapers: List<WallpaperItem>
        get() = if (selectedCategory == null) {
            allWallpapers
        } else {
            allWallpapers.filter { it.category == selectedCategory }
        }
}

class AdminWallpapersViewModel(
    private val wallpaperRepository: WallpaperRepository = Repositories.wallpaper,
    private val categoryRepository: CategoryRepository = Repositories.category
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminWallpapersUiState())
    val uiState: StateFlow<AdminWallpapersUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val wallpapers = wallpaperRepository.getAll()
            val categories = categoryRepository.getAll()
            _uiState.update {
                it.copy(
                    allWallpapers = wallpapers,
                    categories = categories,
                    selectedCategory = it.selectedCategory?.takeIf { c -> c in categories }
                )
            }
        }
    }

    fun selectCategory(category: String?) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun upload(title: String, category: String, imageUri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            wallpaperRepository.upload(title, category, imageUri)
            _uiState.update { it.copy(isSubmitting = false) }
            refresh()
        }
    }

    fun delete(id: String) {
        viewModelScope.launch {
            wallpaperRepository.delete(id)
            refresh()
        }
    }
}
