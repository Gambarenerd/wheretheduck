package com.whereduck.app.data.repository

import com.whereduck.app.data.remote.CloudFunctionsDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlertRepository @Inject constructor(
    private val cloudFunctions: CloudFunctionsDataSource
) {

    suspend fun sendStarnazzo(
        toUserId: String,
        groupId: String,
        level: String,
        animalType: String? = null
    ): Map<String, Any> {
        return cloudFunctions.sendStarnazzo(toUserId, groupId, level, animalType)
    }

    suspend fun sendBroadcast(
        groupId: String,
        level: String,
        animalType: String? = null
    ): Map<String, Any> {
        return cloudFunctions.sendBroadcastStarnazzo(groupId, level, animalType)
    }
}
