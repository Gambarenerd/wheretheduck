package com.whereduck.app.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImage
import com.whereduck.app.R
import com.whereduck.app.ui.theme.DuckOrange500
import com.whereduck.app.ui.theme.DuckTheme
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showNameDialog by remember { mutableStateOf(false) }
    var showMottoDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.updateProfilePicture(it) }
    }

    Scaffold(
        containerColor = DuckTheme.colors.sectionDashboard,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.common_back))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // ── Profile Photo ──
            Box(contentAlignment = Alignment.Center) {
                val pictureFile = uiState.profilePicturePath?.substringBefore("?")
                    ?.let { File(it) }?.takeIf { it.exists() }
                val cacheKey = uiState.profilePicturePath // includes ?t=timestamp for cache busting

                Surface(
                    modifier = Modifier.size(180.dp),
                    shape = CircleShape,
                    color = DuckTheme.colors.accent
                ) {
                    if (pictureFile != null) {
                        AsyncImage(
                            model = coil.request.ImageRequest.Builder(LocalContext.current)
                                .data(pictureFile)
                                .memoryCacheKey(cacheKey)
                                .diskCacheKey(cacheKey)
                                .build(),
                            contentDescription = stringResource(R.string.settings_photo_desc),
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            val initial = (uiState.user?.displayName?.firstOrNull() ?: 'U')
                                .uppercase()
                            Text(
                                text = initial,
                                fontSize = 48.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = DuckTheme.colors.textOnAccent
                            )
                        }
                    }
                }

                // Camera edit button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = (-10).dp, y = (-10).dp)
                        .clip(CircleShape)
                        .background(DuckTheme.colors.cardBackground)
                        .clickable { photoPickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = stringResource(R.string.settings_change_photo_desc),
                        modifier = Modifier.size(18.dp),
                        tint = DuckTheme.colors.textPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Display Name (clickable) ──
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { showNameDialog = true }
            ) {
                Text(
                    text = uiState.user?.displayName?.ifBlank { stringResource(R.string.settings_user_fallback) } ?: stringResource(R.string.settings_user_fallback),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = DuckTheme.colors.textPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    Icons.Default.Edit,
                    contentDescription = stringResource(R.string.settings_edit_name_desc),
                    modifier = Modifier.size(18.dp),
                    tint = DuckTheme.colors.textSecondary
                )
            }

            Text(
                text = uiState.user?.email ?: "",
                fontSize = 14.sp,
                color = DuckTheme.colors.textSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ── Motto ──
            val motto = uiState.user?.motto ?: ""
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = DuckOrange500.copy(alpha = 0.15f),
                modifier = Modifier.clickable { showMottoDialog = true }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = motto.ifBlank { stringResource(R.string.settings_motto_placeholder) },
                        fontSize = 14.sp,
                        fontWeight = if (motto.isNotBlank()) FontWeight.Medium else FontWeight.Normal,
                        fontStyle = if (motto.isBlank()) androidx.compose.ui.text.font.FontStyle.Italic
                                    else androidx.compose.ui.text.font.FontStyle.Normal,
                        color = if (motto.isNotBlank()) DuckTheme.colors.textPrimary
                                else DuckTheme.colors.textSecondary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = stringResource(R.string.settings_edit_motto_desc),
                        modifier = Modifier.size(14.dp),
                        tint = DuckTheme.colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Compact Settings List ──

            DrawerItemRow(
                icon = Icons.Default.Language,
                label = stringResource(R.string.settings_language),
                badge = uiState.currentLanguage.displayName,
                onClick = { showLanguageDialog = true }
            )

            DrawerItemRow(
                icon = if (uiState.currentTheme == AppTheme.DARK) Icons.Default.DarkMode
                       else Icons.Default.LightMode,
                label = stringResource(R.string.settings_dark_theme),
                action = {
                    Switch(
                        checked = uiState.currentTheme == AppTheme.DARK,
                        onCheckedChange = {
                            viewModel.setTheme(if (it) AppTheme.DARK else AppTheme.LIGHT)
                        },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = DuckTheme.colors.accent,
                            checkedThumbColor = DuckTheme.colors.textOnAccent
                        )
                    )
                }
            )

            DrawerItemRow(
                icon = Icons.Default.WorkspacePremium,
                label = stringResource(R.string.settings_plan),
                badge = uiState.currentTier.replaceFirstChar { it.uppercase() },
                onClick = { /* TODO: PlansScreen */ }
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                color = DuckTheme.colors.textSecondary.copy(alpha = 0.1f)
            )

            // ── Debug: Tier Switcher ──
            Text(
                text = stringResource(R.string.settings_debug),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = DuckTheme.colors.textSecondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 4.dp)
            )

            listOf("free", "premium").forEach { tier ->
                DrawerItemRow(
                    icon = if (tier == "premium") Icons.Default.Star else Icons.Default.WorkspacePremium,
                    label = tier.replaceFirstChar { it.uppercase() },
                    badge = if (uiState.currentTier == tier) stringResource(R.string.settings_active) else null,
                    onClick = { viewModel.setTier(tier) }
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                color = DuckTheme.colors.textSecondary.copy(alpha = 0.1f)
            )

            // ── Sign Out ──
            DrawerItemRow(
                icon = Icons.AutoMirrored.Filled.ExitToApp,
                label = stringResource(R.string.settings_logout),
                onClick = {
                    viewModel.signOut()
                    onLogout()
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── Footer ──
            Text(
                text = stringResource(R.string.settings_version),
                fontSize = 12.sp,
                color = DuckTheme.colors.textSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // ── Name Edit Dialog ──
    if (showNameDialog) {
        var nameInput by remember { mutableStateOf(uiState.user?.displayName ?: "") }
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            title = { Text(stringResource(R.string.settings_name_dialog_title)) },
            text = {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text(stringResource(R.string.settings_name_dialog_field)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateDisplayName(nameInput.trim())
                        showNameDialog = false
                    },
                    enabled = nameInput.isNotBlank()
                ) { Text(stringResource(R.string.common_save)) }
            },
            dismissButton = {
                TextButton(onClick = { showNameDialog = false }) { Text(stringResource(R.string.common_cancel)) }
            }
        )
    }

    // ── Motto Edit Dialog ──
    if (showMottoDialog) {
        var mottoInput by remember { mutableStateOf(uiState.user?.motto ?: "") }
        AlertDialog(
            onDismissRequest = { showMottoDialog = false },
            title = { Text(stringResource(R.string.settings_motto_dialog_title)) },
            text = {
                Column {
                    OutlinedTextField(
                        value = mottoInput,
                        onValueChange = { if (it.length <= 50) mottoInput = it },
                        label = { Text(stringResource(R.string.settings_motto_dialog_field)) },
                        placeholder = { Text(stringResource(R.string.settings_motto_dialog_placeholder)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.settings_motto_counter, mottoInput.length),
                        fontSize = 12.sp,
                        color = DuckTheme.colors.textSecondary,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateMotto(mottoInput.trim())
                        showMottoDialog = false
                    }
                ) { Text(stringResource(R.string.common_save)) }
            },
            dismissButton = {
                TextButton(onClick = { showMottoDialog = false }) { Text(stringResource(R.string.common_cancel)) }
            }
        )
    }

    // ── Language Dialog ──
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.settings_language_dialog_title)) },
            text = {
                Column {
                    AppLanguage.entries.forEach { lang ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setLanguage(lang)
                                    showLanguageDialog = false
                                }
                                .padding(vertical = 6.dp)
                        ) {
                            RadioButton(
                                selected = uiState.currentLanguage == lang,
                                onClick = {
                                    viewModel.setLanguage(lang)
                                    showLanguageDialog = false
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = DuckTheme.colors.accent
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = lang.displayName,
                                fontSize = 16.sp,
                                color = DuckTheme.colors.textPrimary
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showLanguageDialog = false }) { Text(stringResource(R.string.common_cancel)) }
            }
        )
    }
}

@Composable
private fun DrawerItemRow(
    icon: ImageVector,
    label: String,
    badge: String? = null,
    onClick: (() -> Unit)? = null,
    action: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = DuckTheme.colors.textPrimary.copy(alpha = 0.7f),
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(24.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = DuckTheme.colors.textPrimary,
            modifier = Modifier.weight(1f)
        )

        if (action != null) {
            action()
        } else if (badge != null) {
            Surface(
                color = DuckTheme.colors.accent,
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = badge,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = DuckTheme.colors.textPrimary
                )
            }
        }
    }
}
