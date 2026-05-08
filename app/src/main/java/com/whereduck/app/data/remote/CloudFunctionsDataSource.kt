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

    // ── Contacts ──

    suspend fun sendContactInvite(email: String): Map<String, Any> {
        ensureFreshToken()
        val data = hashMapOf("email" to email)
        val result = functions.getHttpsCallable("sendContactInvite")
            .call(data)
            .await()
        @Suppress("UNCHECKED_CAST")
        return result.getData() as Map<String, Any>
    }

    suspend fun respondContactInvite(inviteId: String, accepted: Boolean): Map<String, Any> {
        ensureFreshToken()
        val data = hashMapOf(
            "inviteId" to inviteId,
            "accepted" to accepted
        )
        val result = functions.getHttpsCallable("respondContactInvite")
            .call(data)
            .await()
        @Suppress("UNCHECKED_CAST")
        return result.getData() as Map<String, Any>
    }

    suspend fun removeContact(contactUserId: String): Map<String, Any> {
        ensureFreshToken()
        val data = hashMapOf("contactUserId" to contactUserId)
        val result = functions.getHttpsCallable("removeContact")
            .call(data)
            .await()
        @Suppress("UNCHECKED_CAST")
        return result.getData() as Map<String, Any>
    }

    // ── Starnazzo ──

    suspend fun sendStarnazzo(
        toUserId: String,
        level: String,
        animalType: String? = null
    ): Map<String, Any> {
        ensureFreshToken()
        val data = hashMapOf<String, Any>(
            "toUserId" to toUserId,
            "level" to level
        )
        animalType?.let { data["animalType"] = it }
        val result = functions.getHttpsCallable("sendStarnazzo")
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

    suspend fun revengeStarnazzo(alertId: String): Map<String, Any> {
        ensureFreshToken()
        val data = hashMapOf("alertId" to alertId)
        val result = functions.getHttpsCallable("revengeStarnazzo")
            .call(data)
            .await()
        @Suppress("UNCHECKED_CAST")
        return result.getData() as Map<String, Any>
    }
}
