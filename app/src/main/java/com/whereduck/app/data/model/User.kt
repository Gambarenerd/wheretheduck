package com.whereduck.app.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class User(
    @DocumentId
    val id: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val photoUpdatedAt: Timestamp? = null,
    val motto: String = "",
    val fcmToken: String = "",
    val plan: String = "free",
    val planExpiresAt: Timestamp? = null,
    @ServerTimestamp
    val createdAt: Timestamp? = null
)
