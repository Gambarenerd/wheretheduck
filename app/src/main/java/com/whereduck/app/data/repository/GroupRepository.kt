package com.whereduck.app.data.repository

import com.whereduck.app.data.model.Group
import com.whereduck.app.data.model.GroupInvite
import com.whereduck.app.data.model.Member
import com.whereduck.app.data.model.User
import com.whereduck.app.data.remote.CloudFunctionsDataSource
import com.whereduck.app.data.remote.FirestoreDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupRepository @Inject constructor(
    private val firestoreDataSource: FirestoreDataSource,
    private val cloudFunctions: CloudFunctionsDataSource
) {

    suspend fun createGroup(name: String, creatorUser: User): String {
        val group = Group(name = name, createdBy = creatorUser.id)
        return firestoreDataSource.createGroup(group, creatorUser)
    }

    suspend fun getGroup(groupId: String): Group? {
        return firestoreDataSource.getGroup(groupId)
    }

    fun observeUserGroups(userId: String): Flow<List<Group>> {
        return firestoreDataSource.observeUserGroups(userId)
    }

    fun observeGroupMembers(groupId: String): Flow<List<Member>> {
        return firestoreDataSource.observeGroupMembers(groupId)
    }

    fun observeGroupInvites(groupId: String): Flow<List<GroupInvite>> {
        return firestoreDataSource.observeGroupInvites(groupId)
    }

    fun observePendingInvitesForUser(userId: String): Flow<List<GroupInvite>> {
        return firestoreDataSource.observePendingInvitesForUser(userId)
    }

    suspend fun sendInvite(groupId: String, email: String): Map<String, Any> {
        return cloudFunctions.sendGroupInvite(groupId, email)
    }

    suspend fun respondToInvite(
        groupId: String,
        inviteId: String,
        accepted: Boolean
    ): Map<String, Any> {
        return cloudFunctions.respondGroupInvite(groupId, inviteId, accepted)
    }
}
