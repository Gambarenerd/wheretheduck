package com.whereduck.app.ui.group

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.whereduck.app.ui.theme.DuckTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupManagementScreen(
    groupId: String,
    onNavigateBack: () -> Unit,
    onGroupDeleted: () -> Unit,
    viewModel: GroupManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAddContactDialog by remember { mutableStateOf(false) }
    var showRemoveContactId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.groupDeleted.collect { onGroupDeleted() }
    }

    Scaffold(
        containerColor = DuckTheme.colors.sectionDashboard,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = uiState.groupName.ifEmpty { "Gestione" },
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
                    IconButton(onClick = { showRenameDialog = true }) {
                        Icon(Icons.Default.Edit, "Rinomina")
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Contacts in group
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Contatti nel gruppo",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { showAddContactDialog = true }) {
                        Icon(Icons.Default.Add, "Aggiungi contatto")
                    }
                }
            }

            if (uiState.groupContacts.isEmpty()) {
                item {
                    Text(
                        text = "Nessun contatto nel gruppo",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                items(uiState.groupContacts) { contact ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = contact.displayName,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = contact.email,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                            IconButton(onClick = { showRemoveContactId = contact.id }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Rimuovi",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            // Delete group button
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    enabled = !uiState.isDeleting
                ) {
                    Icon(Icons.Default.Delete, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Elimina gruppo")
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    // Rename dialog
    if (showRenameDialog) {
        var newName by remember { mutableStateOf(uiState.groupName) }
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rinomina gruppo") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Nome gruppo") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newName.isNotBlank()) {
                        viewModel.renameGroup(newName.trim())
                        showRenameDialog = false
                    }
                }) { Text("Salva") }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) { Text("Annulla") }
            }
        )
    }

    // Delete dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Elimina gruppo") },
            text = { Text("Sei sicuro di voler eliminare \"${uiState.groupName}\"? I contatti non verranno cancellati.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteGroup()
                    showDeleteDialog = false
                }) { Text("Elimina", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Annulla") }
            }
        )
    }

    // Remove contact from group dialog
    showRemoveContactId?.let { contactId ->
        val contact = uiState.groupContacts.find { it.id == contactId }
        AlertDialog(
            onDismissRequest = { showRemoveContactId = null },
            title = { Text("Rimuovi dal gruppo") },
            text = { Text("Rimuovere ${contact?.displayName ?: ""} da questo gruppo? Resterà nei tuoi contatti.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.removeContactFromGroup(contactId)
                    showRemoveContactId = null
                }) { Text("Rimuovi", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveContactId = null }) { Text("Annulla") }
            }
        )
    }

    // Add contacts dialog
    if (showAddContactDialog) {
        val availableContacts = uiState.allContacts.filter { it.id !in uiState.groupContactIds }
        AlertDialog(
            onDismissRequest = { showAddContactDialog = false },
            title = { Text("Aggiungi contatti") },
            text = {
                if (availableContacts.isEmpty()) {
                    Text("Tutti i tuoi contatti sono già nel gruppo")
                } else {
                    Column {
                        availableContacts.forEach { contact ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.addContactToGroup(contact.id)
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(contact.displayName, fontWeight = FontWeight.Medium)
                                    Text(
                                        contact.email,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Aggiungi",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAddContactDialog = false }) { Text("Chiudi") }
            }
        )
    }
}
