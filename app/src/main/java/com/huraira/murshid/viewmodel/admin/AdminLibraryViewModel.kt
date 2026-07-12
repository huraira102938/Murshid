package com.huraira.murshid.viewmodel.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huraira.murshid.data.model.LibraryItem
import com.huraira.murshid.data.repository.LibraryRepository
import com.huraira.murshid.data.repository.Repositories
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminLibraryUiState(
    val items: List<LibraryItem> = emptyList(),
    val isSubmitting: Boolean = false
)

class AdminLibraryViewModel(
    private val repository: LibraryRepository = Repositories.library
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminLibraryUiState())
    val uiState: StateFlow<AdminLibraryUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(items = repository.getAll()) }
        }
    }

    fun create(item: LibraryItem) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            repository.create(item)
            _uiState.update { it.copy(isSubmitting = false) }
            refresh()
        }
    }

    fun delete(id: String) {
        viewModelScope.launch {
            repository.delete(id)
            refresh()
        }
    }
}
