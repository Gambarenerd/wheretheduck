package com.whereduck.app.ui.contactdetail

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.hilt.navigation.compose.hiltViewModel
import com.whereduck.app.data.model.StarnazzoLevel
import com.whereduck.app.ui.theme.DuckTheme
import com.whereduck.app.ui.theme.StarnazzoHeavy
import com.whereduck.app.ui.theme.StarnazzoLight
import com.whereduck.app.ui.theme.StarnazzoMedium
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToStarnazzoCall: (alertId: String, toName: String, level: String) -> Unit,
    onContactRemoved: () -> Unit,
    viewModel: ContactDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showRemoveDialog by remember { mutableStateOf(false) }

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

    Box(modifier = Modifier.fillMaxSize()) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = uiState.contact?.displayName?.ifBlank { "Contatto" } ?: "Contatto",
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
                    IconButton(onClick = { showRemoveDialog = true }) {
                        Icon(
                            Icons.Default.PersonRemove,
                            "Rimuovi contatto",
                            tint = DuckTheme.colors.negative
                        )
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
                uiState.contact == null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Contatto non trovato",
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                else -> {
                    val contact = uiState.contact!!

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Contact photo
                        Spacer(modifier = Modifier.height(16.dp))
                        if (contact.photoUrl.isNotBlank()) {
                            AsyncImage(
                                model = contact.photoUrl,
                                contentDescription = contact.displayName,
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Surface(
                                modifier = Modifier.size(120.dp),
                                shape = CircleShape,
                                color = DuckTheme.colors.cardBackgroundVariant
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(56.dp),
                                        tint = DuckTheme.colors.textSecondary
                                    )
                                }
                            }
                        }

                        // VIP heart below photo
                        Spacer(modifier = Modifier.height(8.dp))
                        IconButton(
                            onClick = { viewModel.toggleVip() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                if (uiState.isVip) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Vittima preferita",
                                tint = if (uiState.isVip) DuckTheme.colors.vipHeart
                                       else DuckTheme.colors.textSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        // Email
                        if (contact.email.isNotBlank()) {
                            Text(
                                text = contact.email,
                                fontSize = 13.sp,
                                color = DuckTheme.colors.textSecondary,
                                textAlign = TextAlign.Center
                            )
                        }

                        // Motto
                        if (contact.motto.isNotBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = DuckTheme.colors.pillBackground
                            ) {
                                Text(
                                    text = contact.motto,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = DuckTheme.colors.textPrimary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }

                        // ── Starnazzo Carousel ──
                        val levels = StarnazzoLevel.entries.toList()
                        val selectedIndex = levels.indexOf(uiState.selectedLevel)
                        val screenWidth = LocalConfiguration.current.screenWidthDp.dp
                        var lastDirection by remember { mutableIntStateOf(1) }

                        fun colorForLevel(level: StarnazzoLevel): Color = when (level) {
                            StarnazzoLevel.LIGHT -> StarnazzoLight
                            StarnazzoLevel.MEDIUM -> StarnazzoMedium
                            StarnazzoLevel.HEAVY -> StarnazzoHeavy
                        }

                        val buttonColor by animateColorAsState(
                            targetValue = colorForLevel(uiState.selectedLevel),
                            animationSpec = spring(stiffness = Spring.StiffnessLow),
                            label = "btn_color"
                        )

                        // Fill remaining space — push animal + button to bottom
                        Spacer(modifier = Modifier.weight(1f))

                        // Animal emoji — swipeable, fills bottom area
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .pointerInput(selectedIndex) {
                                    var totalDrag = 0f
                                    detectHorizontalDragGestures(
                                        onDragEnd = {
                                            if (totalDrag > 80f) {
                                                // Swipe right → previous
                                                lastDirection = -1
                                                val prev = (selectedIndex - 1 + levels.size) % levels.size
                                                viewModel.selectLevel(levels[prev])
                                            } else if (totalDrag < -80f) {
                                                // Swipe left → next
                                                lastDirection = 1
                                                val next = (selectedIndex + 1) % levels.size
                                                viewModel.selectLevel(levels[next])
                                            }
                                            totalDrag = 0f
                                        },
                                        onDragCancel = { totalDrag = 0f },
                                        onHorizontalDrag = { _, dragAmount ->
                                            totalDrag += dragAmount
                                        }
                                    )
                                },
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            AnimatedContent(
                                targetState = selectedIndex,
                                transitionSpec = {
                                    val dir = lastDirection
                                    (slideInHorizontally { w -> if (dir > 0) w else -w } + fadeIn())
                                        .togetherWith(
                                            slideOutHorizontally { w -> if (dir > 0) -w else w } + fadeOut()
                                        )
                                        .using(SizeTransform(clip = false))
                                },
                                label = "animal_carousel"
                            ) { idx ->
                                Text(
                                    text = levels[idx].emoji,
                                    fontSize = 180.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .offset(y = 60.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Send button — single, color animates with level
                        Box(
                            modifier = Modifier
                                .width(screenWidth / 2)
                                .height(60.dp)
                                .clip(RoundedCornerShape(30.dp))
                                .background(buttonColor)
                                .clickable(enabled = !uiState.isSending) {
                                    viewModel.sendStarnazzo()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (uiState.isSending) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                            } else {
                                Text(
                                    text = "STARNAZZA!",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }

        }
    }

    // Toast overlay (centered on screen, above everything)
    AnimatedVisibility(
        visible = uiState.lastSendResult != null,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier
            .align(Alignment.Center)
            .padding(32.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = DuckTheme.colors.textPrimary.copy(alpha = 0.9f),
            shadowElevation = 8.dp
        ) {
            Text(
                text = uiState.lastSendResult ?: "",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp)
            )
        }
    }
    } // close outer Box

    // Remove contact dialog
    if (showRemoveDialog) {
        AlertDialog(
            onDismissRequest = { showRemoveDialog = false },
            title = { Text("Rimuovi contatto") },
            text = {
                Text("Vuoi rimuovere ${uiState.contact?.displayName ?: "questo contatto"}? Non potrete piu' inviarvi starnazzi.")
            },
            confirmButton = {
                TextButton(onClick = {
                    showRemoveDialog = false
                    viewModel.removeContact()
                    onContactRemoved()
                }) {
                    Text("Rimuovi", color = DuckTheme.colors.negative)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveDialog = false }) {
                    Text("Annulla")
                }
            }
        )
    }
}
