package com.whereduck.app.ui.main

import android.media.MediaPlayer
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.whereduck.app.R
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Pause
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.whereduck.app.data.model.AnimalRegistry
import com.whereduck.app.data.model.StarnazzoLevel
import com.whereduck.app.ui.components.AnimalEmoji
import com.whereduck.app.ui.theme.DuckOrange500
import com.whereduck.app.ui.theme.DuckBrown900
import com.whereduck.app.ui.theme.DuckTheme
import com.whereduck.app.ui.theme.ThemeState
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

@Composable
fun rememberAnimalsPerLevel(): Map<StarnazzoLevel, List<AnimalOption>> = mapOf(
    StarnazzoLevel.LIGHT to listOf(
        AnimalOption(
            key = "cricket",
            emoji = StarnazzoLevel.LIGHT.emoji,
            name = stringResource(R.string.customize_animal_cricket),
            description = stringResource(R.string.customize_desc_cricket),
            quote = stringResource(R.string.customize_quote_cricket),
            noisiness = 0.15f,
            reactionTime = "~0s",
            soundRes = StarnazzoLevel.LIGHT.soundRes
        ),
        AnimalOption(
            key = "mosquito",
            emoji = "\uD83E\uDD9F",
            name = stringResource(R.string.customize_animal_mosquito),
            description = stringResource(R.string.customize_desc_mosquito),
            quote = stringResource(R.string.customize_quote_mosquito),
            noisiness = 0.35f,
            reactionTime = "~0s",
            soundRes = R.raw.mosquito
        )
    ),
    StarnazzoLevel.MEDIUM to listOf(
        AnimalOption(
            key = "duck",
            emoji = StarnazzoLevel.MEDIUM.emoji,
            name = stringResource(R.string.customize_animal_duck),
            description = stringResource(R.string.customize_desc_duck),
            quote = stringResource(R.string.customize_quote_duck),
            noisiness = 0.55f,
            reactionTime = "~5s",
            soundRes = StarnazzoLevel.MEDIUM.soundRes
        ),
        AnimalOption(
            key = "robot",
            emoji = "\uD83E\uDD16",
            name = stringResource(R.string.customize_animal_robot),
            description = stringResource(R.string.customize_desc_robot),
            quote = stringResource(R.string.customize_quote_robot),
            noisiness = 0.28f,
            reactionTime = "~9s",
            soundRes = com.whereduck.app.R.raw.robot
        )
    ),
    StarnazzoLevel.HEAVY to listOf(
        AnimalOption(
            key = "goat",
            emoji = StarnazzoLevel.HEAVY.emoji,
            name = stringResource(R.string.customize_animal_goat),
            description = stringResource(R.string.customize_desc_goat),
            quote = stringResource(R.string.customize_quote_goat),
            noisiness = 0.75f,
            reactionTime = "~2s",
            soundRes = StarnazzoLevel.HEAVY.soundRes
        ),
        AnimalOption(
            key = "godzilla",
            emoji = "\uD83E\uDD96",
            name = stringResource(R.string.customize_animal_godzilla),
            description = stringResource(R.string.customize_desc_godzilla),
            quote = stringResource(R.string.customize_quote_godzilla),
            noisiness = 1.0f,
            reactionTime = "~0s",
            soundRes = R.raw.godzilla
        )
    )
)

// ── Tab ──

@Composable
fun CustomizeTab() {
    val context = LocalContext.current
    val mediaPlayer = remember { MediaPlayer() }
    var playingAnimalKey by remember { mutableStateOf<String?>(null) }

    var expandedLevel by remember { mutableStateOf<StarnazzoLevel?>(null) }

    val selectedAnimals = remember {
        mutableStateMapOf<StarnazzoLevel, String>().also { map ->
            StarnazzoLevel.entries.forEach { level ->
                map[level] = AnimalRegistry.getSelectedAnimal(context, level)
            }
        }
    }

    DisposableEffect(Unit) {
        mediaPlayer.setOnCompletionListener { playingAnimalKey = null }
        onDispose {
            try {
                mediaPlayer.stop()
                mediaPlayer.release()
            } catch (_: Exception) { }
        }
    }

    fun stopSound() {
        try {
            mediaPlayer.stop()
            mediaPlayer.reset()
        } catch (_: Exception) { }
        playingAnimalKey = null
    }

    fun playSound(animalKey: String, soundRes: Int?) {
        if (soundRes == null) return
        try {
            mediaPlayer.reset()
            val afd = context.resources.openRawResourceFd(soundRes)
            mediaPlayer.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            afd.close()
            mediaPlayer.prepare()
            mediaPlayer.start()
            playingAnimalKey = animalKey
        } catch (_: Exception) { }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        val animalsPerLevel = rememberAnimalsPerLevel()
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
                    playingAnimalKey = playingAnimalKey,
                    onTestSound = { animal -> playSound(animal.key, animal.soundRes) },
                    onStopSound = { stopSound() },
                    onToggleCarousel = {
                        expandedLevel = if (isExpanded) null else level
                    },
                    onSelectAnimal = { animal ->
                        selectedAnimals[level] = animal.key
                        AnimalRegistry.setSelectedAnimal(context, level, animal.key)
                    }
                )
            } else {
                SingleAnimalCard(
                    level = level,
                    animal = animals.first(),
                    isPlaying = playingAnimalKey == animals.first().key,
                    onTestSound = { playSound(animals.first().key, animals.first().soundRes) },
                    onStopSound = { stopSound() }
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
    isPlaying: Boolean,
    onTestSound: () -> Unit,
    onStopSound: () -> Unit
) {
    val levelColor = when (level) {
        StarnazzoLevel.LIGHT -> StarnazzoLight
        StarnazzoLevel.MEDIUM -> StarnazzoMedium
        StarnazzoLevel.HEAVY -> DuckTheme.colors.starnazzoHeavy
    }

    Box(modifier = Modifier.padding(horizontal = 20.dp)) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.75f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DuckTheme.colors.cardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            AnimalCardContent(
                animal = animal,
                level = level,
                levelColor = levelColor,
                isPlaying = isPlaying,
                showTryButton = true,
                onTestSound = onTestSound,
                onStopSound = onStopSound
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
    playingAnimalKey: String?,
    onTestSound: (AnimalOption) -> Unit,
    onStopSound: () -> Unit,
    onToggleCarousel: () -> Unit,
    onSelectAnimal: (AnimalOption) -> Unit
) {
    val levelColor = when (level) {
        StarnazzoLevel.LIGHT -> StarnazzoLight
        StarnazzoLevel.MEDIUM -> StarnazzoMedium
        StarnazzoLevel.HEAVY -> DuckTheme.colors.starnazzoHeavy
    }

    val selectedAnimal = animals.find { it.key == selectedKey } ?: animals.first()
    val selectedIndex = animals.indexOf(selectedAnimal)

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

    // Collapsed: carta larga (poco padding). Expanded: si restringe e mostra le vicine.
    val sidePadding by animateDpAsState(
        targetValue = if (isExpanded) 48.dp else 20.dp,
        animationSpec = tween(350),
        label = "sidePadding"
    )
    val spacing by animateDpAsState(
        targetValue = if (isExpanded) 14.dp else 0.dp,
        animationSpec = tween(350),
        label = "spacing"
    )

    HorizontalPager(
        state = pagerState,
        contentPadding = PaddingValues(horizontal = sidePadding),
        pageSpacing = spacing,
        userScrollEnabled = isExpanded,
        modifier = Modifier
            .fillMaxWidth()
    ) { page ->
        val actualIndex = page % animals.size
        val animal = animals[actualIndex]
        val pageOffset = ((pagerState.currentPage - page) +
                pagerState.currentPageOffsetFraction).absoluteValue.coerceIn(0f, 1f)

        val pageScale = lerp(1f, 0.9f, pageOffset)
        val pageAlpha = lerp(1f, 0.6f, pageOffset)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.75f)
                .graphicsLayer {
                    scaleX = pageScale
                    scaleY = pageScale
                    alpha = pageAlpha
                },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DuckTheme.colors.cardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            AnimalCardContent(
                animal = animal,
                level = level,
                levelColor = levelColor,
                isPlaying = playingAnimalKey == animal.key,
                showTryButton = true,
                showChangeButton = true,
                isExpanded = isExpanded,
                onTestSound = { onTestSound(animal) },
                onStopSound = onStopSound,
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
    isPlaying: Boolean = false,
    showTryButton: Boolean = false,
    showChangeButton: Boolean = false,
    isExpanded: Boolean = false,
    onTestSound: () -> Unit = {},
    onStopSound: () -> Unit = {},
    onActionButton: () -> Unit = {}
) {
    val tenueBg = when (level) {
        StarnazzoLevel.LIGHT -> DuckTheme.colors.starnazzoLightTenue
        StarnazzoLevel.MEDIUM -> DuckTheme.colors.starnazzoMediumTenue
        StarnazzoLevel.HEAVY -> DuckTheme.colors.starnazzoHeavyTenue
    }

    Column(modifier = Modifier.fillMaxSize().padding(10.dp)) {
        // Riquadro colorato: chip + barra + animale
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(tenueBg, RoundedCornerShape(12.dp))
        ) {
            // Animale allineato in basso al centro (dietro la chip row)
            val cardRes = AnimalRegistry.findAnimal(animal.key)?.cardRes
            if (cardRes != null) {
                Image(
                    painter = painterResource(cardRes),
                    contentDescription = animal.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 36.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    AnimalEmoji(
                        animalKey = animal.key,
                        emoji = animal.emoji,
                        size = 120.dp,
                        fontSize = 100.sp
                    )
                }
            }

            // Chip row sopra l'immagine (z-order: dopo = sopra)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = CircleShape,
                    color = levelColor
                ) {
                    Text(
                        text = level.displayName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (ThemeState.isDark.value) DuckBrown900 else Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VolumeUp, null, Modifier.size(20.dp), tint = levelColor.copy(alpha = 0.7f))
                    Spacer(modifier = Modifier.width(5.dp))
                    Box(
                        modifier = Modifier
                            .width(44.dp)
                            .height(10.dp)
                            .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(5.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = animal.noisiness)
                                .height(10.dp)
                                .background(levelColor, RoundedCornerShape(5.dp))
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Sezione inferiore: nome, citazione, descrizione
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = animal.name,
                fontSize = 21.sp,
                fontWeight = FontWeight.ExtraBold,
                color = DuckTheme.colors.textPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = animal.quote,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                color = levelColor,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = animal.description,
                fontSize = 12.sp,
                color = DuckTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
                maxLines = 2
            )

            // Bottoni dentro la carta
            if (showTryButton || showChangeButton) {
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (showTryButton) {
                        FilledTonalButton(
                            onClick = if (isPlaying) onStopSound else onTestSound,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = DuckTheme.colors.starnazzoLightTenue,
                                contentColor = StarnazzoLight
                            )
                        ) {
                            Icon(
                                if (isPlaying) Icons.Default.Pause else Icons.Default.VolumeUp,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (isPlaying) stringResource(R.string.customize_btn_stop)
                                else stringResource(R.string.customize_btn_try),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    }
                    if (showTryButton && showChangeButton) {
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                    if (showChangeButton) {
                        FilledTonalButton(
                            onClick = onActionButton,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = DuckTheme.colors.starnazzoMediumTenue,
                                contentColor = DuckOrange500
                            )
                        ) {
                            Icon(
                                if (isExpanded) Icons.Default.Check else Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (isExpanded) stringResource(R.string.customize_btn_done)
                                else stringResource(R.string.customize_btn_change),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

