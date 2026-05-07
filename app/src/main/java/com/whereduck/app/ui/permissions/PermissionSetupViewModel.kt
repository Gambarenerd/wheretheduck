package com.whereduck.app.ui.permissions

import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class PermissionUiState(
    val notificationsGranted: Boolean = false,
    val dndAccessGranted: Boolean = false,
    val batteryOptimizationDisabled: Boolean = false,
    val fullScreenIntentGranted: Boolean = false
) {
    val allGranted: Boolean
        get() = notificationsGranted && dndAccessGranted && batteryOptimizationDisabled &&
                (Build.VERSION.SDK_INT < 34 || fullScreenIntentGranted)
}

@HiltViewModel
class PermissionSetupViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(PermissionUiState())
    val uiState: StateFlow<PermissionUiState> = _uiState.asStateFlow()

    init {
        refreshPermissions(context)
    }

    fun refreshPermissions(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
            as NotificationManager
        val powerManager = context.getSystemService(Context.POWER_SERVICE)
            as PowerManager

        val notificationsGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        val dndAccessGranted = notificationManager.isNotificationPolicyAccessGranted

        val batteryOptimizationDisabled = powerManager.isIgnoringBatteryOptimizations(
            context.packageName
        )

        val fullScreenIntentGranted = if (Build.VERSION.SDK_INT >= 34) {
            notificationManager.canUseFullScreenIntent()
        } else {
            true
        }

        _uiState.value = PermissionUiState(
            notificationsGranted = notificationsGranted,
            dndAccessGranted = dndAccessGranted,
            batteryOptimizationDisabled = batteryOptimizationDisabled,
            fullScreenIntentGranted = fullScreenIntentGranted
        )
    }
}
