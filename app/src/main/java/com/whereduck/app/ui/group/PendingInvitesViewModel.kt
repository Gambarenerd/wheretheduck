package com.whereduck.app.ui.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.whereduck.app.data.model.ContactInvite
import com.whereduck.app.data.repository.ContactRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PendingInvitesUiState(
    val isLoading: Boolean = true,
    val invites: List<ContactInvite> = emptyList()
)

@HiltViewModel
class PendingInvitesViewModel @Inject constructor(
    private val contactRepository: ContactRepository,
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
            contactRepository.observePendingInvites(userId)
                .catch { _uiState.value = PendingInvitesUiState(isLoading = false) }
                .collect { invites ->
                    _uiState.value = PendingInvitesUiState(
                        isLoading = false,
                        invites = invites
                    )
                }
        }
    }

    fun respondToInvite(inviteId: String, accepted: Boolean) {
        viewModelScope.launch {
            try {
                contactRepository.respondToInvite(inviteId, accepted)
            } catch (e: Exception) {
                android.util.Log.e("WTD", "respondToInvite error: ${e.message}", e)
            }
        }
    }
}
