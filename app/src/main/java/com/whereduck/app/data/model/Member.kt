package com.whereduck.app.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class Member(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val photoUrl: String = "",
    val role: String = "member",
    @ServerTimestamp
    val joinedAt: Timestamp? = null
) {
    val isAdmin: Boolean get() = role == "admin"
}
