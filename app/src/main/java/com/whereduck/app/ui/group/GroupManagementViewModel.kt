package com.whereduck.app.ui.group

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.whereduck.app.data.model.Contact
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

data class GroupManagementUiState(
    val groupName: String = "",
    val groupContactIds: List<String> = emptyList(),
    val groupContacts: List<Contact> = emptyList(),
    val allContacts: List<Contact> = emptyList(),
    val actionError: String? = null,
    val isDeleting: Boolean = false
)

@HiltViewModel
class GroupManagementViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val groupRepository: GroupRepository,
    private val contactRepository: ContactRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    val groupId: String = savedStateHandle["groupId"] ?: ""
    private val userId: String = auth.currentUser?.uid ?: ""

    private val _uiState = MutableStateFlow(GroupManagementUiState())
    val uiState: StateFlow<GroupManagementUiState> = _uiState.asStateFlow()

    private val _groupDeleted = MutableSharedFlow<Unit>()
    val groupDeleted: SharedFlow<Unit> = _groupDeleted.asSharedFlow()

    init {
        if (groupId.isNotEmpty() && userId.isNotEmpty()) {
            loadGroupData()
        }
    }

    private fun loadGroupData() {
        viewModelScope.launch {
            groupRepository.observeUserGroups(userId)
                .combine(contactRepository.observeContacts(userId)) { groups, contacts ->
                    val group = groups.find { it.id == groupId }
                    Triple(group, contacts, group?.contactIds ?: emptyList())
                }
                .catch { /* ignore */ }
                .collect { (group, allContacts, contactIds) ->
                    val groupContacts = allContacts.filter { it.id in contactIds }
                    _uiState.value = _uiState.value.copy(
                        groupName = group?.name ?: "",
                        groupContactIds = contactIds,
                        groupContacts = groupContacts,
                        allContacts = allContacts
                    )
                }
        }
    }

    fun renameGroup(newName: String) {
        viewModelScope.launch {
            try {
                groupRepository.renameGroup(userId, groupId, newName)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(actionError = "Errore: ${e.message}")
            }
        }
    }

    fun addContactToGroup(contactId: String) {
        viewModelScope.launch {
            try {
                groupRepository.addContactToGroup(userId, groupId, contactId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(actionError = "Errore: ${e.message}")
            }
        }
    }

    fun removeContactFromGroup(contactId: String) {
        viewModelScope.launch {
            try {
                groupRepository.removeContactFromGroup(userId, groupId, contactId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(actionError = "Errore: ${e.message}")
            }
        }
    }

    fun deleteGroup() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true, actionError = null)
            try {
                groupRepository.deleteGroup(userId, groupId)
                _groupDeleted.emit(Unit)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    actionError = "Errore: ${e.message}"
                )
            }
        }
    }

    fun clearActionError() {
        _uiState.value = _uiState.value.copy(actionError = null)
    }
}
