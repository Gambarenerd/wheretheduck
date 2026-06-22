package com.whereduck.app.ui.main

import android.media.MediaPlayer
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.whereduck.app.data.model.StarnazzoLevel
import com.whereduck.app.ui.theme.DuckTheme
import com.whereduck.app.ui.theme.StarnazzoHeavy
import com.whereduck.app.ui.theme.StarnazzoLight
import com.whereduck.app.ui.theme.StarnazzoMedium
import kotlin.math.absoluteValue

// ── Animal registry ──

data class AnimalOption(
    val key: String,
    val emoji: String,
    val name: String,
    val description: String,
    val quote: String,
    val noisiness: Float,       // 0f..1f — barra rumorosita'
    val reactionTime: String,   // fake stat per ora
    val soundRes: Int? = null
)

val animalsPerLevel: Map<StarnazzoLevel, List<AnimalOption>> = mapOf(
    StarnazzoLevel.LIGHT to listOf(
        AnimalOption(
            key = "cricket",
            emoji = StarnazzoLevel.LIGHT.emoji,
            name = "Grillo",
            description = "Fa cri cri nella notte, silenzioso ma letale",
            quote = "\"Cri cri... ti sento respirare...\"",
            noisiness = 0.15f,
            reactionTime = "~12s",
            soundRes = StarnazzoLevel.LIGHT.soundRes
        ),
        AnimalOption(
            key = "robot",
            emoji = "\uD83E\uDD16",
            name = "Robot",
            description = "Bip bop, il futuro dello starnazzo e' arrivato",
            quote = "\"BEEP BOOP. Starnazzo protocollare iniziato.\"",
            noisiness = 0.28f,
            reactionTime = "~9s",
            soundRes = com.whereduck.app.R.raw.robot
        )
    ),
    StarnazzoLevel.MEDIUM to listOf(
        AnimalOption(
            key = "duck",
            emoji = StarnazzoLevel.MEDIUM.emoji,
            name = "Anatra",
            description = "Il classico starnazzo, nessuno ne esce indenne",
            quote = "\"QUACK! Non puoi ignorarmi.\"",
            noisiness = 0.55f,
            reactionTime = "~5s",
            soundRes = StarnazzoLevel.MEDIUM.soundRes
        )
    ),
    StarnazzoLevel.HEAVY to listOf(
        AnimalOption(
            key = "goat",
            emoji = StarnazzoLevel.HEAVY.emoji,
            name = "Capra Pazza",
            description = "Urla come se non ci fosse un domani",
            quote = "\"BEEEH! Non mi ferma nessuno!\"",
            noisiness = 0.92f,
            reactionTime = "~2s",
            soundRes = StarnazzoLevel.HEAVY.soundRes
        )
    )
)

// ── Tab ──

@Composable
fun CustomizeTab() {
    val context = LocalContext.current
    val mediaPlayer = remember { MediaPlayer() }

    var expandedLevel by remember { mutableStateOf<StarnazzoLevel?>(null) }

    val selectedAnimals = remember {
        mutableStateMapOf(
            StarnazzoLevel.LIGHT to "cricket",
            StarnazzoLevel.MEDIUM to "duck",
            StarnazzoLevel.HEAVY to "goat"
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                mediaPlayer.stop()
                mediaPlayer.release()
            } catch (_: Exception) { }
        }
    }

    fun playSound(soundRes: Int?) {
        if (soundRes == null) return
        try {
            mediaPlayer.reset()
            val afd = context.resources.openRawResourceFd(soundRes)
            mediaPlayer.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            afd.close()
            mediaPlayer.prepare()
            mediaPlayer.start()
        } catch (_: Exception) { }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        StarnazzoLevel.entries.forEach { level ->
            val animals = animalsPerLevel[level] ?: return@forEach
            val selectedKey = selectedAnimals[level] ?: animals.first().key
            val isExpanded = expandedLevel == level
            val hasMultiple = animals.size > 1

            if (hasMultiple) {
                CarouselLevelSection(
                    level = level,
                    animals = animals,
                    selectedKey = selectedKey,
                    isExpanded = isExpanded,
                    onTestSound = { animal -> playSound(animal.soundRes) },
                    onToggleCarousel = {
                        expandedLevel = if (isExpanded) null else level
                    },
                    onSelectAnimal = { animal ->
                        selectedAnimals[level] = animal.key
                    }
                )
            } else {
                SingleAnimalCard(
                    level = level,
                    animal = animals.first(),
                    onTestSound = { playSound(animals.first().soundRes) }
                )
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

// ── Single animal (no carousel needed) ──

@Composable
private fun SingleAnimalCard(
    level: StarnazzoLevel,
    animal: AnimalOption,
    onTestSound: () -> Unit
) {
    val levelColor = when (level) {
        StarnazzoLevel.LIGHT -> StarnazzoLight
        StarnazzoLevel.MEDIUM -> StarnazzoMedium
        StarnazzoLevel.HEAVY -> StarnazzoHeavy
    }

    Box(modifier = Modifier.padding(horizontal = 20.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = DuckTheme.colors.cardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            AnimalCardContent(
                animal = animal,
                level = level,
                levelColor = levelColor,
                showSelectButton = false,
                showChangeButton = false,
                onTestSound = onTestSound,
                onActionButton = {}
            )
        }
    }
}

// ── Carousel level section — always renders HorizontalPager, animates properties ──

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CarouselLevelSection(
    level: StarnazzoLevel,
    animals: List<AnimalOption>,
    selectedKey: String,
    isExpanded: Boolean,
    onTestSound: (AnimalOption) -> Unit,
    onToggleCarousel: () -> Unit,
    onSelectAnimal: (AnimalOption) -> Unit
) {
    val levelColor = when (level) {
        StarnazzoLevel.LIGHT -> StarnazzoLight
        StarnazzoLevel.MEDIUM -> StarnazzoMedium
        StarnazzoLevel.HEAVY -> StarnazzoHeavy
    }

    val selectedAnimal = animals.find { it.key == selectedKey } ?: animals.first()
    val selectedIndex = animals.indexOf(selectedAnimal)

    // All animations on the same composable — no tree swaps
    val liftProgress by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "lift"
    )
    val neighborAlpha by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = tween(400),
        label = "neighbor_alpha"
    )

    val loopCount = 1000
    val loopMiddle = (loopCount / 2) - ((loopCount / 2) % animals.size) + selectedIndex

    val pagerState = rememberPagerState(
        initialPage = loopMiddle,
        pageCount = { loopCount }
    )

    // Auto-select the centered animal when scrolling stops
    if (isExpanded && !pagerState.isScrollInProgress) {
        val centeredIndex = pagerState.currentPage % animals.size
        val centeredAnimal = animals[centeredIndex]
        LaunchedEffect(centeredIndex) {
            onSelectAnimal(centeredAnimal)
        }
    }

    // Animated padding: 20dp collapsed → 48dp expanded (reveals neighbor edges)
    val sidePadding = lerp(20f, 48f, liftProgress)

    HorizontalPager(
        state = pagerState,
        contentPadding = PaddingValues(horizontal = sidePadding.dp),
        pageSpacing = lerp(0f, 12f, liftProgress).dp,
        userScrollEnabled = isExpanded,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                // Lift: slight scale down + move up
                val scale = lerp(1f, 0.97f, liftProgress)
                scaleX = scale
                scaleY = scale
                translationY = lerp(0f, -20f, liftProgress)
            }
    ) { page ->
        val actualIndex = page % animals.size
        val animal = animals[actualIndex]
        val isSelected = animal.key == selectedKey
        val isCenterPage = page == pagerState.currentPage

        val pageOffset = ((pagerState.currentPage - page) +
                pagerState.currentPageOffsetFraction).absoluteValue

        val isNeighbor = pageOffset > 0.01f
        val neighborScale = lerp(0.92f, 1f, 1f - pageOffset.coerceIn(0f, 1f))
        val pageAlpha = if (isNeighbor) {
            lerp(0.85f, 1f, 1f - pageOffset.coerceIn(0f, 1f)) * neighborAlpha
        } else {
            1f
        }

        // Animated border: appears when expanded
        val borderWidth = lerp(0f, 2.5f, liftProgress)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = if (isNeighbor) neighborScale else 1f
                    scaleY = if (isNeighbor) neighborScale else 1f
                    alpha = pageAlpha
                }
                .then(
                    if (borderWidth > 0f) {
                        Modifier.border(
                            width = borderWidth.dp,
                            color = levelColor.copy(alpha = liftProgress * 0.5f),
                            shape = RoundedCornerShape(24.dp)
                        )
                    } else Modifier
                )
                .background(DuckTheme.colors.cardBackground, RoundedCornerShape(24.dp))
        ) {
            AnimalCardContent(
                animal = animal,
                level = level,
                levelColor = levelColor,
                showSelectButton = isExpanded,
                showChangeButton = !isExpanded,
                onTestSound = { onTestSound(animal) },
                onActionButton = { onToggleCarousel() }
            )
        }
    }
}

// ── Card content ──

@Composable
private fun AnimalCardContent(
    animal: AnimalOption,
    level: StarnazzoLevel,
    levelColor: Color,
    showSelectButton: Boolean,
    showChangeButton: Boolean,
    onTestSound: () -> Unit,
    onActionButton: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top row: level chip (left) + noisiness bar (right)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = CircleShape,
                color = levelColor
            ) {
                Text(
                    text = level.displayName,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            // Compact noisiness bar
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                            .background(levelColor, RoundedCornerShape(6.dp))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Animal emoji in circle
        Box(
            modifier = Modifier
                .size(140.dp)
                .background(levelColor.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = animal.emoji,
                fontSize = 72.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Animal name
        Text(
            text = animal.name,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = DuckTheme.colors.textPrimary
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Description
        Text(
            text = animal.description,
            fontSize = 13.sp,
            color = DuckTheme.colors.textSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Iconic quote
        Text(
            text = animal.quote,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
            color = levelColor,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Stats section
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Reaction time
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Timer,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = DuckTheme.colors.textSecondary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Tempo di reazione",
                    fontSize = 12.sp,
                    color = DuckTheme.colors.textSecondary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = animal.reactionTime,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = DuckTheme.colors.textPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalButton(
                onClick = onTestSound,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = levelColor.copy(alpha = 0.15f),
                    contentColor = levelColor
                )
            ) {
                Icon(
                    Icons.Default.VolumeUp,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Prova", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }

            FilledTonalButton(
                onClick = onActionButton,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (showSelectButton) levelColor.copy(alpha = 0.15f)
                                     else DuckTheme.colors.cardBackgroundVariant,
                    contentColor = if (showSelectButton) levelColor
                                   else DuckTheme.colors.textPrimary
                )
            ) {
                Icon(
                    if (showSelectButton) Icons.Default.Check else Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    if (showSelectButton) "Fatto" else "Cambia",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

