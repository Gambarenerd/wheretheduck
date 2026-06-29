package com.whereduck.app.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.whereduck.app.data.model.Alert
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

    suspend fun updateUserFields(userId: String, fields: Map<String, Any>) {
        firestore.collection("users")
            .document(userId)
            .update(fields)
            .await()
    }

    suspend fun updateDisplayName(userId: String, displayName: String) {
        firestore.collection("users")
            .document(userId)
            .update("displayName", displayName)
            .await()
    }

    suspend fun updatePhotoUrl(userId: String, photoUrl: String) {
        firestore.collection("users")
            .document(userId)
            .update(
                mapOf(
                    "photoUrl" to photoUrl,
                    "photoUpdatedAt" to FieldValue.serverTimestamp()
                )
            )
            .await()
    }

    suspend fun updateMotto(userId: String, motto: String) {
        firestore.collection("users")
            .document(userId)
            .update("motto", motto)
            .await()
    }

    suspend fun updateUserPlan(userId: String, plan: String) {
        firestore.collection("users")
            .document(userId)
            .update("plan", plan)
            .await()
    }

    fun observeUser(userId: String): Flow<User?> = callbackFlow {
        val listener = firestore.collection("users")
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val user = snapshot.toObject(User::class.java)
                trySend(user)
            }
        awaitClose { listener.remove() }
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

    suspend fun updateGroupPhotoUrl(userId: String, groupId: String, photoUrl: String) {
        firestore.collection("users")
            .document(userId)
            .collection("groups")
            .document(groupId)
            .update("photoUrl", photoUrl)
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

    fun observeSentAlerts(userId: String): Flow<List<Alert>> = callbackFlow {
        val listener = firestore.collection("alerts")
            .whereEqualTo("fromUserId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val alerts = snapshot.toObjects(Alert::class.java)
                trySend(alerts)
            }
        awaitClose { listener.remove() }
    }

    fun observeReceivedAlerts(userId: String): Flow<List<Alert>> = callbackFlow {
        val listener = firestore.collection("alerts")
            .whereEqualTo("toUserId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val alerts = snapshot.toObjects(Alert::class.java)
                trySend(alerts)
            }
        awaitClose { listener.remove() }
    }
}
