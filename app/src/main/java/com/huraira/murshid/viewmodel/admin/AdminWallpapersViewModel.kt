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
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
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
                _uiState.update { it.copy(isLoading = false, errorMessage = friendlyMessage(e)) }
            }
        }
    }

    fun selectCategory(category: String?) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun upload(title: String, category: String, imageUri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            try {
                wallpaperRepository.upload(title, category, imageUri).getOrThrow()
                _uiState.update { it.copy(isSubmitting = false) }
                refresh()
            } catch (e: Exception) {
                _uiState.update { it.copy(isSubmitting = false, errorMessage = friendlyMessage(e)) }
            }
        }
    }

    fun delete(id: String) {
        viewModelScope.launch {
            try {
                wallpaperRepository.delete(id).getOrThrow()
                refresh()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = friendlyMessage(e)) }
            }
        }
    }

    fun consumeError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun friendlyMessage(e: Exception): String =
        e.message?.takeIf { it.isNotBlank() } ?: "Something went wrong. Please try again."
}
