package com.whereduck.app.data.remote

import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FcmTokenManager @Inject constructor(
    private val messaging: FirebaseMessaging,
    private val firestoreDataSource: FirestoreDataSource
) {

    suspend fun getToken(): String {
        return messaging.token.await()
    }

    suspend fun registerToken(userId: String) {
        val token = getToken()
        firestoreDataSource.updateFcmToken(userId, token)
    }
}
