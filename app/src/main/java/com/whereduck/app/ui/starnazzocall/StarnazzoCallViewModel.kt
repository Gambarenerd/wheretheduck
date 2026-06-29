package com.whereduck.app.ui.starnazzocall

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.whereduck.app.data.model.StarnazzoLevel
import com.whereduck.app.data.repository.AlertRepository
import com.whereduck.app.data.repository.ContactRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class CallPhase {
    SENDING,
    RINGING,
    RESPONDED,
    FAILED
}

data class StarnazzoCallUiState(
    val toName: String = "",
    val level: StarnazzoLevel = StarnazzoLevel.MEDIUM,
    val phase: CallPhase = CallPhase.SENDING,
    val response: String? = null,
    val alertId: String = "",
    val contactPhotoUrl: String = "",
    val contactEmail: String = "",
    val contactMotto: String = "",
    val shouldAutoDismiss: Boolean = false
)

@HiltViewModel
class StarnazzoCallViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val alertRepository: AlertRepository,
    private val contactRepository: ContactRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(StarnazzoCallUiState())
    val uiState: StateFlow<StarnazzoCallUiState> = _uiState.asStateFlow()

    init {
        val alertId = savedStateHandle.get<String>("alertId") ?: ""
        val rawToName = savedStateHandle.get<String>("toName") ?: ""
        val toName = try {
            java.net.URLDecoder.decode(rawToName, "UTF-8")
        } catch (_: Exception) { rawToName }
        val levelKey = savedStateHandle.get<String>("level") ?: "medium"

        _uiState.value = StarnazzoCallUiState(
            toName = toName,
            level = StarnazzoLevel.fromKey(levelKey),
            phase = if (alertId.isNotEmpty()) CallPhase.RINGING else CallPhase.FAILED,
            alertId = alertId
        )

        if (alertId.isNotEmpty()) {
            observeAlertStatus(alertId)
        }

        // Flash level: auto-dismiss after 3 seconds
        if (_uiState.value.level == StarnazzoLevel.LIGHT) {
            viewModelScope.launch {
                kotlinx.coroutines.delay(3000)
                cancelStarnazzo()
                _uiState.value = _uiState.value.copy(shouldAutoDismiss = true)
            }
        }
    }

    fun cancelStarnazzo() {
        val alertId = _uiState.value.alertId
        if (alertId.isEmpty()) return
        viewModelScope.launch {
            try {
                alertRepository.cancelStarnazzo(alertId)
            } catch (_: Exception) { }
        }
    }

    private fun observeAlertStatus(alertId: String) {
        viewModelScope.launch {
            alertRepository.observeAlert(alertId)
                .catch {
                    _uiState.value = _uiState.value.copy(phase = CallPhase.FAILED)
                }
                .collect { data ->
                    val status = data["status"] as? String ?: ""
                    val response = data["response"] as? String

                    // Load contact info on first data
                    if (_uiState.value.contactPhotoUrl.isEmpty()) {
                        val toUserId = data["toUserId"] as? String
                        if (toUserId != null) loadContactInfo(toUserId)
                    }

                    when {
                        response != null -> {
                            _uiState.value = _uiState.value.copy(
                                phase = CallPhase.RESPONDED,
                                response = response
                            )
                        }
                        status == "delivered" -> {
                            _uiState.value = _uiState.value.copy(
                                phase = CallPhase.RINGING
                            )
                        }
                        status == "failed" -> {
                            _uiState.value = _uiState.value.copy(
                                phase = CallPhase.FAILED
                            )
                        }
                    }
                }
        }
    }

    private fun loadContactInfo(toUserId: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val contacts = contactRepository.observeContacts(currentUserId).first()
                val contact = contacts.find { it.id == toUserId }
                if (contact != null) {
                    _uiState.value = _uiState.value.copy(
                        contactPhotoUrl = contact.photoUrl,
                        contactEmail = contact.email,
                        contactMotto = contact.motto
                    )
                }
            } catch (_: Exception) { }
        }
    }
}
