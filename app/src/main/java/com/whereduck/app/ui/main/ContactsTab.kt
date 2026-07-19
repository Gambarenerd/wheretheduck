package com.whereduck.app.ui.main

import androidx.compose.foundation.background
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.Canvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.whereduck.app.R
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.whereduck.app.ui.components.CachedAsyncImage
import com.whereduck.app.data.model.Contact
import com.whereduck.app.data.model.Group
import com.whereduck.app.ui.history.HistoryViewModel
import com.whereduck.app.ui.home.HomeViewModel
import com.whereduck.app.ui.theme.DuckOrange500
import com.whereduck.app.ui.theme.DuckTheme
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ContactsTab(
    onNavigateToContactDetail: (String) -> Unit,
    onNavigateToGroupDetail: (String) -> Unit,
    onNavigateToInvites: () -> Unit,
    inviteTrigger: Int = 0,
    viewModel: HomeViewModel = hiltViewModel(),
    historyViewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val historyState by historyViewModel.uiState.collectAsState()
    var showInviteDialog by remember { mutableStateOf(false) }

    // Last starnazzo time per contact (sent to them)
    val lastStarnazzoMap = remember(historyState.sentAlerts) {
        val fmt = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
        historyState.sentAlerts
            .groupBy { it.toUserId }
            .mapValues { (_, alerts) ->
                val latest = alerts.maxByOrNull { it.createdAt?.toDate()?.time ?: 0L }
                latest?.createdAt?.toDate()?.let { fmt.format(it) } ?: ""
            }
    }

    // Sent/received counts per contact
    val sentCountMap = remember(historyState.sentAlerts) {
        historyState.sentAlerts.groupBy { it.toUserId }.mapValues { it.value.size }
    }
    val receivedCountMap = remember(historyState.receivedAlerts) {
        historyState.receivedAlerts.groupBy { it.fromUserId }.mapValues { it.value.size }
    }

    // Open invite dialog when FAB triggers it
    var lastHandledTrigger by remember { mutableStateOf(0) }
    LaunchedEffect(inviteTrigger) {
        if (inviteTrigger > lastHandledTrigger) {
            lastHandledTrigger = inviteTrigger
            showInviteDialog = true
        }
    }

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
                            shape = RoundedCornerShape(28.dp),
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
                                    text = if (uiState.pendingInviteCount == 1)
                                        stringResource(R.string.contacts_pending_one, uiState.pendingInviteCount)
                                    else
                                        stringResource(R.string.contacts_pending_many, uiState.pendingInviteCount),
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

                // Groups section
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.contacts_groups),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = DuckTheme.colors.sectionTitle
                    )
                }

                if (uiState.groups.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(28.dp),
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
                                    text = stringResource(R.string.contacts_no_groups),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = DuckTheme.colors.textPrimary,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = stringResource(R.string.contacts_no_groups_hint),
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
                            contacts = uiState.contacts,
                            onClick = { onNavigateToGroupDetail(group.id) }
                        )
                    }
                }

                // Contacts section
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.contacts_contacts),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = DuckTheme.colors.sectionTitle
                    )
                }

                if (uiState.contacts.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(28.dp),
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
                                    text = stringResource(R.string.contacts_no_contacts),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = DuckTheme.colors.textPrimary,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = stringResource(R.string.contacts_no_contacts_hint),
                                    fontSize = 13.sp,
                                    color = DuckTheme.colors.textSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    // VIP contacts first, then normal
                    val vipIds = uiState.vipContactIds.toSet()
                    val sortedContacts = uiState.contacts.sortedByDescending { it.id in vipIds }
                    items(sortedContacts) { contact ->
                        val isVip = contact.id in vipIds
                        ContactListCard(
                            contact = contact,
                            isVip = isVip,
                            lastStarnazzo = if (isVip) lastStarnazzoMap[contact.id] else null,
                            sentCount = sentCountMap[contact.id] ?: 0,
                            receivedCount = receivedCountMap[contact.id] ?: 0,
                            onClick = { onNavigateToContactDetail(contact.id) }
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
private fun ContactListCard(
    contact: Contact,
    isVip: Boolean = false,
    lastStarnazzo: String? = null,
    sentCount: Int = 0,
    receivedCount: Int = 0,
    onClick: () -> Unit
) {
    val avatarSize = if (isVip) 66.dp else 44.dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = DuckTheme.colors.cardBackground
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        if (isVip) {
            // VIP layout — taller with motto pill and last starnazzo
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (contact.photoUrl.isNotBlank()) {
                        CachedAsyncImage(
                            model = contact.photoUrl,
                            contentDescription = contact.displayName,
                            modifier = Modifier
                                .size(avatarSize)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(avatarSize)
                                .clip(CircleShape)
                                .background(DuckTheme.colors.accentLight),
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
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = contact.displayName.ifBlank { contact.email },
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DuckTheme.colors.textPrimary
                        )
                        if (contact.motto.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(DuckOrange500.copy(alpha = 0.15f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = contact.motto,
                                    fontSize = 13.sp,
                                    color = DuckTheme.colors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        } else if (contact.displayName.isNotBlank() && contact.email.isNotBlank()) {
                            Text(
                                text = contact.email,
                                fontSize = 13.sp,
                                color = DuckTheme.colors.textSecondary
                            )
                        }
                    }
                }
            }
        } else {
            // Normal contact layout
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (contact.photoUrl.isNotBlank()) {
                    CachedAsyncImage(
                        model = contact.photoUrl,
                        contentDescription = contact.displayName,
                        modifier = Modifier
                            .size(avatarSize)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(avatarSize)
                            .clip(CircleShape)
                            .background(DuckTheme.colors.accentLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(26.dp),
                            tint = DuckTheme.colors.accent
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
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
            }
        }
    }
}

@Composable
private fun GroupCard(
    group: Group,
    contacts: List<Contact>,
    onClick: () -> Unit
) {
    val memberContacts = group.contactIds.mapNotNull { id ->
        contacts.find { it.id == id }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = DuckTheme.colors.cardBackground
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Group photo on the left
            if (group.photoUrl.isNotBlank()) {
                CachedAsyncImage(
                    model = group.photoUrl,
                    contentDescription = group.name,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(DuckTheme.colors.accentLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Groups,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = DuckTheme.colors.accent
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Group name
            Text(
                text = group.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = DuckTheme.colors.textPrimary,
                modifier = Modifier.weight(1f)
            )

            // Member avatars on the right
            if (memberContacts.isNotEmpty()) {
                val avatarSize = 32.dp
                val overlap = 20.dp
                val visibleMembers = memberContacts.take(3)
                val extraCount = memberContacts.size - visibleMembers.size
                val itemCount = visibleMembers.size + if (extraCount > 0) 1 else 0
                val totalWidth = avatarSize + (overlap * (itemCount - 1).coerceAtLeast(0))

                Box(
                    modifier = Modifier
                        .width(totalWidth)
                        .height(avatarSize)
                ) {
                    visibleMembers.forEachIndexed { index, contact ->
                        val offsetX = overlap * index
                        Box(
                            modifier = Modifier
                                .padding(start = offsetX)
                                .size(avatarSize)
                                .clip(CircleShape)
                                .background(DuckTheme.colors.cardBackground)
                                .padding(1.5.dp)
                                .clip(CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (contact.photoUrl.isNotBlank()) {
                                CachedAsyncImage(
                                    model = contact.photoUrl,
                                    contentDescription = contact.displayName,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(DuckTheme.colors.accentLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = DuckTheme.colors.accent
                                    )
                                }
                            }
                        }
                    }
                    if (extraCount > 0) {
                        val offsetX = overlap * visibleMembers.size
                        Box(
                            modifier = Modifier
                                .padding(start = offsetX)
                                .size(avatarSize)
                                .clip(CircleShape)
                                .background(DuckTheme.colors.cardBackground)
                                .padding(1.5.dp)
                                .clip(CircleShape)
                                .background(DuckTheme.colors.accent),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+$extraCount",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = DuckTheme.colors.textOnAccent
                            )
                        }
                    }
                }
            }
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
        title = { Text(stringResource(R.string.contacts_invite_title)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.contacts_invite_body),
                    fontSize = 14.sp,
                    color = DuckTheme.colors.textSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it.trim() },
                    label = { Text(stringResource(R.string.contacts_invite_field)) },
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
                                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.contacts_invite_clear_desc))
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
                Text(stringResource(R.string.contacts_invite_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}
