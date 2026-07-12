package com.huraira.murshid.viewmodel.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huraira.murshid.data.repository.CategoryRepository
import com.huraira.murshid.data.repository.Repositories
import com.huraira.murshid.data.repository.WallpaperRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminCategoriesUiState(
    val categories: List<String> = emptyList(),
    val wallpaperCountByCategory: Map<String, Int> = emptyMap(),
    val isSubmitting: Boolean = false,
    val addErrorMessage: String? = null
)

class AdminCategoriesViewModel(
    private val categoryRepository: CategoryRepository = Repositories.category,
    private val wallpaperRepository: WallpaperRepository = Repositories.wallpaper
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminCategoriesUiState())
    val uiState: StateFlow<AdminCategoriesUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val categories = categoryRepository.getAll()
            val wallpapers = wallpaperRepository.getAll()
            val counts = categories.associateWith { category ->
                wallpapers.count { it.category == category }
            }
            _uiState.update { it.copy(categories = categories, wallpaperCountByCategory = counts) }
        }
    }

    fun addCategory(name: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, addErrorMessage = null) }
            val result = categoryRepository.add(name)
            _uiState.update {
                it.copy(isSubmitting = false, addErrorMessage = result.exceptionOrNull()?.message)
            }
            if (result.isSuccess) refresh()
        }
    }

    fun consumeAddError() {
        _uiState.update { it.copy(addErrorMessage = null) }
    }

    /** [onResult] reports success or a human-readable failure reason (wrong password, etc). */
    fun deleteCategory(name: String, password: String, onResult: (success: Boolean, error: String?) -> Unit) {
        viewModelScope.launch {
            val result = categoryRepository.delete(name, password)
            if (result.isSuccess) {
                refresh()
                onResult(true, null)
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Something went wrong.")
            }
        }
    }
}
