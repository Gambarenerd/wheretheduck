package com.whereduck.app.ui.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.whereduck.app.data.model.GroupInvite
import com.whereduck.app.data.repository.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PendingInvitesUiState(
    val isLoading: Boolean = true,
    val invites: List<GroupInvite> = emptyList()
)

@HiltViewModel
class PendingInvitesViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(PendingInvitesUiState())
    val uiState: StateFlow<PendingInvitesUiState> = _uiState.asStateFlow()

    init {
        loadInvites()
    }

    private fun loadInvites() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            groupRepository.observePendingInvitesForUser(userId)
                .catch { _uiState.value = PendingInvitesUiState(isLoading = false) }
                .collect { invites ->
                    _uiState.value = PendingInvitesUiState(
                        isLoading = false,
                        invites = invites
                    )
                }
        }
    }

    fun respondToInvite(groupId: String, inviteId: String, accepted: Boolean) {
        viewModelScope.launch {
            try {
                android.util.Log.d("WTD", "respondToInvite groupId=$groupId inviteId=$inviteId accepted=$accepted")
                groupRepository.respondToInvite(groupId, inviteId, accepted)
                android.util.Log.d("WTD", "respondToInvite success")
            } catch (e: Exception) {
                android.util.Log.e("WTD", "respondToInvite error: ${e.message}", e)
            }
        }
    }
}
