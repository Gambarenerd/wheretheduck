package com.whereduck.app.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.whereduck.app.data.model.Alert
import com.whereduck.app.data.repository.AlertRepository
import com.whereduck.app.data.repository.ContactRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val isLoading: Boolean = true,
    val sentAlerts: List<Alert> = emptyList(),
    val receivedAlerts: List<Alert> = emptyList(),
    val sentCount: Int = 0,
    val receivedCount: Int = 0,
    val contactPhotos: Map<String, String> = emptyMap()
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val alertRepository: AlertRepository,
    private val contactRepository: ContactRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            combine(
                alertRepository.observeSentAlerts(userId),
                alertRepository.observeReceivedAlerts(userId),
                contactRepository.observeContacts(userId)
            ) { sent, received, contacts ->
                val photoMap = contacts.associate { it.id to it.photoUrl }
                HistoryUiState(
                    isLoading = false,
                    sentAlerts = sent,
                    receivedAlerts = received,
                    sentCount = sent.size,
                    receivedCount = received.size,
                    contactPhotos = photoMap
                )
            }
                .catch { _uiState.value = _uiState.value.copy(isLoading = false) }
                .collect { _uiState.value = it }
        }
    }
}
