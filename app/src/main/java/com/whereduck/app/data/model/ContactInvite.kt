package com.whereduck.app.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class ContactInvite(
    @DocumentId
    val id: String = "",
    val fromUserId: String = "",
    val fromDisplayName: String = "",
    val fromEmail: String = "",
    val fromPhotoUrl: String = "",
    val toUserId: String = "",
    val toEmail: String = "",
    val status: String = "pending",
    @ServerTimestamp
    val createdAt: Timestamp? = null
) {
    val isPending: Boolean get() = status == "pending"
}
