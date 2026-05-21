package com.whereduck.app.ui.main

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.whereduck.app.data.model.Alert
import com.whereduck.app.data.model.StarnazzoLevel
import com.whereduck.app.ui.history.HistoryViewModel
import com.whereduck.app.ui.theme.DuckTheme
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun HistoryTab(
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = all, 1 = sent, 2 = received

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
        uiState.sentAlerts.isEmpty() && uiState.receivedAlerts.isEmpty() -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.History,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = DuckTheme.colors.textSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Nessuno starnazzo ancora",
                    fontSize = 18.sp,
                    color = DuckTheme.colors.textPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "La cronologia dei tuoi starnazzi apparira' qui",
                    fontSize = 14.sp,
                    color = DuckTheme.colors.textSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
        else -> {
            val allAlerts = when (selectedTab) {
                1 -> uiState.sentAlerts
                2 -> uiState.receivedAlerts
                else -> (uiState.sentAlerts + uiState.receivedAlerts)
                    .sortedByDescending { it.createdAt }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            label = { Text("Tutti") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DuckTheme.colors.accent,
                                selectedLabelColor = DuckTheme.colors.textOnAccent
                            )
                        )
                        FilterChip(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            label = { Text("Inviati (${uiState.sentCount})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DuckTheme.colors.accent,
                                selectedLabelColor = DuckTheme.colors.textOnAccent
                            )
                        )
                        FilterChip(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            label = { Text("Ricevuti (${uiState.receivedCount})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DuckTheme.colors.accent,
                                selectedLabelColor = DuckTheme.colors.textOnAccent
                            )
                        )
                    }
                }

                items(allAlerts, key = { it.id }) { alert ->
                    val isSent = alert.fromUserId != "" &&
                        alert.toDisplayName.isNotBlank()
                    AlertHistoryCard(alert = alert, isSentByMe = uiState.sentAlerts.any { it.id == alert.id })
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun AlertHistoryCard(alert: Alert, isSentByMe: Boolean) {
    val level = StarnazzoLevel.fromKey(alert.starnazzoLevel)
    val dateFormat = remember { SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()) }
    val timeText = alert.createdAt?.toDate()?.let { dateFormat.format(it) } ?: ""

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
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
            // Emoji
            Text(
                text = level.emoji,
                fontSize = 32.sp
            )
            Spacer(modifier = Modifier.width(12.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isSentByMe) Icons.AutoMirrored.Filled.CallMade else Icons.AutoMirrored.Filled.CallReceived,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = if (isSentByMe) DuckTheme.colors.accent else DuckTheme.colors.textSecondary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isSentByMe) alert.toDisplayName.ifBlank { "???" }
                               else alert.fromDisplayName.ifBlank { "???" },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DuckTheme.colors.textPrimary
                    )
                    if (alert.isRevenge) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "REVENGE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = DuckTheme.colors.accent
                        )
                    }
                }
                Row {
                    Text(
                        text = "${level.animalName} · ${level.displayName}",
                        fontSize = 12.sp,
                        color = DuckTheme.colors.textSecondary
                    )
                    alert.response?.let { resp ->
                        val responseLabel = when (resp) {
                            "ok" -> " · OK!"
                            "muto" -> " · Mutato"
                            "revenge" -> " · Revenge"
                            "dismissed" -> " · Chiuso"
                            else -> ""
                        }
                        Text(
                            text = responseLabel,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = DuckTheme.colors.textSecondary
                        )
                    }
                }
            }

            // Time + status
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = timeText,
                    fontSize = 11.sp,
                    color = DuckTheme.colors.textSecondary
                )
                val statusText = when (alert.status) {
                    "delivered" -> "Consegnato"
                    "failed" -> "Fallito"
                    "sending" -> "Invio..."
                    else -> ""
                }
                if (statusText.isNotBlank()) {
                    Text(
                        text = statusText,
                        fontSize = 10.sp,
                        color = if (alert.status == "failed") DuckTheme.colors.accent
                               else DuckTheme.colors.textSecondary
                    )
                }
            }
        }
    }
}
