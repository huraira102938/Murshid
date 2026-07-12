package com.huraira.murshid.viewmodel.admin

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
    val isSubmitting: Boolean = false
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
            _uiState.update { it.copy(updates = repository.getAll()) }
        }
    }

    fun create(item: UpdateItem) {
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
