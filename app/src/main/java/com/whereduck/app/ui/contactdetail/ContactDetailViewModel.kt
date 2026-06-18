package com.whereduck.app.ui.contactdetail

import android.app.Application
import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.whereduck.app.data.model.Contact
import com.whereduck.app.data.model.StarnazzoLevel
import com.whereduck.app.data.repository.AlertRepository
import com.whereduck.app.data.repository.ContactRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class CallPhase {
    RINGING,
    RESPONDED,
    FAILED
}

data class ContactDetailUiState(
    val isLoading: Boolean = true,
    val contact: Contact? = null,
    val selectedLevel: StarnazzoLevel = StarnazzoLevel.MEDIUM,
    val isSending: Boolean = false,
    val lastSendResult: String? = null,
    val isVip: Boolean = false,
    val callPhase: CallPhase? = null,
    val callResponse: String? = null,
    val currentAlertId: String? = null
)

@HiltViewModel
class ContactDetailViewModel @Inject constructor(
    private val app: Application,
    savedStateHandle: SavedStateHandle,
    private val contactRepository: ContactRepository,
    private val alertRepository: AlertRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    val contactId: String = savedStateHandle.get<String>("contactId") ?: ""
    private val userId: String = auth.currentUser?.uid ?: ""
    private var alertObserverJob: Job? = null

    private val vipPrefs by lazy {
        app.getSharedPreferences("vip_prefs", Context.MODE_PRIVATE)
    }

    private val _uiState = MutableStateFlow(ContactDetailUiState())
    val uiState: StateFlow<ContactDetailUiState> = _uiState.asStateFlow()

    init {
        loadContact()
        loadVipStatus()
    }

    private fun loadVipStatus() {
        if (userId.isEmpty()) return
        val ids = vipPrefs.getString("vip_$userId", null)
            ?.split(",")
            ?.filter { it.isNotBlank() }
            ?: emptyList()
        _uiState.value = _uiState.value.copy(isVip = contactId in ids)
    }

    fun toggleVip() {
        if (userId.isEmpty()) return
        val ids = vipPrefs.getString("vip_$userId", null)
            ?.split(",")
            ?.filter { it.isNotBlank() }
            ?.toMutableList()
            ?: mutableListOf()

        val isCurrentlyVip = contactId in ids
        if (isCurrentlyVip) {
            ids.remove(contactId)
        } else {
            if (ids.size >= 4) return
            ids.add(contactId)
        }
        vipPrefs.edit().putString("vip_$userId", ids.joinToString(",")).apply()
        _uiState.value = _uiState.value.copy(isVip = !isCurrentlyVip)
    }

    private fun loadContact() {
        if (userId.isEmpty()) return
        viewModelScope.launch {
            contactRepository.observeContacts(userId)
                .catch {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
                .collect { contacts ->
                    val contact = contacts.find { it.id == contactId }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        contact = contact
                    )
                }
        }
    }

    fun selectLevel(level: StarnazzoLevel) {
        _uiState.value = _uiState.value.copy(selectedLevel = level)
    }

    fun sendStarnazzo() {
        val contact = _uiState.value.contact ?: return
        val level = _uiState.value.selectedLevel

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSending = true, lastSendResult = null)
            try {
                val result = alertRepository.sendStarnazzo(
                    toUserId = contactId,
                    level = level.key,
                    animalType = level.defaultAnimal
                )
                val status = result["status"] as? String
                val alertId = result["alertId"] as? String ?: ""

                when (status) {
                    "sent" -> {
                        _uiState.value = _uiState.value.copy(
                            isSending = false,
                            callPhase = CallPhase.RINGING,
                            currentAlertId = alertId
                        )
                        observeAlert(alertId)
                    }
                    "muted" -> {
                        val message = result["message"] as? String
                            ?: "L'utente ti ha bloccato"
                        _uiState.value = _uiState.value.copy(
                            isSending = false,
                            lastSendResult = "$message"
                        )
                    }
                    else -> {
                        _uiState.value = _uiState.value.copy(
                            isSending = false,
                            lastSendResult = "Errore nell'invio"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSending = false,
                    lastSendResult = "Errore: ${e.message}"
                )
            }
        }
    }

    private fun observeAlert(alertId: String) {
        alertObserverJob?.cancel()
        alertObserverJob = viewModelScope.launch {
            alertRepository.observeAlert(alertId)
                .catch {
                    _uiState.value = _uiState.value.copy(callPhase = CallPhase.FAILED)
                }
                .collect { data ->
                    val status = data["status"] as? String ?: ""
                    val response = data["response"] as? String

                    when {
                        response != null -> {
                            _uiState.value = _uiState.value.copy(
                                callPhase = CallPhase.RESPONDED,
                                callResponse = response
                            )
                        }
                        status == "failed" -> {
                            _uiState.value = _uiState.value.copy(
                                callPhase = CallPhase.FAILED
                            )
                        }
                    }
                }
        }
    }

    fun dismissCall() {
        alertObserverJob?.cancel()
        _uiState.value = _uiState.value.copy(
            callPhase = null,
            callResponse = null
        )
    }

    fun cancelStarnazzo() {
        alertObserverJob?.cancel()
        val alertId = _uiState.value.currentAlertId
        _uiState.value = _uiState.value.copy(
            isSending = false,
            callPhase = null,
            callResponse = null,
            currentAlertId = null
        )
        if (alertId != null) {
            viewModelScope.launch {
                try {
                    alertRepository.cancelStarnazzo(alertId)
                } catch (_: Exception) {
                    // Best effort — alert may have already been responded to
                }
            }
        }
    }

    fun removeContact() {
        viewModelScope.launch {
            try {
                contactRepository.removeContact(contactId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    lastSendResult = "Errore: ${e.message}"
                )
            }
        }
    }

    fun clearSendResult() {
        _uiState.value = _uiState.value.copy(lastSendResult = null)
    }
}
