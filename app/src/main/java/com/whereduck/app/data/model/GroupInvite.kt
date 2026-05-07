package com.whereduck.app.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class GroupInvite(
    @DocumentId
    val id: String = "",
    val groupId: String = "",
    val invitedEmail: String = "",
    val invitedUserId: String = "",
    val invitedDisplayName: String = "",
    val invitedBy: String = "",
    val invitedByDisplayName: String = "",
    val groupName: String = "",
    val status: String = "pending",
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    val respondedAt: Timestamp? = null
) {
    val isPending: Boolean get() = status == "pending"
}
