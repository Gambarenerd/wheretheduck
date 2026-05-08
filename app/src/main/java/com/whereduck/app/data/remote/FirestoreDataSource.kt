package com.whereduck.app.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.FieldValue
import com.whereduck.app.data.model.Contact
import com.whereduck.app.data.model.ContactInvite
import com.whereduck.app.data.model.Group
import com.whereduck.app.data.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    // ── Users ──

    suspend fun createOrUpdateUser(user: User) {
        firestore.collection("users")
            .document(user.id)
            .set(user, SetOptions.merge())
            .await()
    }

    suspend fun getUser(userId: String): User? {
        return firestore.collection("users")
            .document(userId)
            .get()
            .await()
            .toObject(User::class.java)
    }

    suspend fun updateFcmToken(userId: String, token: String) {
        firestore.collection("users")
            .document(userId)
            .update("fcmToken", token)
            .await()
    }

    // ── Contacts ──

    fun observeContacts(userId: String): Flow<List<Contact>> = callbackFlow {
        val listener = firestore.collection("users")
            .document(userId)
            .collection("contacts")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val contacts = snapshot.toObjects(Contact::class.java)
                trySend(contacts)
            }
        awaitClose { listener.remove() }
    }

    fun observePendingContactInvites(userId: String): Flow<List<ContactInvite>> = callbackFlow {
        val listener = firestore.collection("contact_invites")
            .whereEqualTo("toUserId", userId)
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val invites = snapshot.toObjects(ContactInvite::class.java)
                trySend(invites)
            }
        awaitClose { listener.remove() }
    }

    // ── Personal Groups ──

    fun observeUserGroups(userId: String): Flow<List<Group>> = callbackFlow {
        val listener = firestore.collection("users")
            .document(userId)
            .collection("groups")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val groups = snapshot.toObjects(Group::class.java)
                trySend(groups)
            }
        awaitClose { listener.remove() }
    }

    suspend fun getGroup(userId: String, groupId: String): Group? {
        return firestore.collection("users")
            .document(userId)
            .collection("groups")
            .document(groupId)
            .get()
            .await()
            .toObject(Group::class.java)
    }

    suspend fun createPersonalGroup(userId: String, name: String): String {
        val docRef = firestore.collection("users")
            .document(userId)
            .collection("groups")
            .document()
        docRef.set(
            hashMapOf(
                "name" to name,
                "contactIds" to emptyList<String>(),
                "createdAt" to FieldValue.serverTimestamp()
            )
        ).await()
        return docRef.id
    }

    suspend fun deletePersonalGroup(userId: String, groupId: String) {
        firestore.collection("users")
            .document(userId)
            .collection("groups")
            .document(groupId)
            .delete()
            .await()
    }

    suspend fun renameGroup(userId: String, groupId: String, newName: String) {
        firestore.collection("users")
            .document(userId)
            .collection("groups")
            .document(groupId)
            .update("name", newName)
            .await()
    }

    suspend fun addContactToGroup(userId: String, groupId: String, contactId: String) {
        firestore.collection("users")
            .document(userId)
            .collection("groups")
            .document(groupId)
            .update("contactIds", FieldValue.arrayUnion(contactId))
            .await()
    }

    suspend fun removeContactFromGroup(userId: String, groupId: String, contactId: String) {
        firestore.collection("users")
            .document(userId)
            .collection("groups")
            .document(groupId)
            .update("contactIds", FieldValue.arrayRemove(contactId))
            .await()
    }

    // ── Alerts ──

    fun observeAlert(alertId: String): Flow<Map<String, Any?>> = callbackFlow {
        val listener = firestore.collection("alerts")
            .document(alertId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                val data = snapshot.data ?: return@addSnapshotListener
                trySend(data)
            }
        awaitClose { listener.remove() }
    }
}
