package com.whereduck.app.ui.group

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.whereduck.app.data.model.GroupInvite
import com.whereduck.app.data.model.Member
import com.whereduck.app.data.repository.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GroupManagementUiState(
    val groupName: String = "",
    val members: List<Member> = emptyList(),
    val pendingInvites: List<GroupInvite> = emptyList(),
    val isAdmin: Boolean = false,
    val isInviting: Boolean = false,
    val inviteError: String? = null,
    val inviteSuccess: String? = null,
    val actionError: String? = null,
    val isDeleting: Boolean = false
)

@HiltViewModel
class GroupManagementViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val groupRepository: GroupRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    val groupId: String = savedStateHandle["groupId"] ?: ""
    val currentUserId: String = auth.currentUser?.uid ?: ""

    private val _uiState = MutableStateFlow(GroupManagementUiState())
    val uiState: StateFlow<GroupManagementUiState> = _uiState.asStateFlow()

    private val _groupDeleted = MutableSharedFlow<Unit>()
    val groupDeleted: SharedFlow<Unit> = _groupDeleted.asSharedFlow()

    private val _leftGroup = MutableSharedFlow<Unit>()
    val leftGroup: SharedFlow<Unit> = _leftGroup.asSharedFlow()

    init {
        if (groupId.isNotEmpty()) {
            loadGroupName()
            loadMembers()
            loadInvites()
        }
    }

    private fun loadGroupName() {
        viewModelScope.launch {
            try {
                val group = groupRepository.getGroup(groupId)
                _uiState.value = _uiState.value.copy(groupName = group?.name ?: "")
            } catch (_: Exception) { }
        }
    }

    private fun loadMembers() {
        viewModelScope.launch {
            groupRepository.observeGroupMembers(groupId)
                .catch { /* ignore */ }
                .collect { members ->
                    val isAdmin = members.any { it.id == currentUserId && it.isAdmin }
                    _uiState.value = _uiState.value.copy(
                        members = members,
                        isAdmin = isAdmin
                    )
                }
        }
    }

    private fun loadInvites() {
        viewModelScope.launch {
            groupRepository.observeGroupInvites(groupId)
                .catch { /* ignore */ }
                .collect { invites ->
                    _uiState.value = _uiState.value.copy(pendingInvites = invites)
                }
        }
    }

    fun sendInvite(groupId: String, email: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isInviting = true,
                inviteError = null,
                inviteSuccess = null
            )
            try {
                val result = groupRepository.sendInvite(groupId, email)
                val status = result["status"] as? String
                when (status) {
                    "sent" -> {
                        _uiState.value = _uiState.value.copy(
                            isInviting = false,
                            inviteSuccess = "Invito inviato a $email!"
                        )
                    }
                    "user_not_found" -> {
                        _uiState.value = _uiState.value.copy(
                            isInviting = false,
                            inviteError = "Utente non trovato. Deve prima registrarsi su WhereTheDuck."
                        )
                    }
                    "already_member" -> {
                        _uiState.value = _uiState.value.copy(
                            isInviting = false,
                            inviteError = "Questo utente è già membro del gruppo."
                        )
                    }
                    "already_invited" -> {
                        _uiState.value = _uiState.value.copy(
                            isInviting = false,
                            inviteError = "Invito già inviato a questo utente."
                        )
                    }
                    else -> {
                        _uiState.value = _uiState.value.copy(
                            isInviting = false,
                            inviteError = "Errore nell'invio dell'invito."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isInviting = false,
                    inviteError = "Errore: ${e.message}"
                )
            }
        }
    }

    fun removeMember(memberId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(actionError = null)
            try {
                groupRepository.removeMember(groupId, memberId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    actionError = "Errore: ${e.message}"
                )
            }
        }
    }

    fun leaveGroup() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(actionError = null)
            try {
                groupRepository.removeMember(groupId, currentUserId)
                _leftGroup.emit(Unit)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    actionError = "Errore: ${e.message}"
                )
            }
        }
    }

    fun deleteGroup() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true, actionError = null)
            try {
                groupRepository.deleteGroup(groupId)
                _groupDeleted.emit(Unit)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    actionError = "Errore: ${e.message}"
                )
            }
        }
    }

    fun clearInviteMessages() {
        _uiState.value = _uiState.value.copy(inviteError = null, inviteSuccess = null)
    }

    fun clearActionError() {
        _uiState.value = _uiState.value.copy(actionError = null)
    }
}
