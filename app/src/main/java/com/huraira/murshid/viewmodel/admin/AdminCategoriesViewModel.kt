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
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val isReordering: Boolean = false,
    val errorMessage: String? = null,
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
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val categories = categoryRepository.getAll()
                val wallpapers = wallpaperRepository.getAll()
                val counts = categories.associateWith { category ->
                    wallpapers.count { it.category == category }
                }
                _uiState.update {
                    it.copy(categories = categories, wallpaperCountByCategory = counts, isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = friendlyMessage(e)) }
            }
        }
    }

    fun addCategory(name: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, addErrorMessage = null) }
            val result = categoryRepository.add(name)
            _uiState.update {
                it.copy(isSubmitting = false, addErrorMessage = result.exceptionOrNull()?.let { e -> friendlyMessage(e) })
            }
            if (result.isSuccess) refresh()
        }
    }

    fun consumeAddError() {
        _uiState.update { it.copy(addErrorMessage = null) }
    }

    fun consumeError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /** [onResult] reports success or a human-readable failure reason (wrong password, etc). */
    fun deleteCategory(name: String, password: String, onResult: (success: Boolean, error: String?) -> Unit) {
        viewModelScope.launch {
            try {
                val result = categoryRepository.delete(name, password)
                if (result.isSuccess) {
                    refresh()
                    onResult(true, null)
                } else {
                    onResult(false, result.exceptionOrNull()?.let { e -> friendlyMessage(e) } ?: "Something went wrong.")
                }
            } catch (e: Exception) {
                onResult(false, friendlyMessage(e))
            }
        }
    }

    fun moveUp(name: String) = reorder { categoryRepository.moveUp(name) }

    fun moveDown(name: String) = reorder { categoryRepository.moveDown(name) }

    private fun reorder(action: suspend () -> Result<Unit>) {
        // Guard against rapid double-taps firing overlapping swaps against stale data.
        if (_uiState.value.isReordering) return
        viewModelScope.launch {
            _uiState.update { it.copy(isReordering = true) }
            val result = action()
            if (result.isFailure) {
                _uiState.update {
                    it.copy(isReordering = false, errorMessage = result.exceptionOrNull()?.let { e -> friendlyMessage(e) })
                }
            } else {
                refresh()
                _uiState.update { it.copy(isReordering = false) }
            }
        }
    }

    private fun friendlyMessage(e: Throwable): String =
        e.message?.takeIf { it.isNotBlank() } ?: "Something went wrong. Please try again."
}
