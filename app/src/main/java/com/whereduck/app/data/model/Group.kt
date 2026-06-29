package com.whereduck.app.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class Group(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val photoUrl: String = "",
    val contactIds: List<String> = emptyList(),
    @ServerTimestamp
    val createdAt: Timestamp? = null
)
