package com.whereduck.app.data.repository

import com.whereduck.app.data.model.Alert
import com.whereduck.app.data.remote.CloudFunctionsDataSource
import com.whereduck.app.data.remote.FirestoreDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlertRepository @Inject constructor(
    private val cloudFunctions: CloudFunctionsDataSource,
    private val firestoreDataSource: FirestoreDataSource
) {

    suspend fun sendStarnazzo(
        toUserId: String,
        level: String,
        animalType: String? = null
    ): Map<String, Any> {
        return cloudFunctions.sendStarnazzo(toUserId, level, animalType)
    }

    suspend fun sendBroadcast(
        groupId: String,
        level: String,
        animalType: String? = null
    ): Map<String, Any> {
        return cloudFunctions.sendBroadcastStarnazzo(groupId, level, animalType)
    }

    suspend fun cancelStarnazzo(alertId: String): Map<String, Any> {
        return cloudFunctions.cancelStarnazzo(alertId)
    }

    suspend fun revengeStarnazzo(alertId: String): Map<String, Any> {
        return cloudFunctions.revengeStarnazzo(alertId)
    }

    fun observeAlert(alertId: String): Flow<Map<String, Any?>> {
        return firestoreDataSource.observeAlert(alertId)
    }

    fun observeSentAlerts(userId: String): Flow<List<Alert>> {
        return firestoreDataSource.observeSentAlerts(userId)
    }

    fun observeReceivedAlerts(userId: String): Flow<List<Alert>> {
        return firestoreDataSource.observeReceivedAlerts(userId)
    }
}
