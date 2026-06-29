package com.whereduck.app.ui.groupdetail

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.tween
import coil.compose.AsyncImage
import androidx.hilt.navigation.compose.hiltViewModel
import com.whereduck.app.data.model.AnimalRegistry
import com.whereduck.app.data.model.StarnazzoLevel
import com.whereduck.app.ui.components.AnimalEmoji
import com.whereduck.app.ui.components.ContactCard
import com.whereduck.app.ui.main.animalsPerLevel
import com.whereduck.app.ui.theme.DuckOrange500
import com.whereduck.app.ui.theme.DuckTheme
import com.whereduck.app.ui.theme.StarnazzoHeavy
import com.whereduck.app.ui.theme.StarnazzoLight
import com.whereduck.app.ui.theme.StarnazzoMedium
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GroupDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToGroupManagement: (String) -> Unit,
    onNavigateToStarnazzoCall: (alertId: String, toName: String, level: String) -> Unit,
    onNavigateToContactDetail: (String) -> Unit = {},
    viewModel: GroupDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isZenMode = com.whereduck.app.ui.home.HomeViewModel.isZenMode(LocalContext.current)

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.updateGroupPhoto(it) }
    }

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

    Scaffold(
        containerColor = DuckTheme.colors.sectionDashboard,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = uiState.groupName.ifEmpty { "Gruppo" },
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
                    IconButton(onClick = {
                        onNavigateToGroupManagement(viewModel.groupId)
                    }) {
                        Icon(Icons.Default.Settings, "Gestione gruppo")
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
                uiState.contacts.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Sei solo nel gruppo!",
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Aggiungi contatti al gruppo per iniziare a duckare.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            onNavigateToGroupManagement(viewModel.groupId)
                        }) {
                            Text("Aggiungi contatti")
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Group photo
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(180.dp)
                                        .clickable { photoPickerLauncher.launch("image/*") },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (uiState.groupPhotoUrl.isNotBlank()) {
                                        AsyncImage(
                                            model = uiState.groupPhotoUrl,
                                            contentDescription = uiState.groupName,
                                            modifier = Modifier
                                                .size(180.dp)
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Surface(
                                            modifier = Modifier.size(180.dp),
                                            shape = CircleShape,
                                            color = DuckTheme.colors.cardBackgroundVariant
                                        ) {
                                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                Icon(
                                                    Icons.Default.Groups,
                                                    null,
                                                    Modifier.size(48.dp),
                                                    tint = DuckTheme.colors.textSecondary
                                                )
                                            }
                                        }
                                    }
                                    // Camera badge
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .offset(x = (-10).dp, y = (-10).dp)
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(DuckTheme.colors.cardBackground)
                                            .clickable { photoPickerLauncher.launch("image/*") },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.CameraAlt,
                                            contentDescription = "Cambia foto",
                                            modifier = Modifier.size(18.dp),
                                            tint = DuckTheme.colors.textPrimary
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }

                        // Contacts header
                        item {
                            Text(
                                text = "Duckers",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }

                        // Contact list
                        items(uiState.contacts) { contact ->
                            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                ContactCard(
                                    contact = contact,
                                    onClick = { onNavigateToContactDetail(contact.id) }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(DuckTheme.colors.highlight)
                                            .clickable { onNavigateToContactDetail(contact.id) },
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

                        // Level carousel / Zen banner
                        item {
                            if (isZenMode) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = DuckOrange500
                                    ) {
                                        Text(
                                            text = "Sei in modalita Zen",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                                        )
                                    }
                                }
                            } else {
                            Spacer(modifier = Modifier.height(8.dp))

                            val context = LocalContext.current
                            val levelAnimals = remember {
                                StarnazzoLevel.entries.map { level ->
                                    val selectedKey = AnimalRegistry.getSelectedAnimal(context, level)
                                    val animal = animalsPerLevel[level]
                                        ?.find { it.key == selectedKey }
                                        ?: animalsPerLevel[level]!!.first()
                                    level to animal
                                }
                            }

                            fun levelColor(level: StarnazzoLevel): Color = when (level) {
                                StarnazzoLevel.LIGHT -> StarnazzoLight
                                StarnazzoLevel.MEDIUM -> StarnazzoMedium
                                StarnazzoLevel.HEAVY -> StarnazzoHeavy
                            }

                            val virtualPageCount = 1000
                            val startPage = (virtualPageCount / 2) - ((virtualPageCount / 2) % levelAnimals.size)
                            val pagerState = rememberPagerState(initialPage = startPage) { virtualPageCount }

                            val settledPage = pagerState.settledPage
                            LaunchedEffect(settledPage) {
                                val (level, _) = levelAnimals[settledPage % levelAnimals.size]
                                viewModel.selectLevel(level)
                            }

                            HorizontalPager(
                                state = pagerState,
                                contentPadding = PaddingValues(horizontal = 32.dp),
                                pageSpacing = 12.dp
                            ) { page ->
                                val (level, animal) = levelAnimals[page % levelAnimals.size]
                                val lvlColor = levelColor(level)

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = DuckTheme.colors.cardBackground),
                                    elevation = CardDefaults.cardElevation(0.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(20.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        // Top row: level chip + noisiness bar
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = lvlColor
                                            ) {
                                                Text(
                                                    text = level.displayName,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                                )
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.VolumeUp,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(14.dp),
                                                    tint = DuckTheme.colors.textSecondary
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .width(60.dp)
                                                        .height(12.dp)
                                                        .background(
                                                            DuckTheme.colors.cardBackgroundVariant,
                                                            RoundedCornerShape(6.dp)
                                                        )
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth(fraction = animal.noisiness)
                                                            .height(12.dp)
                                                            .background(lvlColor, RoundedCornerShape(6.dp))
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        Box(
                                            modifier = Modifier
                                                .size(140.dp)
                                                .background(lvlColor.copy(alpha = 0.15f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            AnimalEmoji(
                                                animalKey = animal.key,
                                                emoji = animal.emoji,
                                                size = 80.dp,
                                                fontSize = 72.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Text(
                                            text = animal.name,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DuckTheme.colors.textPrimary
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = animal.description,
                                            fontSize = 13.sp,
                                            color = DuckTheme.colors.textSecondary,
                                            textAlign = TextAlign.Center,
                                            minLines = 2,
                                            maxLines = 2
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = animal.quote,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                            color = lvlColor,
                                            textAlign = TextAlign.Center,
                                            minLines = 2,
                                            maxLines = 2
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Duck di gruppo button
                            val duckButtonColor by animateColorAsState(
                                targetValue = levelColor(uiState.selectedLevel),
                                animationSpec = tween(400),
                                label = "group_duck_btn"
                            )
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(200.dp)
                                        .height(52.dp)
                                        .clip(RoundedCornerShape(26.dp))
                                        .background(duckButtonColor)
                                        .clickable(
                                            enabled = !uiState.isBroadcasting && uiState.sendingToUserId == null
                                        ) { viewModel.sendBroadcast() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (uiState.isBroadcasting) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            strokeWidth = 2.dp,
                                            color = Color.White
                                        )
                                    } else {
                                        Text(
                                            text = "Duck di gruppo!",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                            } // else !isZenMode
                        }

                        // Bottom spacing
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }
            }

            // Snackbar for send results
            AnimatedVisibility(
                visible = uiState.lastSendResult != null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Snackbar {
                    Text(uiState.lastSendResult ?: "")
                }
            }
        }
    }
}
