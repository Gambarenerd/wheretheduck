package com.whereduck.app.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class Alert(
    @DocumentId
    val id: String = "",
    val fromUserId: String = "",
    val fromDisplayName: String = "",
    val toUserId: String = "",
    val toDisplayName: String = "",
    val groupId: String? = null,
    val broadcastId: String? = null,
    val starnazzoLevel: String = "medium",
    val animalType: String = "duck",
    val status: String = "sending",
    val response: String? = null,
    val muteDuration: Int? = null,
    val isRevenge: Boolean = false,
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    val deliveredAt: Timestamp? = null,
    val respondedAt: Timestamp? = null
)
