package com.whereduck.app.ui.groupdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.whereduck.app.data.model.Group
import com.whereduck.app.data.model.Member
import com.whereduck.app.data.model.StarnazzoLevel
import com.whereduck.app.data.repository.AlertRepository
import com.whereduck.app.data.repository.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GroupDetailUiState(
    val isLoading: Boolean = true,
    val groupName: String = "",
    val members: List<Member> = emptyList(),
    val selectedLevel: StarnazzoLevel = StarnazzoLevel.MEDIUM,
    val sendingToUserId: String? = null,
    val isBroadcasting: Boolean = false,
    val lastSendResult: String? = null,
    val error: String? = null
)

@HiltViewModel
class GroupDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val groupRepository: GroupRepository,
    private val alertRepository: AlertRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    val groupId: String = savedStateHandle.get<String>("groupId") ?: ""

    private val _uiState = MutableStateFlow(GroupDetailUiState())
    val uiState: StateFlow<GroupDetailUiState> = _uiState.asStateFlow()

    init {
        loadGroupInfo()
        loadMembers()
    }

    private fun loadGroupInfo() {
        viewModelScope.launch {
            groupRepository.observeGroupMembers(groupId)
                .catch { /* ignore */ }
                .collect { /* members loaded separately */ }
        }
        // Load group name
        viewModelScope.launch {
            try {
                val group = groupRepository.getGroup(groupId)
                _uiState.value = _uiState.value.copy(groupName = group?.name ?: "")
            } catch (_: Exception) { }
        }
    }

    private fun loadMembers() {
        val currentUserId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            groupRepository.observeGroupMembers(groupId)
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
                .collect { members ->
                    val otherMembers = members.filter { it.id != currentUserId }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        members = otherMembers
                    )
                }
        }
    }

    fun selectLevel(level: StarnazzoLevel) {
        _uiState.value = _uiState.value.copy(selectedLevel = level)
    }

    fun sendStarnazzo(toUserId: String) {
        val level = _uiState.value.selectedLevel

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                sendingToUserId = toUserId,
                lastSendResult = null
            )
            try {
                val result = alertRepository.sendStarnazzo(
                    toUserId = toUserId,
                    groupId = groupId,
                    level = level.key,
                    animalType = level.defaultAnimal
                )
                val status = result["status"] as? String
                _uiState.value = _uiState.value.copy(
                    sendingToUserId = null,
                    lastSendResult = when (status) {
                        "sent" -> "Starnazzo inviato!"
                        "rate_limited" -> "Troppi starnazzi! Aspetta un po'."
                        "plan_limited" -> "Limite giornaliero raggiunto. Passa a Premium!"
                        else -> "Errore nell'invio"
                    }
                )
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

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isBroadcasting = true,
                lastSendResult = null
            )
            try {
                val result = alertRepository.sendBroadcast(
                    groupId = groupId,
                    level = level.key,
                    animalType = level.defaultAnimal
                )
                val status = result["status"] as? String
                _uiState.value = _uiState.value.copy(
                    isBroadcasting = false,
                    lastSendResult = when (status) {
                        "sent" -> "Starnazzo inviato a tutti!"
                        "partial" -> "Starnazzo inviato (alcuni membri non raggiunti)"
                        "rate_limited" -> "Troppi starnazzi! Aspetta un po'."
                        "plan_limited" -> "Limite broadcast raggiunto. Passa a Premium!"
                        else -> "Errore nell'invio"
                    }
                )
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
