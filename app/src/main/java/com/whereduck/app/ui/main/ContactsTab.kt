package com.whereduck.app.ui.main

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.whereduck.app.data.model.Group
import com.whereduck.app.ui.home.HomeViewModel
import com.whereduck.app.ui.theme.DuckTheme

@Composable
fun ContactsTab(
    onNavigateToGroupDetail: (String) -> Unit,
    onNavigateToInvites: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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

                // Groups section
                item {
                    Spacer(modifier = Modifier.height(4.dp))
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
