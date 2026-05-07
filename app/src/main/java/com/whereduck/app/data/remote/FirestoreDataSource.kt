package com.whereduck.app.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.whereduck.app.data.model.Group
import com.whereduck.app.data.model.GroupInvite
import com.whereduck.app.data.model.Member
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

    // ── Groups ──

    suspend fun createGroup(group: Group, creatorUser: User): String {
        val docRef = firestore.collection("groups").document()
        val groupWithId = group.copy(id = docRef.id)
        docRef.set(groupWithId).await()

        // Add creator as admin member
        val member = Member(
            id = creatorUser.id,
            userId = creatorUser.id,
            displayName = creatorUser.displayName,
            photoUrl = creatorUser.photoUrl,
            role = "admin"
        )
        docRef.collection("members")
            .document(creatorUser.id)
            .set(member)
            .await()

        return docRef.id
    }

    suspend fun getGroup(groupId: String): Group? {
        return firestore.collection("groups")
            .document(groupId)
            .get()
            .await()
            .toObject(Group::class.java)
    }

    fun observeUserGroups(userId: String): Flow<List<Group>> = callbackFlow {
        // First get groups where user is a member by querying all groups
        // and checking membership. For now, we query groups created by user
        // and groups where they are members via a collection group query.
        val membershipQuery = firestore.collectionGroup("members")
            .whereEqualTo("userId", userId)

        val listener = membershipQuery.addSnapshotListener { memberSnapshots, error ->
            if (error != null || memberSnapshots == null) {
                return@addSnapshotListener
            }

            val groupIds = memberSnapshots.documents.mapNotNull { doc ->
                doc.reference.parent.parent?.id
            }

            if (groupIds.isEmpty()) {
                trySend(emptyList())
                return@addSnapshotListener
            }

            // Fetch each group document individually
            val groups = mutableListOf<Group>()
            var remaining = groupIds.size
            for (gid in groupIds) {
                firestore.collection("groups").document(gid).get()
                    .addOnSuccessListener { doc ->
                        doc.toObject(Group::class.java)?.let { groups.add(it) }
                        remaining--
                        if (remaining == 0) {
                            trySend(groups.toList())
                        }
                    }
                    .addOnFailureListener {
                        remaining--
                        if (remaining == 0) {
                            trySend(groups.toList())
                        }
                    }
            }
        }

        awaitClose { listener.remove() }
    }

    fun observeGroupMembers(groupId: String): Flow<List<Member>> = callbackFlow {
        val listener = firestore.collection("groups")
            .document(groupId)
            .collection("members")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val members = snapshot.toObjects(Member::class.java)
                trySend(members)
            }
        awaitClose { listener.remove() }
    }

    // ── Invites ──

    fun observeGroupInvites(groupId: String): Flow<List<GroupInvite>> = callbackFlow {
        val listener = firestore.collection("groups")
            .document(groupId)
            .collection("invites")
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val invites = snapshot.toObjects(GroupInvite::class.java)
                trySend(invites)
            }
        awaitClose { listener.remove() }
    }

    fun observePendingInvitesForUser(userId: String): Flow<List<GroupInvite>> = callbackFlow {
        android.util.Log.d("WTD", "observePendingInvites for userId=$userId")
        val listener = firestore.collectionGroup("invites")
            .whereEqualTo("invitedUserId", userId)
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("WTD", "invites error: ${error.message}", error)
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener
                android.util.Log.d("WTD", "invites found: ${snapshot.size()}")
                val invites = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(GroupInvite::class.java)?.let { invite ->
                        if (invite.groupId.isEmpty()) {
                            val groupIdFromPath = doc.reference.parent.parent?.id ?: ""
                            invite.copy(groupId = groupIdFromPath)
                        } else {
                            invite
                        }
                    }
                }
                trySend(invites)
            }
        awaitClose { listener.remove() }
    }
}
