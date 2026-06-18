package com.whereduck.app.ui.contactdetail

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
    onContactRemoved: () -> Unit,
    viewModel: ContactDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showRemoveDialog by remember { mutableStateOf(false) }

    val isCalling = uiState.callPhase != null

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

    // Background color animation
    fun levelColor(level: StarnazzoLevel): Color = when (level) {
        StarnazzoLevel.LIGHT -> StarnazzoLight
        StarnazzoLevel.MEDIUM -> StarnazzoMedium
        StarnazzoLevel.HEAVY -> StarnazzoHeavy
    }

    val bgColor by animateColorAsState(
        targetValue = if (isCalling) levelColor(uiState.selectedLevel)
                      else DuckTheme.colors.sectionDashboard,
        animationSpec = tween(500),
        label = "bg_color"
    )

    // Top bar icon colors
    val topBarContentColor by animateColorAsState(
        targetValue = if (isCalling) Color.White else MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(500),
        label = "topbar_color"
    )

    // Button animation: width morphs, color morphs
    val buttonWidth by animateDpAsState(
        targetValue = if (isCalling) 60.dp else LocalConfiguration.current.screenWidthDp.dp / 2,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "btn_width"
    )
    val buttonColor by animateColorAsState(
        targetValue = if (isCalling) DuckTheme.colors.negative
                      else levelColor(uiState.selectedLevel),
        animationSpec = tween(500),
        label = "btn_color"
    )

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

    // Pill/email text color animation
    val textSecondaryColor by animateColorAsState(
        targetValue = if (isCalling) Color.White.copy(alpha = 0.7f) else DuckTheme.colors.textSecondary,
        animationSpec = tween(500),
        label = "text_secondary"
    )
    val textPrimaryColor by animateColorAsState(
        targetValue = if (isCalling) Color.White else DuckTheme.colors.textPrimary,
        animationSpec = tween(500),
        label = "text_primary"
    )
    val pillBgColor by animateColorAsState(
        targetValue = if (isCalling) Color.White.copy(alpha = 0.2f) else DuckTheme.colors.pillBackground,
        animationSpec = tween(500),
        label = "pill_bg"
    )

    Box(modifier = Modifier.fillMaxSize()) {
    Scaffold(
        containerColor = bgColor,
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
                    IconButton(onClick = {
                        if (isCalling) viewModel.cancelStarnazzo() else onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Indietro")
                    }
                },
                actions = {
                    if (!isCalling) {
                        IconButton(onClick = { viewModel.toggleVip() }) {
                            Icon(
                                if (uiState.isVip) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Vittima preferita",
                                tint = if (uiState.isVip) DuckTheme.colors.vipHeart
                                       else topBarContentColor
                            )
                        }
                        IconButton(onClick = { showRemoveDialog = true }) {
                            Icon(
                                Icons.Default.PersonRemove,
                                "Rimuovi contatto",
                                tint = DuckTheme.colors.negative
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = topBarContentColor,
                    navigationIconContentColor = topBarContentColor,
                    actionIconContentColor = topBarContentColor
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
                        // Contact photo (+10% = 132dp)
                        Spacer(modifier = Modifier.height(16.dp))
                        if (contact.photoUrl.isNotBlank()) {
                            AsyncImage(
                                model = contact.photoUrl,
                                contentDescription = contact.displayName,
                                modifier = Modifier
                                    .size(132.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Surface(
                                modifier = Modifier.size(132.dp),
                                shape = CircleShape,
                                color = if (isCalling) Color.White.copy(alpha = 0.2f)
                                        else DuckTheme.colors.cardBackgroundVariant
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(60.dp),
                                        tint = if (isCalling) Color.White.copy(alpha = 0.6f)
                                               else DuckTheme.colors.textSecondary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Email
                        if (contact.email.isNotBlank()) {
                            Text(
                                text = contact.email,
                                fontSize = 13.sp,
                                color = textSecondaryColor,
                                textAlign = TextAlign.Center
                            )
                        }

                        // Motto
                        if (contact.motto.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = pillBgColor
                            ) {
                                Text(
                                    text = contact.motto,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = textPrimaryColor,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }

                        // Call status text
                        if (isCalling) {
                            Spacer(modifier = Modifier.height(16.dp))
                            when (uiState.callPhase) {
                                CallPhase.RINGING -> {
                                    Text(
                                        text = "Starnazzando...",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White.copy(alpha = dotsAlpha),
                                        textAlign = TextAlign.Center
                                    )
                                }
                                CallPhase.RESPONDED -> {
                                    val (responseEmoji, responseText) = when (uiState.callResponse) {
                                        "ok" -> "\uD83D\uDC4D" to "OK! Ha visto!"
                                        "muto" -> "\uD83D\uDD07" to "Non mi rompere!"
                                        "revenge" -> "\uD83D\uDD25" to "REVENGE!"
                                        "dismissed" -> "\uD83D\uDC4B" to "Ha chiuso"
                                        else -> "" to "Risposta ricevuta"
                                    }
                                    Text(
                                        text = responseEmoji,
                                        fontSize = 48.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = responseText,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                CallPhase.FAILED -> {
                                    Text(
                                        text = "Invio fallito",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                else -> {}
                            }
                        }

                        // Push animal + button to bottom
                        Spacer(modifier = Modifier.weight(1f))

                        // Animal area — swipeable when not calling, animated when calling
                        val levels = StarnazzoLevel.entries.toList()
                        val selectedIndex = levels.indexOf(uiState.selectedLevel)
                        var lastDirection by remember { mutableIntStateOf(1) }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .then(
                                    if (!isCalling) {
                                        Modifier.pointerInput(selectedIndex) {
                                            var totalDrag = 0f
                                            detectHorizontalDragGestures(
                                                onDragEnd = {
                                                    if (totalDrag > 80f) {
                                                        lastDirection = -1
                                                        val prev = (selectedIndex - 1 + levels.size) % levels.size
                                                        viewModel.selectLevel(levels[prev])
                                                    } else if (totalDrag < -80f) {
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
                                        }
                                    } else Modifier
                                ),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            // Ripple when calling
                            if (isCalling && uiState.callPhase == CallPhase.RINGING) {
                                Box(
                                    modifier = Modifier
                                        .size(120.dp)
                                        .scale(rippleScale)
                                        .alpha(rippleAlpha)
                                        .background(Color.White, CircleShape)
                                        .align(Alignment.Center)
                                )
                            }

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
                                        .scale(if (isCalling && uiState.callPhase == CallPhase.RINGING) emojiScale else 1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Button — morphs from wide send to circular close
                        Box(
                            modifier = Modifier
                                .width(buttonWidth)
                                .height(60.dp)
                                .clip(RoundedCornerShape(30.dp))
                                .background(buttonColor)
                                .clickable {
                                    if (uiState.isSending) {
                                        viewModel.cancelStarnazzo()
                                    } else if (isCalling) {
                                        viewModel.cancelStarnazzo()
                                    } else {
                                        viewModel.sendStarnazzo()
                                    }
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
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Chiudi",
                                    modifier = Modifier.size(28.dp),
                                    tint = Color.White
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
