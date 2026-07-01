package com.whereduck.app.ui.incoming

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.whereduck.app.data.model.AnimalRegistry
import com.whereduck.app.data.model.StarnazzoLevel
import com.whereduck.app.ui.components.AnimalEmoji
import com.whereduck.app.ui.main.animalsPerLevel
import com.whereduck.app.ui.theme.DuckOrange500
import com.whereduck.app.ui.theme.DuckTheme
import com.whereduck.app.ui.theme.StarnazzoHeavy
import com.whereduck.app.ui.theme.StarnazzoLight
import com.whereduck.app.ui.theme.StarnazzoLightTenue
import com.whereduck.app.ui.theme.StarnazzoMediumTenue
import com.whereduck.app.ui.theme.StarnazzoHeavyTenue
import com.whereduck.app.ui.theme.StarnazzoMedium

@Composable
fun IncomingAlertScreen(
    fromName: String,
    fromPhotoUrl: String = "",
    level: StarnazzoLevel,
    animalKey: String = level.defaultAnimal,
    animalEmoji: String = level.emoji,
    isRevenge: Boolean,
    onOk: () -> Unit,
    onMute: (Int) -> Unit,
    onRevenge: () -> Unit,
    onDismiss: () -> Unit
) {

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val cardScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
        label = "card_scale"
    )

    val lvlColor = when (level) {
        StarnazzoLevel.LIGHT -> StarnazzoLight
        StarnazzoLevel.MEDIUM -> StarnazzoMedium
        StarnazzoLevel.HEAVY -> StarnazzoHeavy
    }

    val levelTenueColor = when (level) {
        StarnazzoLevel.LIGHT -> StarnazzoLightTenue
        StarnazzoLevel.MEDIUM -> StarnazzoMediumTenue
        StarnazzoLevel.HEAVY -> StarnazzoHeavyTenue
    }

    // Find the animal display info
    val animalInfo = AnimalRegistry.findAnimal(animalKey)
    val animalOption = animalsPerLevel[level]?.find { it.key == animalKey }
    val noisiness = animalOption?.noisiness ?: 0.5f

    val navBarBottom = WindowInsets.navigationBars
        .asPaddingValues().calculateBottomPadding()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(lvlColor)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        // ── Sender name ──
        Text(
            text = fromName,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ── Sender photo ──
        if (fromPhotoUrl.isNotBlank()) {
            AsyncImage(
                model = fromPhotoUrl,
                contentDescription = fromName,
                modifier = Modifier
                    .size(180.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Surface(
                modifier = Modifier.size(180.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.2f)
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Person, null,
                        Modifier.size(60.dp),
                        tint = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── Title ──
        Text(
            text = if (isRevenge) "You got Revenged!" else "Where the Duck are you?",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ── Animal card (same style as send screen) ──
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .scale(cardScale),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = levelTenueColor),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Top row: level chip + noisiness bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
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
                                    .fillMaxWidth(fraction = noisiness)
                                    .height(12.dp)
                                    .background(lvlColor, RoundedCornerShape(6.dp))
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
                        animalKey = animalKey,
                        emoji = animalEmoji,
                        size = 200.dp,
                        fontSize = 120.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // ── Action buttons ──
        var showMuteOptions by remember { mutableStateOf(false) }

        // OK button
        Button(
            onClick = onOk,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = lvlColor
            )
        ) {
            Text("OK!", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        AnimatedContent(
            targetState = showMuteOptions,
            transitionSpec = {
                (fadeIn(tween(300)) + scaleIn(tween(300), initialScale = 0.9f))
                    .togetherWith(fadeOut(tween(200)) + scaleOut(tween(200), targetScale = 0.9f))
            },
            label = "mute_buttons"
        ) { muteMode ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!muteMode) {
                    // Non mi rompere + Revenge
                    Button(
                        onClick = { showMuteOptions = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DuckOrange500,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Non mi rompere!", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    if (!isRevenge) {
                        Button(
                            onClick = onRevenge,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE53935),
                                contentColor = Color.White
                            )
                        ) {
                            Text("Revenge!", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                } else {
                    // Mute duration buttons
                    listOf(5 to "5 min", 30 to "30 min", 60 to "1 ora").forEach { (minutes, label) ->
                        Button(
                            onClick = { onMute(minutes) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DuckOrange500,
                                contentColor = Color.White
                            )
                        ) {
                            Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(maxOf(24.dp, navBarBottom + 16.dp)))
    }
}
