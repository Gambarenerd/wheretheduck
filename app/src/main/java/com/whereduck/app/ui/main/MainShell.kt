package com.whereduck.app.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.whereduck.app.ui.theme.DuckTheme
import kotlinx.coroutines.launch

data class BottomTab(
    val icon: ImageVector,
    val label: String
)

val bottomTabs = listOf(
    BottomTab(Icons.Default.Dashboard, "Dashboard"),
    BottomTab(Icons.Default.People, "Duckers"),
    BottomTab(Icons.Default.MusicNote, "Personalizza"),
    BottomTab(Icons.Default.History, "Cronologia"),
)

// Section titles are now built as AnnotatedString in sectionTitle() below

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainShell(
    onOpenUserMenu: () -> Unit,
    onCreateGroup: () -> Unit,
    dashboardContent: @Composable () -> Unit,
    contactsContent: @Composable (inviteTrigger: Int) -> Unit,
    historyContent: @Composable () -> Unit = {},
    customizeContent: @Composable () -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val scope = rememberCoroutineScope()
    var inviteTrigger by remember { mutableStateOf(0) }

    // Animate background color between sections
    val sectionColors = listOf(
        DuckTheme.colors.sectionDashboard,
        DuckTheme.colors.sectionContacts,
        DuckTheme.colors.sectionDashboard, // Customize uses same bg
        DuckTheme.colors.sectionHistory,
    )
    val bgColor by animateColorAsState(
        targetValue = sectionColors[pagerState.currentPage],
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "bg_color"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        // ── Page content ──
        Column(modifier = Modifier.fillMaxSize()) {
            // Top area: section title + user avatar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 64.dp, bottom = 8.dp)
            ) {
                // Section title (left)
                val highlightColor = DuckTheme.colors.accentDark
                val titleText = when (pagerState.currentPage) {
                    0 -> buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold)) {
                            append("Where ")
                        }
                        withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold, fontStyle = FontStyle.Italic, color = highlightColor)) {
                            append("The Duck")
                        }
                        withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold)) {
                            append("\nAre You?!")
                        }
                    }
                    1 -> buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold)) {
                            append("Hey ")
                        }
                        withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold, fontStyle = FontStyle.Italic, color = highlightColor)) {
                            append("Duckers!")
                        }
                    }
                    2 -> buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold)) {
                            append("Facciamo un po'\ndi ")
                        }
                        withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold, fontStyle = FontStyle.Italic, color = highlightColor)) {
                            append("Casino!")
                        }
                    }
                    else -> buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold)) {
                            append("Duck ")
                        }
                        withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold, fontStyle = FontStyle.Italic, color = highlightColor)) {
                            append("Passati")
                        }
                    }
                }
                Text(
                    text = titleText,
                    fontSize = if (pagerState.currentPage == 0) 22.sp else 28.sp,
                    color = DuckTheme.colors.textPrimary,
                    lineHeight = 24.sp,
                    modifier = Modifier.align(Alignment.CenterStart)
                )

                // User avatar (right) - show profile photo if available
                val context = LocalContext.current
                val prefs = context.getSharedPreferences("settings_prefs", android.content.Context.MODE_PRIVATE)
                val picturePath = prefs.getString("profile_picture_path", null)?.substringBefore("?")
                val hasPhoto = picturePath != null && java.io.File(picturePath).exists()

                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(DuckTheme.colors.accent)
                        .clickable { onOpenUserMenu() }
                        .align(Alignment.TopEnd),
                    contentAlignment = Alignment.Center
                ) {
                    if (hasPhoto) {
                        AsyncImage(
                            model = java.io.File(picturePath!!),
                            contentDescription = "Profilo",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Profilo",
                            tint = DuckTheme.colors.textOnAccent,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            // Pager with fading edges
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        when (page) {
                            0 -> dashboardContent()
                            1 -> contactsContent(inviteTrigger)
                            2 -> customizeContent()
                            3 -> historyContent()
                        }
                    }
                }
            }

            // No spacer — content scrolls behind bottom bar
        }

        // ── Bottom bar ──
        val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        val bottomPadding = maxOf(24.dp, navBarBottom)
        var fabExpanded by remember { mutableStateOf(false) }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = bottomPadding)
                .height(62.dp),
            shape = CircleShape,
            color = DuckTheme.colors.bottomBarBackground,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxHeight(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                bottomTabs.forEachIndexed { index, tab ->
                    val selected = pagerState.currentPage == index
                    val bounceAnim = remember { Animatable(1f) }
                    LaunchedEffect(selected) {
                        if (selected) {
                            bounceAnim.animateTo(
                                targetValue = 1.12f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                )
                            )
                            bounceAnim.animateTo(
                                targetValue = 1f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                )
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .scale(bounceAnim.value)
                            .clip(CircleShape)
                            .background(
                                if (selected) DuckTheme.colors.bottomBarSelected
                                else Color.Transparent
                            )
                            .clickable {
                                scope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.label,
                            tint = if (selected) DuckTheme.colors.sectionTitle
                            else DuckTheme.colors.bottomBarIcon,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // ── FAB area: main button + sub-buttons that pop out ──
        val fabSize = 62.dp
        val subFabSize = 48.dp

        // Animate sub-buttons popping out from the main FAB
        val offsetGroup by animateDpAsState(
            targetValue = if (fabExpanded) -(fabSize + 14.dp) else 0.dp,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "offset_group"
        )
        val offsetPerson by animateDpAsState(
            targetValue = if (fabExpanded) -(fabSize + subFabSize + 24.dp) else 0.dp,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "offset_person"
        )
        val subFabScale by animateFloatAsState(
            targetValue = if (fabExpanded) 1f else 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "sub_fab_scale"
        )

        // Sub-FAB: Add person
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp + (fabSize - subFabSize) / 2, bottom = bottomPadding + (fabSize - subFabSize) / 2)
                .offset(y = offsetPerson)
                .scale(subFabScale)
                .size(subFabSize)
                .clip(CircleShape)
                .background(DuckTheme.colors.bottomBarBackground)
                .clickable(enabled = fabExpanded) {
                    fabExpanded = false
                    inviteTrigger++
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.PersonAdd,
                contentDescription = "Aggiungi persona",
                modifier = Modifier.size(22.dp),
                tint = DuckTheme.colors.textOnAccent
            )
        }

        // Sub-FAB: Add group
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp + (fabSize - subFabSize) / 2, bottom = bottomPadding + (fabSize - subFabSize) / 2)
                .offset(y = offsetGroup)
                .scale(subFabScale)
                .size(subFabSize)
                .clip(CircleShape)
                .background(DuckTheme.colors.bottomBarBackground)
                .clickable(enabled = fabExpanded) {
                    fabExpanded = false
                    onCreateGroup()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.GroupAdd,
                contentDescription = "Crea gruppo",
                modifier = Modifier.size(22.dp),
                tint = DuckTheme.colors.textOnAccent
            )
        }

        // Main FAB (+ / ×) with rotation animation
        val fabRotation by animateFloatAsState(
            targetValue = if (fabExpanded) 135f else 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "fab_rotation"
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = bottomPadding)
                .size(fabSize)
                .clip(CircleShape)
                .background(DuckTheme.colors.buttonPrimary)
                .clickable { fabExpanded = !fabExpanded },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = if (fabExpanded) "Chiudi" else "Aggiungi",
                modifier = Modifier
                    .size(26.dp)
                    .graphicsLayer { rotationZ = fabRotation },
                tint = DuckTheme.colors.textOnButtonPrimary
            )
        }
    }
}
