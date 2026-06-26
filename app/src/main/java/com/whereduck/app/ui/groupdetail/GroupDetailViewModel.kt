package com.whereduck.app.ui.groupdetail

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.whereduck.app.data.model.AnimalRegistry
import com.whereduck.app.data.model.Contact
import com.whereduck.app.data.model.StarnazzoLevel
import com.whereduck.app.data.repository.AlertRepository
import com.whereduck.app.data.repository.ContactRepository
import com.whereduck.app.data.repository.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GroupDetailUiState(
    val isLoading: Boolean = true,
    val groupName: String = "",
    val contacts: List<Contact> = emptyList(),
    val selectedLevel: StarnazzoLevel = StarnazzoLevel.MEDIUM,
    val sendingToUserId: String? = null,
    val isBroadcasting: Boolean = false,
    val lastSendResult: String? = null,
    val error: String? = null
)

data class StarnazzoSentEvent(
    val alertId: String,
    val toName: String,
    val level: String
)

@HiltViewModel
class GroupDetailViewModel @Inject constructor(
    private val app: Application,
    savedStateHandle: SavedStateHandle,
    private val groupRepository: GroupRepository,
    private val contactRepository: ContactRepository,
    private val alertRepository: AlertRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    val groupId: String = savedStateHandle.get<String>("groupId") ?: ""
    private val userId: String = auth.currentUser?.uid ?: ""

    private val _uiState = MutableStateFlow(GroupDetailUiState())
    val uiState: StateFlow<GroupDetailUiState> = _uiState.asStateFlow()

    private val _starnazzoSent = MutableSharedFlow<StarnazzoSentEvent>()
    val starnazzoSent: SharedFlow<StarnazzoSentEvent> = _starnazzoSent.asSharedFlow()

    init {
        loadGroupWithContacts()
    }

    private fun loadGroupWithContacts() {
        if (userId.isEmpty()) return
        viewModelScope.launch {
            // Combine group data with contacts to resolve contactIds
            groupRepository.observeUserGroups(userId)
                .combine(contactRepository.observeContacts(userId)) { groups, allContacts ->
                    val group = groups.find { it.id == groupId }
                    val groupContacts = if (group != null) {
                        allContacts.filter { it.id in group.contactIds }
                    } else {
                        emptyList()
                    }
                    Pair(group, groupContacts)
                }
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
                .collect { (group, contacts) ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        groupName = group?.name ?: "",
                        contacts = contacts
                    )
                }
        }
    }

    fun selectLevel(level: StarnazzoLevel) {
        _uiState.value = _uiState.value.copy(selectedLevel = level)
    }

    fun sendStarnazzo(toUserId: String) {
        val level = _uiState.value.selectedLevel
        val toContact = _uiState.value.contacts.find { it.id == toUserId }
        val toName = toContact?.displayName ?: "Qualcuno"
        val selectedAnimal = AnimalRegistry.getSelectedAnimal(app, level)

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                sendingToUserId = toUserId,
                lastSendResult = null
            )
            try {
                val result = alertRepository.sendStarnazzo(
                    toUserId = toUserId,
                    level = level.key,
                    animalType = selectedAnimal
                )
                val status = result["status"] as? String
                val alertId = result["alertId"] as? String ?: ""

                _uiState.value = _uiState.value.copy(sendingToUserId = null)

                when (status) {
                    "sent" -> {
                        _starnazzoSent.emit(StarnazzoSentEvent(alertId, toName, level.key))
                    }
                    else -> {
                        _uiState.value = _uiState.value.copy(
                            lastSendResult = "Errore nell'invio"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    sendingToUserId = null,
                    lastSendResult = "Errore: ${e.message}"
                )
            }
        }
    }

    fun sendBroadcast() {
        val level = _uiState.value.selectedLevel
        val selectedAnimal = AnimalRegistry.getSelectedAnimal(app, level)

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isBroadcasting = true,
                lastSendResult = null
            )
            try {
                val result = alertRepository.sendBroadcast(
                    groupId = groupId,
                    level = level.key,
                    animalType = selectedAnimal
                )
                val status = result["status"] as? String

                _uiState.value = _uiState.value.copy(isBroadcasting = false)

                @Suppress("UNCHECKED_CAST")
                val alertIds = result["alertIds"] as? List<String> ?: emptyList()
                val firstAlertId = alertIds.firstOrNull() ?: ""

                when (status) {
                    "sent", "partial" -> {
                        _starnazzoSent.emit(StarnazzoSentEvent(firstAlertId, "TUTTI", level.key))
                    }
                    else -> {
                        _uiState.value = _uiState.value.copy(
                            lastSendResult = "Errore nell'invio"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isBroadcasting = false,
                    lastSendResult = "Errore: ${e.message}"
                )
            }
        }
    }

    fun clearSendResult() {
        _uiState.value = _uiState.value.copy(lastSendResult = null)
    }
}
