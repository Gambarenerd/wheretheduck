package com.whereduck.app.ui.starnazzocall

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.whereduck.app.ui.theme.DuckTheme
import com.whereduck.app.ui.theme.StarnazzoHeavy
import com.whereduck.app.ui.theme.StarnazzoLight
import com.whereduck.app.ui.theme.StarnazzoMedium
import com.whereduck.app.data.model.StarnazzoLevel
import kotlinx.coroutines.delay

@Composable
fun StarnazzoCallScreen(
    onDismiss: () -> Unit,
    viewModel: StarnazzoCallViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val bgColor = when (uiState.level) {
        StarnazzoLevel.LIGHT -> StarnazzoLight
        StarnazzoLevel.MEDIUM -> StarnazzoMedium
        StarnazzoLevel.HEAVY -> StarnazzoHeavy
    }

    // Gradient: lighter center → darker edges
    val bgGradient = Brush.radialGradient(
        colors = listOf(
            bgColor.copy(alpha = 0.85f),
            bgColor,
            bgColor.copy(red = bgColor.red * 0.7f, green = bgColor.green * 0.7f, blue = bgColor.blue * 0.7f)
        ),
        radius = 900f
    )

    // Auto-dismiss after response shown for 4 seconds
    LaunchedEffect(uiState.phase) {
        if (uiState.phase == CallPhase.RESPONDED) {
            delay(4000)
            onDismiss()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "call_anim")

    // Pulsing emoji animation
    val emojiScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "emoji_scale"
    )

    // Dots animation for "ringing" text
    val dotsAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dots_alpha"
    )

    // Ring ripple effect
    val rippleScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple_scale"
    )
    val rippleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple_alpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Emoji with ripple
        Box(contentAlignment = Alignment.Center) {
            if (uiState.phase == CallPhase.RINGING) {
                // Ripple rings
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(rippleScale)
                        .alpha(rippleAlpha)
                        .background(Color.White, CircleShape)
                )
            }
            Text(
                text = uiState.level.emoji,
                fontSize = 100.sp,
                modifier = Modifier.scale(
                    if (uiState.phase == CallPhase.RESPONDED) 1f else emojiScale
                )
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Status text
        when (uiState.phase) {
            CallPhase.SENDING -> {
                Text(
                    text = "Invio in corso...",
                    fontSize = 20.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
            CallPhase.RINGING -> {
                Text(
                    text = "Starnazzando",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = uiState.toName,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "In attesa di risposta...",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = dotsAlpha),
                    textAlign = TextAlign.Center
                )
            }
            CallPhase.RESPONDED -> {
                val (responseEmoji, responseText) = when (uiState.response) {
                    "ok" -> "\uD83D\uDC4D" to "OK! Ha visto!"
                    "muto" -> "\uD83D\uDD07" to "Non mi rompere!"
                    "revenge" -> "\uD83D\uDD25" to "REVENGE!"
                    "dismissed" -> "\uD83D\uDC4B" to "Ha chiuso"
                    else -> "" to "Risposta ricevuta"
                }

                Text(
                    text = responseEmoji,
                    fontSize = 64.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = responseText,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = uiState.toName,
                    fontSize = 20.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
            CallPhase.FAILED -> {
                Text(
                    text = "Invio fallito",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Non e' stato possibile raggiungere ${uiState.toName}",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Hang up / close button
        if (uiState.phase != CallPhase.RESPONDED) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = {
                        viewModel.cancelStarnazzo()
                        onDismiss()
                    },
                    modifier = Modifier.size(64.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = DuckTheme.colors.negative,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        Icons.Default.CallEnd,
                        contentDescription = "Chiudi",
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Chiudi",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}
