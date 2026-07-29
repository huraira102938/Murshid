package com.huraira.murshid.viewmodel.admin

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huraira.murshid.data.model.LibraryContentType
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
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null
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
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val items = repository.getAll()
                _uiState.update { it.copy(items = items, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = friendlyMessage(e)) }
            }
        }
    }

    fun create(type: LibraryContentType, quoteText: String?, author: String?, imageUri: Uri?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            val result = repository.create(type, quoteText, author, imageUri)
            _uiState.update {
                it.copy(isSubmitting = false, errorMessage = result.exceptionOrNull()?.let { e -> friendlyMessage(e) })
            }
            if (result.isSuccess) refresh()
        }
    }

    fun delete(id: String) {
        viewModelScope.launch {
            val result = repository.delete(id)
            if (result.isFailure) {
                _uiState.update { it.copy(errorMessage = result.exceptionOrNull()?.let { e -> friendlyMessage(e) }) }
            } else {
                refresh()
            }
        }
    }

    fun consumeError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun friendlyMessage(e: Throwable): String =
        e.message?.takeIf { it.isNotBlank() } ?: "Something went wrong. Please try again."
}
