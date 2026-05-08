package com.whereduck.app.data.repository

import com.whereduck.app.data.model.Group
import com.whereduck.app.data.remote.FirestoreDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupRepository @Inject constructor(
    private val firestoreDataSource: FirestoreDataSource
) {

    fun observeUserGroups(userId: String): Flow<List<Group>> {
        return firestoreDataSource.observeUserGroups(userId)
    }

    suspend fun getGroup(userId: String, groupId: String): Group? {
        return firestoreDataSource.getGroup(userId, groupId)
    }

    suspend fun createGroup(userId: String, name: String): String {
        return firestoreDataSource.createPersonalGroup(userId, name)
    }

    suspend fun deleteGroup(userId: String, groupId: String) {
        firestoreDataSource.deletePersonalGroup(userId, groupId)
    }

    suspend fun renameGroup(userId: String, groupId: String, newName: String) {
        firestoreDataSource.renameGroup(userId, groupId, newName)
    }

    suspend fun addContactToGroup(userId: String, groupId: String, contactId: String) {
        firestoreDataSource.addContactToGroup(userId, groupId, contactId)
    }

    suspend fun removeContactFromGroup(userId: String, groupId: String, contactId: String) {
        firestoreDataSource.removeContactFromGroup(userId, groupId, contactId)
    }
}
