package com.whereduck.app.ui.main

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.whereduck.app.data.model.Contact
import com.whereduck.app.data.model.Group
import com.whereduck.app.ui.home.HomeViewModel
import com.whereduck.app.ui.theme.DuckTheme

@Composable
fun ContactsTab(
    onNavigateToContactDetail: (String) -> Unit,
    onNavigateToGroupDetail: (String) -> Unit,
    onNavigateToInvites: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showInviteDialog by remember { mutableStateOf(false) }

    when {
        uiState.isLoading -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = DuckTheme.colors.accent)
            }
        }
        else -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Pending invites banner
                if (uiState.pendingInviteCount > 0) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToInvites() },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = DuckTheme.colors.accentLight
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BadgedBox(
                                    badge = {
                                        Badge { Text("${uiState.pendingInviteCount}") }
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Notifications,
                                        contentDescription = null,
                                        tint = DuckTheme.colors.accentDark
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = "Hai ${uiState.pendingInviteCount} invit${if (uiState.pendingInviteCount == 1) "o" else "i"} in sospeso",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DuckTheme.colors.textPrimary
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(
                                    Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = DuckTheme.colors.textSecondary
                                )
                            }
                        }
                    }
                }

                // Contacts section
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Contatti",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = DuckTheme.colors.textPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { showInviteDialog = true }) {
                            Icon(
                                Icons.Default.PersonAdd,
                                contentDescription = "Aggiungi contatto",
                                tint = DuckTheme.colors.accent
                            )
                        }
                    }
                }

                if (uiState.contacts.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = DuckTheme.colors.cardBackground
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = DuckTheme.colors.textSecondary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Nessun contatto ancora",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = DuckTheme.colors.textPrimary,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "Invita qualcuno con la sua email",
                                    fontSize = 13.sp,
                                    color = DuckTheme.colors.textSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(uiState.contacts) { contact ->
                        ContactListCard(
                            contact = contact,
                            onClick = { onNavigateToContactDetail(contact.id) }
                        )
                    }
                }

                // Groups section
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Gruppi",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DuckTheme.colors.textPrimary
                    )
                }

                if (uiState.groups.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = DuckTheme.colors.cardBackground
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Groups,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = DuckTheme.colors.textSecondary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Nessun gruppo ancora",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = DuckTheme.colors.textPrimary,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "Usa il + per creare il tuo primo gruppo",
                                    fontSize = 13.sp,
                                    color = DuckTheme.colors.textSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(uiState.groups) { group ->
                        GroupCard(
                            group = group,
                            onClick = { onNavigateToGroupDetail(group.id) }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }

    if (showInviteDialog) {
        InviteContactDialog(
            onDismiss = { showInviteDialog = false },
            onInvite = { email ->
                viewModel.sendContactInvite(email)
                showInviteDialog = false
            }
        )
    }
}

@Composable
private fun ContactListCard(contact: Contact, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DuckTheme.colors.cardBackground
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (contact.photoUrl.isNotBlank()) {
                AsyncImage(
                    model = contact.photoUrl,
                    contentDescription = contact.displayName,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = DuckTheme.colors.accent
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.displayName.ifBlank { contact.email },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DuckTheme.colors.textPrimary
                )
                if (contact.displayName.isNotBlank() && contact.email.isNotBlank()) {
                    Text(
                        text = contact.email,
                        fontSize = 13.sp,
                        color = DuckTheme.colors.textSecondary
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Apri",
                tint = DuckTheme.colors.textSecondary
            )
        }
    }
}

@Composable
private fun GroupCard(
    group: Group,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DuckTheme.colors.cardBackground
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Groups,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = DuckTheme.colors.accent
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = group.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = DuckTheme.colors.textPrimary,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Apri",
                tint = DuckTheme.colors.textSecondary
            )
        }
    }
}

@Composable
private fun InviteContactDialog(
    onDismiss: () -> Unit,
    onInvite: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Aggiungi contatto") },
        text = {
            Column {
                Text(
                    text = "Inserisci l'email del contatto da invitare",
                    fontSize = 14.sp,
                    color = DuckTheme.colors.textSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it.trim() },
                    label = { Text("Email") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (email.isNotBlank()) onInvite(email)
                        }
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        if (email.isNotBlank()) {
                            IconButton(onClick = { email = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Cancella")
                            }
                        }
                    }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onInvite(email) },
                enabled = email.isNotBlank() && email.contains("@")
            ) {
                Text("Invita")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annulla")
            }
        }
    )
}
