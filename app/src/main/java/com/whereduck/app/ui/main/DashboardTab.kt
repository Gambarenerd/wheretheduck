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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.whereduck.app.data.model.Contact
import com.whereduck.app.data.model.StarnazzoLevel
import com.whereduck.app.ui.history.HistoryViewModel
import com.whereduck.app.ui.home.HomeViewModel
import com.whereduck.app.ui.theme.DuckTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun DashboardTab(
    onSendStarnazzo: ((String) -> Unit)? = null,
    historyViewModel: HistoryViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val historyState by historyViewModel.uiState.collectAsState()
    val homeState by homeViewModel.uiState.collectAsState()
    var showVipPicker by remember { mutableStateOf(false) }

    val weekAgo = remember { System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L }

    val weekSent = remember(historyState.sentAlerts) {
        historyState.sentAlerts.count { alert ->
            (alert.createdAt?.toDate()?.time ?: 0L) > weekAgo
        }
    }
    val weekReceived = remember(historyState.receivedAlerts) {
        historyState.receivedAlerts.count { alert ->
            (alert.createdAt?.toDate()?.time ?: 0L) > weekAgo
        }
    }

    // Group by day for chart
    val keyFmt = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val sentByDay = remember(historyState.sentAlerts) {
        historyState.sentAlerts
            .filter { (it.createdAt?.toDate()?.time ?: 0L) > weekAgo }
            .groupBy { keyFmt.format(it.createdAt!!.toDate()) }
            .mapValues { it.value.size }
    }
    val receivedByDay = remember(historyState.receivedAlerts) {
        historyState.receivedAlerts
            .filter { (it.createdAt?.toDate()?.time ?: 0L) > weekAgo }
            .groupBy { keyFmt.format(it.createdAt!!.toDate()) }
            .mapValues { it.value.size }
    }

    val topContact = remember(historyState.sentAlerts) {
        historyState.sentAlerts
            .groupBy { it.toUserId }
            .maxByOrNull { it.value.size }
            ?.let { (_, alerts) ->
                val name = alerts.first().toDisplayName.ifBlank { "???" }
                name to alerts.size
            }
    }

    val topLevel = remember(historyState.sentAlerts) {
        historyState.sentAlerts
            .groupBy { it.starnazzoLevel }
            .maxByOrNull { it.value.size }
            ?.let { (level, alerts) ->
                StarnazzoLevel.fromKey(level) to alerts.size
            }
    }

    // Resolve VIP contacts
    val vipContacts = remember(homeState.vipContactIds, homeState.contacts) {
        homeState.vipContactIds.mapNotNull { id ->
            homeState.contacts.find { it.id == id }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Victims section
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = null,
                    tint = DuckTheme.colors.vipHeart,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Vittime preferite",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DuckTheme.colors.sectionTitle
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                vipContacts.forEach { contact ->
                    VipAvatar(
                        contact = contact,
                        onClick = { onSendStarnazzo?.invoke(contact.id) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // Add button if < 4
                if (vipContacts.size < 4) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showVipPicker = true },
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(DuckTheme.colors.vipAddCircle),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.PersonAdd,
                                    contentDescription = "Aggiungi vittima",
                                    tint = DuckTheme.colors.vipAddIcon,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                            // Match VipAvatar height (spacer + text line)
                            Spacer(modifier = Modifier.height(18.dp))
                        }
                    }
                }
                // Fill remaining empty spaces to keep alignment
                val emptySlots = 4 - vipContacts.size - 1
                repeat(emptySlots.coerceAtLeast(0)) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        // Stats card with chart
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Danni della settimana",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DuckTheme.colors.sectionTitle
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
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
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    WeeklyBarChart(
                        sentByDay = sentByDay,
                        receivedByDay = receivedByDay,
                        totalSent = weekSent,
                        totalReceived = weekReceived,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Stats details card
        if (topLevel != null || topContact != null) {
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
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        if (topLevel != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = topLevel.first.emoji,
                                    fontSize = 22.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Arma preferita: ${topLevel.first.displayName}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = DuckTheme.colors.textPrimary
                                )
                            }
                        }

                        if (topContact != null) {
                            if (topLevel != null) Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Vittima piu' tormentata: ${topContact.first} (${topContact.second}x)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = DuckTheme.colors.textPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Totale: ${historyState.sentCount} inflitti · ${historyState.receivedCount} subiti",
                            fontSize = 12.sp,
                            color = DuckTheme.colors.textSecondary
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }

    // VIP picker dialog
    if (showVipPicker) {
        val availableContacts = homeState.contacts.filter { c ->
            c.id !in homeState.vipContactIds
        }
        VipPickerDialog(
            contacts = availableContacts,
            onPick = { contactId ->
                homeViewModel.addVip(contactId)
                showVipPicker = false
            },
            onDismiss = { showVipPicker = false }
        )
    }
}

@Composable
private fun VipAvatar(
    contact: Contact,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.clickable(onClick = onClick),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (contact.photoUrl.isNotBlank()) {
                AsyncImage(
                    model = contact.photoUrl,
                    contentDescription = contact.displayName,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(DuckTheme.colors.accentLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = DuckTheme.colors.accent
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = contact.displayName.ifBlank { contact.email }.split(" ").first(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = DuckTheme.colors.textPrimary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun VipPickerDialog(
    contacts: List<Contact>,
    onPick: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Scegli una vittima") },
        text = {
            if (contacts.isEmpty()) {
                Text(
                    text = "Nessun contatto disponibile",
                    color = DuckTheme.colors.textSecondary
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    contacts.forEach { contact ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onPick(contact.id) }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (contact.photoUrl.isNotBlank()) {
                                AsyncImage(
                                    model = contact.photoUrl,
                                    contentDescription = contact.displayName,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(DuckTheme.colors.accentLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(22.dp),
                                        tint = DuckTheme.colors.accent
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = contact.displayName.ifBlank { contact.email },
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = DuckTheme.colors.textPrimary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annulla")
            }
        }
    )
}
