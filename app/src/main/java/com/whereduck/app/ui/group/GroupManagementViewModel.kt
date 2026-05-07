package com.whereduck.app.ui.group

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.whereduck.app.data.model.GroupInvite
import com.whereduck.app.data.model.Member
import com.whereduck.app.data.repository.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
    val inviteSuccess: String? = null
)

@HiltViewModel
class GroupManagementViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val groupRepository: GroupRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val groupId: String = savedStateHandle["groupId"] ?: ""

    private val _uiState = MutableStateFlow(GroupManagementUiState())
    val uiState: StateFlow<GroupManagementUiState> = _uiState.asStateFlow()

    init {
        if (groupId.isNotEmpty()) {
            loadMembers()
            loadInvites()
        }
    }

    private fun loadMembers() {
        viewModelScope.launch {
            groupRepository.observeGroupMembers(groupId)
                .catch { /* ignore */ }
                .collect { members ->
                    val currentUserId = auth.currentUser?.uid
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
                            inviteError = "Utente non trovato. L'utente deve prima registrarsi su WhereTheDuck."
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
                val currentUser = auth.currentUser
                val debugInfo = "Auth: ${currentUser?.uid ?: "NULL"}, email: ${currentUser?.email ?: "NULL"}"
                _uiState.value = _uiState.value.copy(
                    isInviting = false,
                    inviteError = "${e.message} [$debugInfo]"
                )
            }
        }
    }

    fun clearInviteMessages() {
        _uiState.value = _uiState.value.copy(inviteError = null, inviteSuccess = null)
    }
}
