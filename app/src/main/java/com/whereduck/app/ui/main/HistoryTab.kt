package com.whereduck.app.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.whereduck.app.data.model.Alert
import com.whereduck.app.data.model.StarnazzoLevel
import com.whereduck.app.ui.history.HistoryViewModel
import com.whereduck.app.ui.theme.DuckTheme
import com.whereduck.app.ui.theme.StarnazzoHeavy
import com.whereduck.app.ui.theme.StarnazzoLight
import com.whereduck.app.ui.theme.StarnazzoMedium
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private data class GroupedAlert(
    val personName: String,
    val personId: String,
    val personPhotoUrl: String,
    val level: StarnazzoLevel,
    val isSentByMe: Boolean,
    val isRevenge: Boolean,
    val count: Int,
    val lastResponse: String?,
    val lastTime: Date?
)

private data class DaySection(
    val label: String,
    val groups: List<GroupedAlert>
)

@Composable
fun HistoryTab(
    onNavigateToContactDetail: (String) -> Unit = {},
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

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
            val sentIds = remember(uiState.sentAlerts) {
                uiState.sentAlerts.map { it.id }.toSet()
            }

            val filteredAlerts = when (selectedTab) {
                1 -> uiState.sentAlerts
                2 -> uiState.receivedAlerts
                else -> (uiState.sentAlerts + uiState.receivedAlerts)
                    .sortedByDescending { it.createdAt }
            }

            val daySections = remember(filteredAlerts, uiState.contactPhotos) {
                buildDaySections(filteredAlerts, sentIds, uiState.contactPhotos)
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
                            shape = CircleShape,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DuckTheme.colors.accent,
                                selectedLabelColor = DuckTheme.colors.textOnAccent
                            )
                        )
                        FilterChip(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            label = { Text("Inviati (${uiState.sentCount})") },
                            shape = CircleShape,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DuckTheme.colors.accent,
                                selectedLabelColor = DuckTheme.colors.textOnAccent
                            )
                        )
                        FilterChip(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            label = { Text("Ricevuti (${uiState.receivedCount})") },
                            shape = CircleShape,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DuckTheme.colors.accent,
                                selectedLabelColor = DuckTheme.colors.textOnAccent
                            )
                        )
                    }
                }

                daySections.forEach { section ->
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = section.label,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = DuckTheme.colors.sectionTitle
                        )
                    }

                    items(section.groups, key = { "${it.personId}_${it.level}_${it.isSentByMe}_${it.isRevenge}_${section.label}" }) { group ->
                        GroupedAlertCard(
                            group = group,
                            onClick = { onNavigateToContactDetail(group.personId) }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

private fun buildDaySections(
    alerts: List<Alert>,
    sentIds: Set<String>,
    contactPhotos: Map<String, String>
): List<DaySection> {
    val dayFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val today = dayFmt.format(Date())
    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        .let { dayFmt.format(it.time) }
    val displayFmt = SimpleDateFormat("EEEE d MMMM", Locale.ITALIAN)

    // Group by day
    val byDay = alerts
        .filter { it.createdAt != null }
        .groupBy { dayFmt.format(it.createdAt!!.toDate()) }
        .toSortedMap(compareByDescending { it })

    return byDay.map { (dayKey, dayAlerts) ->
        val label = when (dayKey) {
            today -> "Oggi"
            yesterday -> "Ieri"
            else -> displayFmt.format(dayFmt.parse(dayKey)!!).replaceFirstChar { it.uppercase() }
        }

        // Group alerts within the day by (person, level, direction, revenge)
        val grouped = dayAlerts.groupBy { alert ->
            val isSent = alert.id in sentIds
            val personId = if (isSent) alert.toUserId else alert.fromUserId
            val level = alert.starnazzoLevel
            "$personId|$level|$isSent|${alert.isRevenge}"
        }.map { (_, groupAlerts) ->
            val first = groupAlerts.first()
            val isSent = first.id in sentIds
            val personId = if (isSent) first.toUserId else first.fromUserId
            val personName = if (isSent) first.toDisplayName.ifBlank { "???" }
                             else first.fromDisplayName.ifBlank { "???" }
            val photoUrl = contactPhotos[personId] ?: ""
            val lastAlert = groupAlerts.maxByOrNull { it.createdAt?.toDate()?.time ?: 0L } ?: first

            GroupedAlert(
                personName = personName,
                personId = personId,
                personPhotoUrl = photoUrl,
                level = StarnazzoLevel.fromKey(first.starnazzoLevel),
                isSentByMe = isSent,
                isRevenge = first.isRevenge,
                count = groupAlerts.size,
                lastResponse = lastAlert.response,
                lastTime = lastAlert.createdAt?.toDate()
            )
        }.sortedByDescending { it.lastTime?.time ?: 0L }

        DaySection(label = label, groups = grouped)
    }
}

@Composable
private fun GroupedAlertCard(group: GroupedAlert, onClick: () -> Unit) {
    val timeFmt = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val timeText = group.lastTime?.let { timeFmt.format(it) } ?: ""

    val levelColor = when (group.level) {
        StarnazzoLevel.LIGHT -> StarnazzoLight
        StarnazzoLevel.MEDIUM -> StarnazzoMedium
        StarnazzoLevel.HEAVY -> StarnazzoHeavy
    }

    Card(
        onClick = onClick,
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
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with level emoji badge
            Box(modifier = Modifier.size(48.dp)) {
                if (group.personPhotoUrl.isNotBlank()) {
                    AsyncImage(
                        model = group.personPhotoUrl,
                        contentDescription = group.personName,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Surface(
                        modifier = Modifier.size(44.dp),
                        shape = CircleShape,
                        color = DuckTheme.colors.accentLight
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = DuckTheme.colors.accent
                            )
                        }
                    }
                }
                // Level emoji badge
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(levelColor)
                        .align(Alignment.BottomEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = group.level.emoji,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Info + time
            Column(modifier = Modifier.weight(1f)) {
                // Name row with response chip + time on the right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (group.isSentByMe) Icons.AutoMirrored.Filled.CallMade
                                      else Icons.AutoMirrored.Filled.CallReceived,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = if (group.isSentByMe) DuckTheme.colors.accent
                               else DuckTheme.colors.textSecondary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = group.personName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DuckTheme.colors.textPrimary
                    )
                    // Response chip right after name
                    group.lastResponse?.let { resp ->
                        val (chipText, chipColor) = when (resp) {
                            "ok" -> "OK!" to Color(0xFF4CAF50)
                            "muto" -> "Mutato" to DuckTheme.colors.negative
                            "revenge" -> "Revenge" to Color(0xFFFF9800)
                            "dismissed" -> "Chiuso" to DuckTheme.colors.textSecondary
                            else -> null to null
                        }
                        if (chipText != null && chipColor != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = CircleShape,
                                color = chipColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = chipText,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = chipColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    // Revenge badge after response
                    if (group.isRevenge) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFF9800).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "REVENGE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF9800),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = timeText,
                        fontSize = 11.sp,
                        color = DuckTheme.colors.textSecondary
                    )
                }

                // Description with count on the right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (group.isSentByMe) "Hai starnazzato ${group.personName}"
                               else "${group.personName} ti ha starnazzato",
                        fontSize = 12.sp,
                        color = DuckTheme.colors.textSecondary,
                        modifier = Modifier.weight(1f)
                    )
                    if (group.count > 1) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = CircleShape,
                            color = DuckTheme.colors.highlight
                        ) {
                            Text(
                                text = "x${group.count}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = DuckTheme.colors.sectionTitle,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
