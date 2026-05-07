package com.whereduck.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.whereduck.app.data.model.Group
import com.whereduck.app.data.repository.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val groups: List<Group> = emptyList(),
    val pendingInviteCount: Int = 0,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadGroups()
        loadPendingInvites()
    }

    private fun loadGroups() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            groupRepository.observeUserGroups(userId)
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
                .collect { groups ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        groups = groups,
                        error = null
                    )
                }
        }
    }

    private fun loadPendingInvites() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            groupRepository.observePendingInvitesForUser(userId)
                .catch { /* ignore */ }
                .collect { invites ->
                    _uiState.value = _uiState.value.copy(
                        pendingInviteCount = invites.size
                    )
                }
        }
    }
}
