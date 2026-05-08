package com.whereduck.app.ui.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.whereduck.app.data.repository.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateGroupViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    fun createGroup(name: String, onResult: (Result<String>) -> Unit) {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid ?: throw Exception("Non autenticato")
                val groupId = groupRepository.createGroup(userId, name)
                onResult(Result.success(groupId))
            } catch (e: Exception) {
                onResult(Result.failure(e))
            }
        }
    }
}
