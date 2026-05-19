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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.whereduck.app.data.model.Contact
import com.whereduck.app.data.model.StarnazzoLevel
import com.whereduck.app.ui.history.HistoryViewModel
import com.whereduck.app.ui.home.HomeViewModel
import com.whereduck.app.ui.theme.DuckTheme

@Composable
fun DashboardTab(
    onSendStarnazzo: ((String) -> Unit)? = null,
    historyViewModel: HistoryViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val historyState by historyViewModel.uiState.collectAsState()
    val homeState by homeViewModel.uiState.collectAsState()

    val weekSent = remember(historyState.sentAlerts) {
        val weekAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
        historyState.sentAlerts.count { alert ->
            (alert.createdAt?.toDate()?.time ?: 0L) > weekAgo
        }
    }
    val weekReceived = remember(historyState.receivedAlerts) {
        val weekAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
        historyState.receivedAlerts.count { alert ->
            (alert.createdAt?.toDate()?.time ?: 0L) > weekAgo
        }
    }

    // Most starnazzed contact (sent to most)
    val topContact = remember(historyState.sentAlerts) {
        historyState.sentAlerts
            .groupBy { it.toUserId }
            .maxByOrNull { it.value.size }
            ?.let { (userId, alerts) ->
                val name = alerts.first().toDisplayName.ifBlank { "???" }
                name to alerts.size
            }
    }

    // Most used level
    val topLevel = remember(historyState.sentAlerts) {
        historyState.sentAlerts
            .groupBy { it.starnazzoLevel }
            .maxByOrNull { it.value.size }
            ?.let { (level, alerts) ->
                StarnazzoLevel.fromKey(level) to alerts.size
            }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Stats card
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Le tue stats",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DuckTheme.colors.textPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
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
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Questa settimana",
                        fontSize = 14.sp,
                        color = DuckTheme.colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$weekSent inviati · $weekReceived ricevuti",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = DuckTheme.colors.textPrimary
                    )

                    if (topLevel != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = topLevel.first.emoji,
                                fontSize = 20.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Livello preferito: ${topLevel.first.displayName}",
                                fontSize = 13.sp,
                                color = DuckTheme.colors.textSecondary
                            )
                        }
                    }

                    if (topContact != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Piu' starnazzato: ${topContact.first} (${topContact.second}x)",
                            fontSize = 13.sp,
                            color = DuckTheme.colors.textSecondary
                        )
                    }

                    // Total stats
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Totale: ${historyState.sentCount} inviati · ${historyState.receivedCount} ricevuti",
                        fontSize = 12.sp,
                        color = DuckTheme.colors.textSecondary
                    )
                }
            }
        }

        // Quick starnazzo section
        if (homeState.contacts.isNotEmpty()) {
            item {
                Text(
                    text = "Starnazzo rapido",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DuckTheme.colors.textPrimary
                )
            }

            items(homeState.contacts.take(5)) { contact ->
                QuickContactCard(
                    contact = contact,
                    onClick = { onSendStarnazzo?.invoke(contact.id) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun QuickContactCard(
    contact: Contact,
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
                .padding(14.dp),
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
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = DuckTheme.colors.accent
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = contact.displayName.ifBlank { contact.email },
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = DuckTheme.colors.textPrimary,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "STARNAZZA",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = DuckTheme.colors.accent
            )
        }
    }
}
