package com.whereduck.app.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.whereduck.app.data.model.Contact
import androidx.compose.ui.platform.LocalContext
import com.whereduck.app.data.model.AnimalRegistry
import com.whereduck.app.ui.components.AnimalEmoji
import com.whereduck.app.data.model.StarnazzoLevel
import com.whereduck.app.ui.theme.DuckGrey300
import com.whereduck.app.ui.theme.DuckOrange500
import com.whereduck.app.ui.theme.StarnazzoHeavy
import com.whereduck.app.ui.theme.StarnazzoLight
import com.whereduck.app.ui.theme.StarnazzoLightTenue
import com.whereduck.app.ui.theme.StarnazzoMedium
import com.whereduck.app.ui.theme.StarnazzoMediumTenue
import com.whereduck.app.ui.theme.StarnazzoHeavyTenue
import com.whereduck.app.ui.history.HistoryViewModel
import com.whereduck.app.ui.home.HomeViewModel
import com.whereduck.app.ui.theme.DuckTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private data class EnemyStat(
    val userId: String,
    val name: String,
    val photoUrl: String,
    val count: Int
)

@Composable
fun DashboardTab(
    onSendStarnazzo: ((String) -> Unit)? = null,
    onNavigateToContact: ((String) -> Unit)? = null,
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

    val topLevel = remember(historyState.sentAlerts) {
        historyState.sentAlerts
            .groupBy { it.starnazzoLevel }
            .maxByOrNull { it.value.size }
            ?.let { (level, alerts) ->
                StarnazzoLevel.fromKey(level) to alerts.size
            }
    }

    // "Guerra aperta" — most interactions (sent + received) this week
    val weekWarContact = remember(historyState.sentAlerts, historyState.receivedAlerts, homeState.contacts) {
        val sentCounts = historyState.sentAlerts
            .filter { (it.createdAt?.toDate()?.time ?: 0L) > weekAgo }
            .groupBy { it.toUserId }
            .mapValues { it.value.size }
        val receivedCounts = historyState.receivedAlerts
            .filter { (it.createdAt?.toDate()?.time ?: 0L) > weekAgo }
            .groupBy { it.fromUserId }
            .mapValues { it.value.size }
        val allIds = sentCounts.keys + receivedCounts.keys
        allIds.maxByOrNull { (sentCounts[it] ?: 0) + (receivedCounts[it] ?: 0) }
            ?.let { userId ->
                val total = (sentCounts[userId] ?: 0) + (receivedCounts[userId] ?: 0)
                val contact = homeState.contacts.find { it.id == userId }
                if (contact != null && total > 0) EnemyStat(userId, contact.displayName.ifBlank { contact.email }, contact.photoUrl, total)
                else null
            }
    }

    // "Miglior nemico di sempre" — most interactions all time
    val allTimeEnemy = remember(historyState.sentAlerts, historyState.receivedAlerts, homeState.contacts) {
        val sentCounts = historyState.sentAlerts
            .groupBy { it.toUserId }
            .mapValues { it.value.size }
        val receivedCounts = historyState.receivedAlerts
            .groupBy { it.fromUserId }
            .mapValues { it.value.size }
        val allIds = sentCounts.keys + receivedCounts.keys
        allIds.maxByOrNull { (sentCounts[it] ?: 0) + (receivedCounts[it] ?: 0) }
            ?.let { userId ->
                val total = (sentCounts[userId] ?: 0) + (receivedCounts[userId] ?: 0)
                val contact = homeState.contacts.find { it.id == userId }
                if (contact != null && total > 0) EnemyStat(userId, contact.displayName.ifBlank { contact.email }, contact.photoUrl, total)
                else null
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

        // Zen Mode
        item {
            Text(
                text = "Modalita Zen",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = DuckTheme.colors.sectionTitle
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (homeState.zenMode) DuckOrange500
                                     else DuckTheme.colors.cardBackground
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Niente Duck, solo relax",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (homeState.zenMode) Color.White
                                else DuckTheme.colors.sectionTitle,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = homeState.zenMode,
                        onCheckedChange = { homeViewModel.toggleZenMode() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color.White.copy(alpha = 0.3f),
                            checkedBorderColor = Color.Transparent,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = DuckGrey300,
                            uncheckedBorderColor = Color.Transparent
                        )
                    )
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

        // Arma preferita + Starnazzi totali (square, side by side)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Arma preferita
                val weaponBgColor = if (topLevel != null) {
                    when (topLevel.first) {
                        StarnazzoLevel.LIGHT -> StarnazzoLightTenue
                        StarnazzoLevel.MEDIUM -> StarnazzoMediumTenue
                        StarnazzoLevel.HEAVY -> StarnazzoHeavyTenue
                    }
                } else DuckTheme.colors.cardBackground

                val weaponTextColor = if (topLevel != null) {
                    when (topLevel.first) {
                        StarnazzoLevel.LIGHT -> StarnazzoLight
                        StarnazzoLevel.MEDIUM -> StarnazzoMedium
                        StarnazzoLevel.HEAVY -> StarnazzoHeavy
                    }
                } else DuckTheme.colors.textSecondary

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(130.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = weaponBgColor
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "Arma preferita",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = weaponTextColor,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 14.dp)
                        )
                        if (topLevel != null) {
                            val dashCtx = LocalContext.current
                            val dashAnimalKey = AnimalRegistry.getSelectedAnimal(dashCtx, topLevel.first)
                            AnimalEmoji(
                                animalKey = dashAnimalKey,
                                emoji = AnimalRegistry.getEmoji(dashAnimalKey, topLevel.first),
                                size = 60.dp,
                                fontSize = 50.sp,
                                modifier = Modifier
                                    .fillMaxHeight(0.6f)
                                    .align(Alignment.BottomCenter)
                            )
                        } else {
                            Text(
                                text = "—",
                                fontSize = 40.sp,
                                color = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                }

                // Starnazzi totali
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(130.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = DuckTheme.colors.cardBackground
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "Duck totali",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = DuckTheme.colors.sectionTitle,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(top = 14.dp)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${historyState.sentCount}",
                                fontSize = 52.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = DuckTheme.colors.accentDark,
                                lineHeight = 52.sp
                            )
                        }
                    }
                }
            }
        }

        // People stats section
        if (weekWarContact != null || allTimeEnemy != null) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Chi l'ha fatta grossa",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DuckTheme.colors.sectionTitle
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (weekWarContact != null) {
                        EnemyCard(
                            label = "Guerra aperta",
                            name = weekWarContact.name,
                            photoUrl = weekWarContact.photoUrl,
                            subtitle = "Duckato ${weekWarContact.count} volte questa settimana",
                            onClick = { onNavigateToContact?.invoke(weekWarContact.userId) }
                        )
                    }
                    if (allTimeEnemy != null) {
                        EnemyCard(
                            label = "Miglior nemico di sempre",
                            name = allTimeEnemy.name,
                            photoUrl = allTimeEnemy.photoUrl,
                            subtitle = "Duckato ${allTimeEnemy.count} volte in totale",
                            onClick = { onNavigateToContact?.invoke(allTimeEnemy.userId) }
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
private fun EnemyCard(
    label: String,
    name: String,
    photoUrl: String,
    subtitle: String,
    onClick: () -> Unit
) {
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Text(
                text = label,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = DuckTheme.colors.sectionTitle
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (photoUrl.isNotBlank()) {
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = name,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(DuckTheme.colors.accentLight),
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
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DuckTheme.colors.textPrimary
                    )
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = DuckTheme.colors.textSecondary
                    )
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(DuckTheme.colors.highlight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Campaign,
                        contentDescription = "Duck",
                        modifier = Modifier.size(20.dp),
                        tint = DuckTheme.colors.sectionTitle
                    )
                }
            }
        }
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
