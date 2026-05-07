package com.whereduck.app.ui.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.whereduck.app.data.model.User
import com.whereduck.app.data.remote.FirestoreDataSource
import com.whereduck.app.data.repository.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateGroupViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val firestoreDataSource: FirestoreDataSource,
    private val auth: FirebaseAuth
) : ViewModel() {

    fun createGroup(name: String, onResult: (Result<String>) -> Unit) {
        viewModelScope.launch {
            try {
                val firebaseUser = auth.currentUser ?: throw Exception("Non autenticato")
                val user = firestoreDataSource.getUser(firebaseUser.uid)
                    ?: User(
                        id = firebaseUser.uid,
                        displayName = firebaseUser.displayName ?: "",
                        email = firebaseUser.email ?: "",
                        photoUrl = firebaseUser.photoUrl?.toString() ?: ""
                    )
                val groupId = groupRepository.createGroup(name, user)
                onResult(Result.success(groupId))
            } catch (e: Exception) {
                onResult(Result.failure(e))
            }
        }
    }
}
