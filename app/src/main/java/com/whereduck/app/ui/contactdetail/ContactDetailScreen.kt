package com.whereduck.app.ui.contactdetail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.whereduck.app.R
import com.whereduck.app.ui.components.CachedAsyncImage
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.platform.LocalContext
import com.whereduck.app.data.model.Alert
import com.whereduck.app.data.model.AnimalRegistry
import com.whereduck.app.data.model.StarnazzoLevel
import com.whereduck.app.ui.components.AnimalEmoji
import com.whereduck.app.ui.main.rememberAnimalsPerLevel
import com.whereduck.app.ui.theme.DuckOrange500
import com.whereduck.app.ui.theme.DuckTheme
import com.whereduck.app.ui.theme.StarnazzoHeavy
import com.whereduck.app.ui.theme.StarnazzoLight
import com.whereduck.app.ui.theme.StarnazzoLightTenue
import com.whereduck.app.ui.theme.StarnazzoMedium
import com.whereduck.app.ui.theme.StarnazzoMediumTenue
import com.whereduck.app.ui.theme.StarnazzoHeavyTenue
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ContactDetailScreen(
    onNavigateBack: () -> Unit,
    onContactRemoved: () -> Unit,
    viewModel: ContactDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showRemoveDialog by remember { mutableStateOf(false) }
    var showMuteOptions by remember { mutableStateOf(false) }
    val isZenMode = com.whereduck.app.ui.home.HomeViewModel.isZenMode(LocalContext.current)

    val isCalling = uiState.callPhase != null

    // Reload mute status when screen becomes visible (e.g. after IncomingAlertActivity mute)
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.RESUMED) {
            viewModel.loadMuteStatus()
        }
    }

    // Auto-dismiss snackbar
    LaunchedEffect(uiState.lastSendResult) {
        if (uiState.lastSendResult != null) {
            delay(3000)
            viewModel.clearSendResult()
        }
    }

    // Auto-dismiss after response
    LaunchedEffect(uiState.callPhase) {
        if (uiState.callPhase == CallPhase.RESPONDED) {
            delay(4000)
            viewModel.dismissCall()
        }
    }

    fun levelColor(level: StarnazzoLevel): Color = when (level) {
        StarnazzoLevel.LIGHT -> StarnazzoLight
        StarnazzoLevel.MEDIUM -> StarnazzoMedium
        StarnazzoLevel.HEAVY -> StarnazzoHeavy
    }

    fun levelTenueColor(level: StarnazzoLevel): Color = when (level) {
        StarnazzoLevel.LIGHT -> StarnazzoLightTenue
        StarnazzoLevel.MEDIUM -> StarnazzoMediumTenue
        StarnazzoLevel.HEAVY -> StarnazzoHeavyTenue
    }

    // Call animations
    val infiniteTransition = rememberInfiniteTransition(label = "call_anim")
    val emojiScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
        label = "emoji_scale"
    )
    val dotsAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800, easing = LinearEasing), RepeatMode.Reverse),
        label = "dots_alpha"
    )
    val rippleScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Restart),
        label = "ripple_scale"
    )
    val rippleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Restart),
        label = "ripple_alpha"
    )

    // Animated colors for calling state
    val bgColor by animateColorAsState(
        targetValue = if (isCalling) levelColor(uiState.selectedLevel)
                      else DuckTheme.colors.sectionDashboard,
        animationSpec = tween(500),
        label = "bg_color"
    )
    val topBarContentColor by animateColorAsState(
        targetValue = if (isCalling) Color.White else MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(500),
        label = "topbar_color"
    )
    val buttonWidth by animateDpAsState(
        targetValue = if (isCalling) 52.dp else 160.dp,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "btn_width"
    )
    val buttonColor by animateColorAsState(
        targetValue = if (isCalling) DuckTheme.colors.negative
                      else levelColor(uiState.selectedLevel),
        animationSpec = tween(500),
        label = "btn_color"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = bgColor,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = uiState.contact?.displayName?.ifBlank { stringResource(R.string.contact_fallback) } ?: stringResource(R.string.contact_fallback),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (isCalling) viewModel.cancelStarnazzo() else onNavigateBack()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.common_back))
                        }
                    },
                    actions = {},
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = topBarContentColor,
                        navigationIconContentColor = topBarContentColor,
                        actionIconContentColor = topBarContentColor
                    )
                )
            }
        ) { paddingValues ->
            val topPadding = paddingValues.calculateTopPadding()
            Box(modifier = Modifier.fillMaxSize()) {
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
                                text = stringResource(R.string.contact_not_found),
                                style = MaterialTheme.typography.titleLarge,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    else -> {
                        val contact = uiState.contact!!
                        val context = LocalContext.current
                        val navBarBottom = WindowInsets.navigationBars
                            .asPaddingValues().calculateBottomPadding()

                        // ── UNIFIED LAYOUT: animates between normal and calling ──
                        val animalsPerLevel = rememberAnimalsPerLevel()
                        val levelAnimals = remember(animalsPerLevel) {
                            StarnazzoLevel.entries.map { level ->
                                val selectedKey = AnimalRegistry.getSelectedAnimal(context, level)
                                val animal = animalsPerLevel[level]
                                    ?.find { it.key == selectedKey }
                                    ?: animalsPerLevel[level]!!.first()
                                level to animal
                            }
                        }

                        val virtualPageCount = 1000
                        val startPage = (virtualPageCount / 2) - ((virtualPageCount / 2) % levelAnimals.size)
                        val pagerState = rememberPagerState(initialPage = startPage) { virtualPageCount }

                        // Sync pager → viewModel
                        val settledPage = pagerState.settledPage
                        LaunchedEffect(settledPage) {
                            val (level, animal) = levelAnimals[settledPage % levelAnimals.size]
                            viewModel.selectAnimal(level, animal.key)
                        }

                        // Card scale animation during calling
                        val cardScale by animateFloatAsState(
                            targetValue = if (isCalling && uiState.callPhase == CallPhase.RINGING) emojiScale else 1f,
                            animationSpec = spring(stiffness = Spring.StiffnessLow),
                            label = "card_scale"
                        )

                        // Text color animation
                        val textColor by animateColorAsState(
                            targetValue = if (isCalling) Color.White else DuckTheme.colors.textSecondary,
                            animationSpec = tween(500),
                            label = "text_color"
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = topPadding)
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(modifier = Modifier.height(8.dp))

                            // ── Photo (always visible) ──
                            if (contact.photoUrl.isNotBlank()) {
                                CachedAsyncImage(
                                    model = contact.photoUrl,
                                    contentDescription = contact.displayName,
                                    modifier = Modifier
                                        .size(180.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Surface(
                                    modifier = Modifier.size(180.dp),
                                    shape = CircleShape,
                                    color = if (isCalling) Color.White.copy(alpha = 0.2f)
                                            else DuckTheme.colors.cardBackgroundVariant
                                ) {
                                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.Person, null,
                                            Modifier.size(56.dp),
                                            tint = if (isCalling) Color.White.copy(alpha = 0.6f)
                                                   else DuckTheme.colors.textSecondary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // ── Email (always visible) ──
                            if (contact.email.isNotBlank()) {
                                Text(
                                    text = contact.email,
                                    fontSize = 13.sp,
                                    color = textColor,
                                    textAlign = TextAlign.Center
                                )
                            }

                            // ── Motto (always visible) ──
                            if (contact.motto.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isCalling) Color.White.copy(alpha = 0.15f)
                                            else DuckOrange500.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = contact.motto,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isCalling) Color.White else DuckTheme.colors.textPrimary,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                    )
                                }
                            }

                            // ── Call phase text (only during call) ──
                            AnimatedVisibility(
                                visible = isCalling,
                                enter = fadeIn(tween(300)) + expandVertically(),
                                exit = fadeOut(tween(200)) + shrinkVertically()
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    when (uiState.callPhase) {
                                        CallPhase.RINGING -> {}
                                        CallPhase.RESPONDED -> {
                                            val (responseEmoji, responseText) = when (uiState.callResponse) {
                                                "ok" -> "\uD83D\uDC4D" to stringResource(R.string.response_ok)
                                                "muto" -> "\uD83D\uDD07" to stringResource(R.string.response_mute)
                                                "revenge" -> "\uD83D\uDD25" to stringResource(R.string.response_revenge)
                                                "dismissed" -> "\uD83D\uDC4B" to stringResource(R.string.response_dismissed)
                                                "zen" -> "\uD83E\uDDD8" to stringResource(R.string.response_zen, contact.displayName)
                                                else -> "" to stringResource(R.string.response_fallback)
                                            }
                                            Text(responseEmoji, fontSize = 48.sp, textAlign = TextAlign.Center)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(responseText, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, textAlign = TextAlign.Center)
                                        }
                                        CallPhase.FAILED -> {
                                            Text(stringResource(R.string.call_failed), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center)
                                        }
                                        else -> {}
                                    }
                                }
                            }

                            // ── Action buttons (hide during call) ──
                            AnimatedVisibility(
                                visible = !isCalling,
                                enter = fadeIn(tween(300)) + expandVertically(),
                                exit = fadeOut(tween(200)) + shrinkVertically()
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // VIP button
                                        Surface(
                                            onClick = { viewModel.toggleVip() },
                                            shape = CircleShape,
                                            color = DuckTheme.colors.outline
                                        ) {
                                            Box(
                                                modifier = Modifier.size(64.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    if (uiState.isVip) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                    contentDescription = stringResource(R.string.contact_favorite_desc),
                                                    modifier = Modifier.size(28.dp),
                                                    tint = if (uiState.isVip) DuckTheme.colors.vipHeart
                                                           else DuckTheme.colors.textSecondary
                                                )
                                            }
                                        }

                                        // Mute button
                                        Surface(
                                            onClick = {
                                                if (uiState.isMuted) {
                                                    viewModel.toggleMute(null)
                                                } else {
                                                    showMuteOptions = !showMuteOptions
                                                }
                                            },
                                            shape = CircleShape,
                                            color = DuckTheme.colors.outline
                                        ) {
                                            Box(
                                                modifier = Modifier.size(64.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    if (uiState.isMuted) Icons.Default.VolumeOff else Icons.Default.NotificationsOff,
                                                    contentDescription = stringResource(R.string.contact_mute_desc),
                                                    modifier = Modifier.size(28.dp),
                                                    tint = if (uiState.isMuted) DuckTheme.colors.negative
                                                           else DuckTheme.colors.textSecondary
                                                )
                                            }
                                        }

                                        // Remove contact button
                                        Surface(
                                            onClick = { showRemoveDialog = true },
                                            shape = CircleShape,
                                            color = DuckTheme.colors.outline
                                        ) {
                                            Box(
                                                modifier = Modifier.size(64.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    Icons.Default.PersonRemove,
                                                    contentDescription = stringResource(R.string.contact_remove_desc),
                                                    modifier = Modifier.size(28.dp),
                                                    tint = DuckTheme.colors.textSecondary
                                                )
                                            }
                                        }
                                    }

                                    // Mute duration chips
                                    AnimatedVisibility(
                                        visible = showMuteOptions && !uiState.isMuted
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(top = 12.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            listOf(5 to stringResource(R.string.incoming_mute_5), 30 to stringResource(R.string.incoming_mute_30), 60 to stringResource(R.string.incoming_mute_60)).forEach { (min, label) ->
                                                Surface(
                                                    onClick = {
                                                        viewModel.toggleMute(min)
                                                        showMuteOptions = false
                                                    },
                                                    shape = RoundedCornerShape(20.dp),
                                                    color = Color(0xFFFF9800)
                                                ) {
                                                    Text(
                                                        text = label,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White,
                                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // ── Animal card / Zen mode ──
                            if (isZenMode && !isCalling) {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = DuckOrange500
                                    ) {
                                        Text(
                                            text = stringResource(R.string.contact_zen_banner),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                                        )
                                    }
                                }
                            } else if (!isZenMode || isCalling) {
                                // Animated card properties
                                val selectedIndex = levelAnimals.indexOfFirst { it.first == uiState.selectedLevel }
                                val currentIndex = if (selectedIndex >= 0) selectedIndex else 0
                                val (selectedLevel, selectedAnimal) = levelAnimals[currentIndex]
                                val selectedLvlColor = levelColor(selectedLevel)

                                val cardBgColor by animateColorAsState(
                                    targetValue = if (isCalling) levelTenueColor(selectedLevel)
                                                  else DuckTheme.colors.cardBackground,
                                    animationSpec = tween(500),
                                    label = "card_bg"
                                )
                                val circleFraction by animateFloatAsState(
                                    targetValue = if (isCalling) 1f else 0f,
                                    animationSpec = tween(600),
                                    label = "circle_grow"
                                )
                                val textAlpha by animateFloatAsState(
                                    targetValue = if (isCalling) 0f else 1f,
                                    animationSpec = tween(300),
                                    label = "text_alpha"
                                )

                                if (!isCalling) {
                                    // Normal state: carousel
                                    HorizontalPager(
                                        state = pagerState,
                                        contentPadding = PaddingValues(horizontal = 32.dp),
                                        pageSpacing = 12.dp
                                    ) { page ->
                                        val (level, animal) = levelAnimals[page % levelAnimals.size]
                                        val lvlColor = levelColor(level)

                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
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
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Surface(shape = CircleShape, color = lvlColor) {
                                                        Text(
                                                            text = level.displayName,
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color.White,
                                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                                        )
                                                    }
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Default.VolumeUp, null, Modifier.size(14.dp), tint = DuckTheme.colors.textSecondary)
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Box(
                                                            modifier = Modifier
                                                                .width(60.dp)
                                                                .height(12.dp)
                                                                .background(DuckTheme.colors.cardBackgroundVariant, RoundedCornerShape(6.dp))
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
                                                        .clip(CircleShape)
                                                        .background(levelTenueColor(level)),
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
                                                Text(animal.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DuckTheme.colors.textPrimary)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(animal.description, fontSize = 13.sp, color = DuckTheme.colors.textSecondary, textAlign = TextAlign.Center, minLines = 2, maxLines = 2)
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(animal.quote, fontSize = 12.sp, fontWeight = FontWeight.Medium, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, color = lvlColor, textAlign = TextAlign.Center, minLines = 2, maxLines = 2)
                                            }
                                        }
                                    }
                                } else {
                                    // Calling state: single card with animated expansion
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 32.dp)
                                            .aspectRatio(1f)
                                            .scale(cardScale),
                                        shape = RoundedCornerShape(20.dp),
                                        colors = CardDefaults.cardColors(containerColor = cardBgColor),
                                        elevation = CardDefaults.cardElevation(0.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(RoundedCornerShape(20.dp))
                                        ) {
                                            // Expanding circle background
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                val circleSize = 140.dp + (400.dp - 140.dp) * circleFraction
                                                Box(
                                                    modifier = Modifier
                                                        .size(circleSize)
                                                        .clip(CircleShape)
                                                        .background(levelTenueColor(selectedLevel))
                                                )
                                            }

                                            // Top row: level chip + noisiness bar
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Surface(shape = CircleShape, color = selectedLvlColor) {
                                                    Text(
                                                        text = selectedLevel.displayName,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White,
                                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                                    )
                                                }
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.VolumeUp, null, Modifier.size(14.dp), tint = DuckTheme.colors.textSecondary)
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .width(60.dp)
                                                            .height(12.dp)
                                                            .background(DuckTheme.colors.cardBackgroundVariant, RoundedCornerShape(6.dp))
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxWidth(fraction = selectedAnimal.noisiness)
                                                                .height(12.dp)
                                                                .background(selectedLvlColor, RoundedCornerShape(6.dp))
                                                        )
                                                    }
                                                }
                                            }

                                            // Animal aligned to bottom
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(top = 48.dp, start = 20.dp, end = 20.dp),
                                                contentAlignment = Alignment.BottomCenter
                                            ) {
                                                AnimalEmoji(
                                                    animalKey = selectedAnimal.key,
                                                    emoji = selectedAnimal.emoji,
                                                    size = 200.dp,
                                                    fontSize = 120.sp
                                                )
                                            }

                                            // Fading text
                                            if (textAlpha > 0.01f) {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .align(Alignment.BottomCenter)
                                                        .padding(bottom = 16.dp, start = 20.dp, end = 20.dp)
                                                        .graphicsLayer { alpha = textAlpha },
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Text(selectedAnimal.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DuckTheme.colors.textPrimary)
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(selectedAnimal.description, fontSize = 13.sp, color = DuckTheme.colors.textSecondary, textAlign = TextAlign.Center, maxLines = 2)
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // ── DUCK! / Cancel button ──
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(buttonWidth)
                                            .height(52.dp)
                                            .clip(CircleShape)
                                            .background(buttonColor)
                                            .clickable {
                                                if (isCalling) viewModel.cancelStarnazzo()
                                                else viewModel.sendStarnazzo()
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (uiState.isSending) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(24.dp),
                                                strokeWidth = 2.dp,
                                                color = Color.White
                                            )
                                        } else if (isCalling) {
                                            Icon(Icons.Default.Close, stringResource(R.string.contact_close_desc), Modifier.size(28.dp), tint = Color.White)
                                        } else {
                                            Text(
                                                text = stringResource(R.string.contact_duck_button),
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }

                            // ── Stats (hide during call) ──
                            AnimatedVisibility(
                                visible = !isCalling,
                                enter = fadeIn(tween(300)) + expandVertically(),
                                exit = fadeOut(tween(200)) + shrinkVertically()
                            ) {
                                Column {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 20.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Card(
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(28.dp),
                                            colors = CardDefaults.cardColors(containerColor = DuckTheme.colors.cardBackground),
                                            elevation = CardDefaults.cardElevation(0.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(stringResource(R.string.contact_stats_streak), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DuckTheme.colors.textSecondary)
                                                Text("${uiState.streakDays}d", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = DuckOrange500)
                                            }
                                        }
                                        Card(
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(28.dp),
                                            colors = CardDefaults.cardColors(containerColor = DuckTheme.colors.cardBackground),
                                            elevation = CardDefaults.cardElevation(0.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(stringResource(R.string.contact_stats_sent), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DuckTheme.colors.textSecondary)
                                                Text("${uiState.sentCount}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = DuckTheme.colors.accentDark)
                                            }
                                        }
                                        Card(
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(28.dp),
                                            colors = CardDefaults.cardColors(containerColor = DuckTheme.colors.cardBackground),
                                            elevation = CardDefaults.cardElevation(0.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(stringResource(R.string.contact_stats_received), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DuckTheme.colors.textSecondary)
                                                Text("${uiState.receivedCount}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = StarnazzoLight)
                                            }
                                        }
                                    }
                                }
                            }

                            // ── Recent history (hide during call) ──
                            AnimatedVisibility(
                                visible = !isCalling && uiState.recentAlerts.isNotEmpty(),
                                enter = fadeIn(tween(300)) + expandVertically(),
                                exit = fadeOut(tween(200)) + shrinkVertically()
                            ) {
                                Column {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = stringResource(R.string.contact_recent_header),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DuckTheme.colors.sectionTitle,
                                        modifier = Modifier.padding(horizontal = 20.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    uiState.recentAlerts.forEach { alert ->
                                        RecentAlertRow(
                                            alert = alert,
                                            isSentByMe = alert.fromUserId != viewModel.contactId,
                                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(maxOf(24.dp, navBarBottom + 16.dp)))
                        }
                    }
                }
            }
        }

        // Toast overlay
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
    }

    // Remove contact dialog
    if (showRemoveDialog) {
        AlertDialog(
            onDismissRequest = { showRemoveDialog = false },
            title = { Text(stringResource(R.string.contact_remove_title)) },
            text = {
                val displayName = uiState.contact?.displayName
                Text(
                    if (displayName != null) stringResource(R.string.contact_remove_body, displayName)
                    else stringResource(R.string.contact_remove_body_fallback)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showRemoveDialog = false
                    viewModel.removeContact()
                    onContactRemoved()
                }) {
                    Text(stringResource(R.string.common_remove), color = DuckTheme.colors.negative)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }
}

@Composable
private fun RecentAlertRow(
    alert: Alert,
    isSentByMe: Boolean,
    modifier: Modifier = Modifier
) {
    val timeFmt = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val dayFmt = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val displayFmt = remember { SimpleDateFormat("dd/MM", Locale.getDefault()) }

    val alertDate = alert.createdAt?.toDate()
    val today = remember { dayFmt.format(Date()) }
    val yesterday = remember {
        Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }.let { dayFmt.format(it.time) }
    }

    val timeText = alertDate?.let {
        val dayKey = dayFmt.format(it)
        when (dayKey) {
            today -> timeFmt.format(it)
            yesterday -> "${stringResource(R.string.contact_yesterday_prefix)}${timeFmt.format(it)}"
            else -> "${displayFmt.format(it)} ${timeFmt.format(it)}"
        }
    } ?: ""

    val level = StarnazzoLevel.fromKey(alert.starnazzoLevel)
    val levelColor = when (level) {
        StarnazzoLevel.LIGHT -> StarnazzoLight
        StarnazzoLevel.MEDIUM -> StarnazzoMedium
        StarnazzoLevel.HEAVY -> StarnazzoHeavy
    }

    val tenueColor = when (level) {
        StarnazzoLevel.LIGHT -> StarnazzoLightTenue
        StarnazzoLevel.MEDIUM -> StarnazzoMediumTenue
        StarnazzoLevel.HEAVY -> StarnazzoHeavyTenue
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(50),
        colors = CardDefaults.cardColors(containerColor = DuckTheme.colors.cardBackground),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Animal badge (large)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(tenueColor),
                contentAlignment = Alignment.Center
            ) {
                AnimalEmoji(
                    animalKey = alert.animalType,
                    emoji = AnimalRegistry.getEmoji(alert.animalType, level),
                    size = 34.dp,
                    fontSize = 26.sp
                )
            }

            // Direction arrow (thick, next to animal)
            Icon(
                imageVector = if (isSentByMe) Icons.AutoMirrored.Filled.CallMade
                              else Icons.AutoMirrored.Filled.CallReceived,
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 6.dp)
                    .size(22.dp),
                tint = if (isSentByMe) DuckOrange500 else StarnazzoLight
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Time
            Text(
                text = timeText,
                fontSize = 14.sp,
                color = DuckTheme.colors.textSecondary,
                modifier = Modifier.weight(1f)
            )

            // Response chip
            alert.response?.let { resp ->
                val (chipText, chipColor) = when (resp) {
                    "ok" -> "OK!" to Color(0xFF4CAF50)
                    "muto" -> "Mutato" to DuckTheme.colors.negative
                    "revenge" -> "Revenge!" to Color(0xFFFF9800)
                    "dismissed" -> "Chiuso" to DuckTheme.colors.textSecondary
                    else -> null to null
                }
                if (chipText != null && chipColor != null) {
                    Surface(
                        shape = CircleShape,
                        color = chipColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = chipText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = chipColor,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            // Revenge badge
            if (alert.isRevenge) {
                Spacer(modifier = Modifier.width(4.dp))
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFF9800).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "REVENGE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFFF9800),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
