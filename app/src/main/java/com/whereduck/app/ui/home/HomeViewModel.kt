package com.whereduck.app.ui.home

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.whereduck.app.data.model.Contact
import com.whereduck.app.data.model.Group
import com.whereduck.app.data.model.StarnazzoLevel
import com.whereduck.app.data.repository.AlertRepository
import com.whereduck.app.data.repository.ContactRepository
import com.whereduck.app.data.repository.GroupRepository
import android.content.SharedPreferences
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

data class HomeUiState(
    val isLoading: Boolean = true,
    val groups: List<Group> = emptyList(),
    val contacts: List<Contact> = emptyList(),
    val vipContactIds: List<String> = emptyList(),
    val pendingInviteCount: Int = 0,
    val sendingQuickStarnazzoTo: String? = null,
    val error: String? = null
)

data class QuickStarnazzoEvent(
    val alertId: String,
    val toName: String,
    val level: String
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val app: Application,
    private val groupRepository: GroupRepository,
    private val contactRepository: ContactRepository,
    private val alertRepository: AlertRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _quickStarnazzoSent = MutableSharedFlow<QuickStarnazzoEvent>()
    val quickStarnazzoSent: SharedFlow<QuickStarnazzoEvent> = _quickStarnazzoSent.asSharedFlow()

    private val vipPrefs by lazy {
        app.getSharedPreferences("vip_prefs", Context.MODE_PRIVATE)
    }

    private val vipListener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
        loadVipContacts()
    }

    init {
        loadGroups()
        loadContacts()
        loadPendingInvites()
        loadVipContacts()
        vipPrefs.registerOnSharedPreferenceChangeListener(vipListener)
    }

    override fun onCleared() {
        super.onCleared()
        vipPrefs.unregisterOnSharedPreferenceChangeListener(vipListener)
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

    private fun loadContacts() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            contactRepository.observeContacts(userId)
                .catch { /* ignore */ }
                .collect { contacts ->
                    _uiState.value = _uiState.value.copy(contacts = contacts)
                }
        }
    }

    private fun loadPendingInvites() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            contactRepository.observePendingInvites(userId)
                .catch { /* ignore */ }
                .collect { invites ->
                    _uiState.value = _uiState.value.copy(
                        pendingInviteCount = invites.size
                    )
                }
        }
    }

    fun sendContactInvite(email: String) {
        viewModelScope.launch {
            try {
                contactRepository.sendInvite(email)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Errore invio invito"
                )
            }
        }
    }

    private fun loadVipContacts() {
        val userId = auth.currentUser?.uid ?: return
        val ids = vipPrefs.getString("vip_$userId", null)
            ?.split(",")
            ?.filter { it.isNotBlank() }
            ?: emptyList()
        _uiState.value = _uiState.value.copy(vipContactIds = ids)
    }

    private fun saveVipContacts(ids: List<String>) {
        val userId = auth.currentUser?.uid ?: return
        vipPrefs.edit().putString("vip_$userId", ids.joinToString(",")).apply()
        _uiState.value = _uiState.value.copy(vipContactIds = ids)
    }

    fun addVip(contactId: String) {
        val current = _uiState.value.vipContactIds
        if (contactId in current || current.size >= 4) return
        saveVipContacts(current + contactId)
    }

    fun removeVip(contactId: String) {
        val current = _uiState.value.vipContactIds
        saveVipContacts(current - contactId)
    }

    fun sendQuickStarnazzo(contactId: String) {
        if (_uiState.value.sendingQuickStarnazzoTo != null) return
        val level = StarnazzoLevel.MEDIUM
        val contact = _uiState.value.contacts.find { it.id == contactId }
        val toName = contact?.displayName?.ifBlank { contact.email } ?: "Qualcuno"

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(sendingQuickStarnazzoTo = contactId)
            try {
                val result = alertRepository.sendStarnazzo(
                    toUserId = contactId,
                    level = level.key,
                    animalType = level.defaultAnimal
                )
                val status = result["status"] as? String
                val alertId = result["alertId"] as? String ?: ""

                _uiState.value = _uiState.value.copy(sendingQuickStarnazzoTo = null)

                if (status == "sent") {
                    _quickStarnazzoSent.emit(QuickStarnazzoEvent(alertId, toName, level.key))
                }
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(sendingQuickStarnazzoTo = null)
            }
        }
    }
}
