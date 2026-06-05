package com.whereduck.app.ui.groupdetail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.whereduck.app.ui.components.ContactCard
import com.whereduck.app.ui.theme.DuckTheme
import com.whereduck.app.ui.components.StarnazzoLevelSelector
import com.whereduck.app.ui.theme.StarnazzoHeavy
import com.whereduck.app.ui.theme.StarnazzoMedium
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToGroupManagement: (String) -> Unit,
    onNavigateToStarnazzoCall: (alertId: String, toName: String, level: String) -> Unit,
    onNavigateToContactDetail: (String) -> Unit = {},
    viewModel: GroupDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Navigate to call screen when starnazzo is sent
    LaunchedEffect(Unit) {
        viewModel.starnazzoSent.collect { event ->
            onNavigateToStarnazzoCall(event.alertId, event.toName, event.level)
        }
    }

    // Auto-dismiss snackbar
    LaunchedEffect(uiState.lastSendResult) {
        if (uiState.lastSendResult != null) {
            delay(3000)
            viewModel.clearSendResult()
        }
    }

    Scaffold(
        containerColor = DuckTheme.colors.sectionDashboard,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = uiState.groupName.ifEmpty { "Gruppo" },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Indietro")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        onNavigateToGroupManagement(viewModel.groupId)
                    }) {
                        Icon(Icons.Default.Settings, "Gestione gruppo")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.contacts.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Sei solo nel gruppo!",
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Aggiungi contatti al gruppo per iniziare a starnazzare.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            onNavigateToGroupManagement(viewModel.groupId)
                        }) {
                            Text("Aggiungi contatti")
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Contacts header
                        item {
                            Text(
                                text = "Contatti",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Contact list with starnazzo button
                        items(uiState.contacts) { contact ->
                            ContactCard(
                                contact = contact,
                                onClick = { onNavigateToContactDetail(contact.id) }
                            ) {
                                val isSending = uiState.sendingToUserId == contact.id
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(DuckTheme.colors.highlight)
                                        .clickable(
                                            enabled = !isSending && uiState.sendingToUserId == null && !uiState.isBroadcasting
                                        ) { viewModel.sendStarnazzo(contact.id) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSending) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp,
                                            color = DuckTheme.colors.sectionTitle
                                        )
                                    } else {
                                        Icon(
                                            Icons.Default.Campaign,
                                            contentDescription = "Starnazza",
                                            modifier = Modifier.size(20.dp),
                                            tint = DuckTheme.colors.sectionTitle
                                        )
                                    }
                                }
                            }
                        }

                        // Level selector
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Scegli il livello di starnazzo",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            StarnazzoLevelSelector(
                                selectedLevel = uiState.selectedLevel,
                                onLevelSelected = { viewModel.selectLevel(it) }
                            )
                        }

                        // Broadcast button
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.sendBroadcast() },
                                enabled = !uiState.isBroadcasting && uiState.sendingToUserId == null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = StarnazzoHeavy
                                )
                            ) {
                                if (uiState.isBroadcasting) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                } else {
                                    Icon(Icons.Default.Campaign, null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "QUACK TUTTI!",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Bottom spacing
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }
            }

            // Snackbar for send results
            AnimatedVisibility(
                visible = uiState.lastSendResult != null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Snackbar {
                    Text(uiState.lastSendResult ?: "")
                }
            }
        }
    }
}
