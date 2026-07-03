package com.huraira.murshid.viewmodel

import androidx.lifecycle.ViewModel
import com.huraira.murshid.data.DummyDataProvider
import com.huraira.murshid.data.model.LibraryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LibraryUiState(
    val items: List<LibraryItem> = emptyList(),
    val isLoading: Boolean = false
)

class LibraryViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        _uiState.value = LibraryUiState(
            items = DummyDataProvider.getLibraryItems(),
            isLoading = false
        )
    }
}