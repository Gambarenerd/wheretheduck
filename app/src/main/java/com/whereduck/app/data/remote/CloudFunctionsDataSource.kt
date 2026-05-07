package com.whereduck.app.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudFunctionsDataSource @Inject constructor(
    private val functions: FirebaseFunctions,
    private val auth: FirebaseAuth
) {

    private suspend fun ensureFreshToken() {
        auth.currentUser?.getIdToken(true)?.await()
    }

    suspend fun sendGroupInvite(groupId: String, email: String): Map<String, Any> {
        ensureFreshToken()
        val data = hashMapOf(
            "groupId" to groupId,
            "email" to email
        )
        val result = functions.getHttpsCallable("sendGroupInvite")
            .call(data)
            .await()
        @Suppress("UNCHECKED_CAST")
        return result.getData() as Map<String, Any>
    }

    suspend fun respondGroupInvite(
        groupId: String,
        inviteId: String,
        accepted: Boolean
    ): Map<String, Any> {
        ensureFreshToken()
        val data = hashMapOf(
            "groupId" to groupId,
            "inviteId" to inviteId,
            "accepted" to accepted
        )
        val result = functions.getHttpsCallable("respondGroupInvite")
            .call(data)
            .await()
        @Suppress("UNCHECKED_CAST")
        return result.getData() as Map<String, Any>
    }

    suspend fun sendStarnazzo(
        toUserId: String,
        groupId: String,
        level: String,
        animalType: String? = null
    ): Map<String, Any> {
        ensureFreshToken()
        val data = hashMapOf<String, Any>(
            "toUserId" to toUserId,
            "groupId" to groupId,
            "level" to level
        )
        animalType?.let { data["animalType"] = it }
        val result = functions.getHttpsCallable("sendStarnazzo")
            .call(data)
            .await()
        @Suppress("UNCHECKED_CAST")
        return result.getData() as Map<String, Any>
    }

    suspend fun deleteGroup(groupId: String): Map<String, Any> {
        ensureFreshToken()
        val data = hashMapOf("groupId" to groupId)
        val result = functions.getHttpsCallable("deleteGroup")
            .call(data)
            .await()
        @Suppress("UNCHECKED_CAST")
        return result.getData() as Map<String, Any>
    }

    suspend fun removeMember(groupId: String, memberId: String): Map<String, Any> {
        ensureFreshToken()
        val data = hashMapOf(
            "groupId" to groupId,
            "memberId" to memberId
        )
        val result = functions.getHttpsCallable("removeMember")
            .call(data)
            .await()
        @Suppress("UNCHECKED_CAST")
        return result.getData() as Map<String, Any>
    }

    suspend fun respondStarnazzo(
        alertId: String,
        response: String,
        muteDurationMinutes: Int? = null
    ): Map<String, Any> {
        ensureFreshToken()
        val data = hashMapOf<String, Any>(
            "alertId" to alertId,
            "response" to response
        )
        muteDurationMinutes?.let { data["muteDurationMinutes"] = it }
        val result = functions.getHttpsCallable("respondStarnazzo")
            .call(data)
            .await()
        @Suppress("UNCHECKED_CAST")
        return result.getData() as Map<String, Any>
    }

    suspend fun sendBroadcastStarnazzo(
        groupId: String,
        level: String,
        animalType: String? = null
    ): Map<String, Any> {
        ensureFreshToken()
        val data = hashMapOf<String, Any>(
            "groupId" to groupId,
            "level" to level
        )
        animalType?.let { data["animalType"] = it }
        val result = functions.getHttpsCallable("sendBroadcastStarnazzo")
            .call(data)
            .await()
        @Suppress("UNCHECKED_CAST")
        return result.getData() as Map<String, Any>
    }
}
