package com.huraira.murshid.viewmodel.admin

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huraira.murshid.data.model.UpdateItem
import com.huraira.murshid.data.repository.Repositories
import com.huraira.murshid.data.repository.UpdatesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminUpdatesUiState(
    val updates: List<UpdateItem> = emptyList(),
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null
)

class AdminUpdatesViewModel(
    private val repository: UpdatesRepository = Repositories.updates
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUpdatesUiState())
    val uiState: StateFlow<AdminUpdatesUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val updates = repository.getAll()
                _uiState.update { it.copy(updates = updates, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = friendlyMessage(e)) }
            }
        }
    }

    fun create(
        title: String,
        date: String,
        summary: String,
        fullContent: String,
        detailImageUri: Uri?,
        youtubeVideoId: String?
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            val result = repository.create(title, date, summary, fullContent, detailImageUri, youtubeVideoId)
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
