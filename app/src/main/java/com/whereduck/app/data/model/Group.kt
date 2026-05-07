package com.whereduck.app.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class Group(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val createdBy: String = "",
    @ServerTimestamp
    val createdAt: Timestamp? = null
)
