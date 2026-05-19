package com.whereduck.app.ui.settings

import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.whereduck.app.data.model.User
import com.whereduck.app.data.remote.FirestoreDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject

enum class AppLanguage(val tag: String, val displayName: String) {
    ENGLISH("en", "English"),
    ITALIAN("it", "Italiano"),
    FRENCH("fr", "Français"),
    SPANISH("es", "Español")
}

enum class AppTheme(val displayName: String) {
    LIGHT("Light"),
    DARK("Dark")
}

data class SettingsUiState(
    val user: User? = null,
    val profilePicturePath: String? = null,
    val currentLanguage: AppLanguage = AppLanguage.ENGLISH,
    val currentTheme: AppTheme = AppTheme.LIGHT,
    val currentTier: String = "free",
    val isSaving: Boolean = false,
    val message: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val firestoreDataSource: FirestoreDataSource,
    private val auth: FirebaseAuth,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val prefs = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)

    init {
        loadSettings()
        observeUser()
    }

    private fun loadSettings() {
        val langTag = prefs.getString("language", null)
            ?: AppCompatDelegate.getApplicationLocales().toLanguageTags().take(2)
        val language = AppLanguage.entries.find { it.tag == langTag } ?: AppLanguage.ENGLISH

        val themeName = prefs.getString("theme", "LIGHT") ?: "LIGHT"
        val theme = try { AppTheme.valueOf(themeName) } catch (_: Exception) { AppTheme.LIGHT }

        val picturePath = prefs.getString("profile_picture_path", null)

        _uiState.value = _uiState.value.copy(
            currentLanguage = language,
            currentTheme = theme,
            profilePicturePath = picturePath
        )
    }

    private fun observeUser() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            firestoreDataSource.observeUser(userId)
                .catch { /* ignore */ }
                .collect { user ->
                    _uiState.value = _uiState.value.copy(
                        user = user,
                        currentTier = user?.plan ?: "free"
                    )
                }
        }
    }

    fun updateDisplayName(newName: String) {
        val userId = auth.currentUser?.uid ?: return
        if (newName.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            try {
                // Update Firestore
                firestoreDataSource.updateDisplayName(userId, newName)
                // Update Firebase Auth profile
                auth.currentUser?.updateProfile(
                    UserProfileChangeRequest.Builder()
                        .setDisplayName(newName)
                        .build()
                )?.await()
                _uiState.value = _uiState.value.copy(isSaving = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    message = "Errore: ${e.message}"
                )
            }
        }
    }

    fun updateProfilePicture(uri: Uri) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(isSaving = true)
            try {
                // 1. Copy to local storage for immediate display
                val file = File(context.filesDir, "profile_avatar.jpg")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    file.outputStream().use { output -> input.copyTo(output) }
                } ?: run {
                    _uiState.value = _uiState.value.copy(isSaving = false)
                    return@launch
                }

                val localPath = file.absolutePath + "?t=${System.currentTimeMillis()}"
                prefs.edit().putString("profile_picture_path", localPath).apply()
                _uiState.value = _uiState.value.copy(profilePicturePath = localPath)

                // 2. Upload to Firebase Storage
                val storageRef = FirebaseStorage.getInstance()
                    .reference
                    .child("profile_photos/$userId.jpg")

                val metadata = StorageMetadata.Builder()
                    .setContentType("image/jpeg")
                    .build()
                storageRef.putBytes(file.readBytes(), metadata).await()

                // 3. Get public download URL
                val downloadUrl = storageRef.downloadUrl.await().toString()

                // 4. Save download URL to Firestore (visible to other users)
                firestoreDataSource.updatePhotoUrl(userId, downloadUrl)

                // 5. Update Firebase Auth profile photo
                auth.currentUser?.updateProfile(
                    UserProfileChangeRequest.Builder()
                        .setPhotoUri(Uri.parse(downloadUrl))
                        .build()
                )?.await()

                _uiState.value = _uiState.value.copy(isSaving = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    message = "Errore foto: ${e.message}"
                )
            }
        }
    }

    fun setLanguage(language: AppLanguage) {
        prefs.edit().putString("language", language.tag).apply()
        _uiState.value = _uiState.value.copy(currentLanguage = language)
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(language.tag)
        )
    }

    fun setTheme(theme: AppTheme) {
        prefs.edit().putString("theme", theme.name).apply()
        _uiState.value = _uiState.value.copy(currentTheme = theme)
    }

    fun updateMotto(newMotto: String) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                firestoreDataSource.updateMotto(userId, newMotto.take(50))
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    message = "Errore: ${e.message}"
                )
            }
        }
    }

    fun setTier(tier: String) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                firestoreDataSource.updateUserPlan(userId, tier)
                _uiState.value = _uiState.value.copy(currentTier = tier)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    message = "Errore: ${e.message}"
                )
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    fun signOut() {
        auth.signOut()
    }
}
