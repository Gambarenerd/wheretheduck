package com.whereduck.app.ui.permissions

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.DoNotDisturb
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.whereduck.app.R
import com.whereduck.app.ui.theme.DuckOrange500
import com.whereduck.app.ui.theme.DuckTheme
import com.whereduck.app.ui.theme.StarnazzoLight
import com.whereduck.app.ui.theme.StarnazzoMedium
import com.whereduck.app.util.OemBatteryHelper

@Composable
fun PermissionSetupScreen(
    onAllPermissionsGranted: () -> Unit,
    viewModel: PermissionSetupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { viewModel.refreshPermissions(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DuckTheme.colors.sectionDashboard)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        Text(
            text = stringResource(R.string.permissions_welcome),
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = DuckTheme.colors.textPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.permissions_subtitle),
            fontSize = 15.sp,
            color = DuckTheme.colors.textSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        // 1. Notifications (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PermissionCard(
                icon = Icons.Default.NotificationsActive,
                title = stringResource(R.string.permissions_notifications_title),
                description = stringResource(R.string.permissions_notifications_desc),
                isGranted = uiState.notificationsGranted,
                onRequest = {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 2. DND Access
        PermissionCard(
            icon = Icons.Default.DoNotDisturb,
            title = stringResource(R.string.permissions_dnd_title),
            description = stringResource(R.string.permissions_dnd_desc),
            isGranted = uiState.dndAccessGranted,
            onRequest = {
                context.startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 3. Battery Optimization
        PermissionCard(
            icon = Icons.Default.BatteryChargingFull,
            title = stringResource(R.string.permissions_battery_title),
            description = stringResource(R.string.permissions_battery_desc),
            isGranted = uiState.batteryOptimizationDisabled,
            onRequest = {
                val intent = Intent(
                    Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                    android.net.Uri.parse("package:${context.packageName}")
                )
                context.startActivity(intent)
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 4. Full Screen Intent (Android 14+)
        if (Build.VERSION.SDK_INT >= 34) {
            PermissionCard(
                icon = Icons.Default.Fullscreen,
                title = stringResource(R.string.permissions_fullscreen_title),
                description = stringResource(R.string.permissions_fullscreen_desc),
                isGranted = uiState.fullScreenIntentGranted,
                onRequest = {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT)
                    context.startActivity(intent)
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // OEM-specific instructions
        val oemInstruction = OemBatteryHelper.getInstruction()
        if (oemInstruction != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = StarnazzoMedium.copy(alpha = 0.12f)
                ),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.permissions_oem_important, oemInstruction.manufacturer),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = DuckOrange500
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    oemInstruction.steps.forEachIndexed { index, step ->
                        Text(
                            text = "${index + 1}. $step",
                            fontSize = 14.sp,
                            color = DuckTheme.colors.textPrimary,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                    if (oemInstruction.settingsIntent != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { OemBatteryHelper.tryOpenSettings(context, oemInstruction) },
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DuckOrange500,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                stringResource(R.string.permissions_oem_open_settings, oemInstruction.manufacturer),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Warning about force stop
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFEEBEBE)
            ),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = DuckTheme.colors.negative,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.permissions_warning),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = DuckTheme.colors.negative
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Continue button
        Button(
            onClick = {
                viewModel.refreshPermissions(context)
                if (uiState.allGranted) {
                    onAllPermissionsGranted()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (uiState.allGranted) StarnazzoLight else DuckTheme.colors.outline,
                contentColor = Color.White
            ),
            enabled = uiState.allGranted
        ) {
            Text(
                text = if (uiState.allGranted) stringResource(R.string.permissions_continue) else stringResource(R.string.permissions_grant_all),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (!uiState.allGranted) {
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = { viewModel.refreshPermissions(context) },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DuckTheme.colors.cardBackground,
                    contentColor = DuckTheme.colors.textPrimary
                )
            ) {
                Text(
                    stringResource(R.string.permissions_refresh),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun PermissionCard(
    icon: ImageVector,
    title: String,
    description: String,
    isGranted: Boolean,
    onRequest: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DuckTheme.colors.cardBackground),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon circle
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (isGranted) StarnazzoLight.copy(alpha = 0.15f)
                        else DuckOrange500.copy(alpha = 0.12f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isGranted) Icons.Default.Check else icon,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = if (isGranted) StarnazzoLight else DuckOrange500
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = DuckTheme.colors.textPrimary
                )
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = DuckTheme.colors.textSecondary
                )
            }

            if (!isGranted) {
                Spacer(modifier = Modifier.width(10.dp))
                Button(
                    onClick = onRequest,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DuckOrange500,
                        contentColor = Color.White
                    )
                ) {
                    Text(stringResource(R.string.permissions_enable), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            } else {
                Spacer(modifier = Modifier.width(10.dp))
                Surface(
                    shape = CircleShape,
                    color = StarnazzoLight.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = stringResource(R.string.permissions_granted),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = StarnazzoLight,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}
