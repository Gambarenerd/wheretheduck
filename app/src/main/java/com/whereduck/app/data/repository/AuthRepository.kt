package com.whereduck.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.whereduck.app.data.model.User
import com.whereduck.app.data.remote.FcmTokenManager
import com.whereduck.app.data.remote.FirestoreDataSource
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestoreDataSource: FirestoreDataSource,
    private val fcmTokenManager: FcmTokenManager
) {

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val isLoggedIn: Boolean
        get() = auth.currentUser != null

    suspend fun signInWithGoogle(idToken: String): FirebaseUser {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        val firebaseUser = result.user ?: throw Exception("Login failed")

        // Check if user already exists in Firestore
        val existingUser = firestoreDataSource.getUser(firebaseUser.uid)

        if (existingUser != null) {
            // User exists — only update email (may change) and displayName if blank,
            // but never overwrite photoUrl (user may have set a custom one)
            val updates = mutableMapOf<String, Any>(
                "email" to (firebaseUser.email ?: "")
            )
            if (existingUser.displayName.isBlank()) {
                updates["displayName"] = firebaseUser.displayName ?: ""
            }
            firestoreDataSource.updateUserFields(firebaseUser.uid, updates)
        } else {
            // New user — create with Google profile data
            val user = User(
                id = firebaseUser.uid,
                displayName = firebaseUser.displayName ?: "",
                email = firebaseUser.email ?: "",
                photoUrl = firebaseUser.photoUrl?.toString() ?: "",
                plan = "free"
            )
            firestoreDataSource.createOrUpdateUser(user)
        }

        fcmTokenManager.registerToken(firebaseUser.uid)

        return firebaseUser
    }

    fun signOut() {
        auth.signOut()
    }
}
