package com.whereduck.app.data.repository

import com.whereduck.app.data.model.Contact
import com.whereduck.app.data.model.ContactInvite
import com.whereduck.app.data.remote.CloudFunctionsDataSource
import com.whereduck.app.data.remote.FirestoreDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactRepository @Inject constructor(
    private val firestoreDataSource: FirestoreDataSource,
    private val cloudFunctions: CloudFunctionsDataSource
) {

    fun observeContacts(userId: String): Flow<List<Contact>> {
        return firestoreDataSource.observeContacts(userId)
    }

    fun observePendingInvites(userId: String): Flow<List<ContactInvite>> {
        return firestoreDataSource.observePendingContactInvites(userId)
    }

    suspend fun sendInvite(email: String): Map<String, Any> {
        return cloudFunctions.sendContactInvite(email)
    }

    suspend fun respondToInvite(inviteId: String, accepted: Boolean): Map<String, Any> {
        return cloudFunctions.respondContactInvite(inviteId, accepted)
    }

    suspend fun removeContact(contactUserId: String): Map<String, Any> {
        return cloudFunctions.removeContact(contactUserId)
    }
}
