package com.huraira.murshid.viewmodel.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huraira.murshid.data.repository.NotificationRepository
import com.huraira.murshid.data.repository.Repositories
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminNotificationsUiState(
    val isSending: Boolean = false,
    val lastSendSucceeded: Boolean? = null
)

class AdminNotificationsViewModel(
    private val repository: NotificationRepository = Repositories.notification
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminNotificationsUiState())
    val uiState: StateFlow<AdminNotificationsUiState> = _uiState.asStateFlow()

    fun send(title: String, body: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, lastSendSucceeded = null) }
            val result = repository.send(title, body)
            _uiState.update { it.copy(isSending = false, lastSendSucceeded = result.isSuccess) }
        }
    }

    fun consumeSendResult() {
        _uiState.update { it.copy(lastSendSucceeded = null) }
    }
}
