package com.huraira.murshid.viewmodel

import androidx.lifecycle.ViewModel
import com.huraira.murshid.data.DummyDataProvider
import com.huraira.murshid.data.model.UpdateItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UpdatesUiState(
    val updates: List<UpdateItem> = emptyList(),
    val isLoading: Boolean = false
)

class UpdatesViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(UpdatesUiState())
    val uiState: StateFlow<UpdatesUiState> = _uiState.asStateFlow()

    init {
        _uiState.value = UpdatesUiState(
            updates = DummyDataProvider.getUpdates(),
            isLoading = false
        )
    }

    fun findById(id: String): UpdateItem? =
        _uiState.value.updates.firstOrNull { it.id == id }
}