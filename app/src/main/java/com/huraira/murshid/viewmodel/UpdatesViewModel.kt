package com.huraira.murshid.viewmodel

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

data class UpdatesUiState(
    val updates: List<UpdateItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class UpdatesViewModel(
    private val repository: UpdatesRepository = Repositories.updates
) : ViewModel() {

    private val _uiState = MutableStateFlow(UpdatesUiState())
    val uiState: StateFlow<UpdatesUiState> = _uiState.asStateFlow()

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
                _uiState.update { it.copy(isLoading = false, errorMessage = "Couldn't load updates.") }
            }
        }
    }

    fun findById(id: String): UpdateItem? =
        _uiState.value.updates.firstOrNull { it.id == id }
}
