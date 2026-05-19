package com.whereduck.app.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class Contact(
    @DocumentId
    val id: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val motto: String = "",
    @ServerTimestamp
    val addedAt: Timestamp? = null
)
