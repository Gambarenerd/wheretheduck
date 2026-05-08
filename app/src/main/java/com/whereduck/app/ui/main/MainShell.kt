package com.whereduck.app.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
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
    BottomTab(Icons.Default.History, "Cronologia"),
)

val sectionTitles = listOf(
    "Where The Duck\nAre You?",
    "Duckers",
    "Cronologia",
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainShell(
    onOpenUserMenu: () -> Unit,
    onOpenCustomize: () -> Unit,
    onCreateGroup: () -> Unit,
    dashboardContent: @Composable () -> Unit,
    contactsContent: @Composable () -> Unit,
    historyContent: @Composable () -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    // Animate background color between sections
    val sectionColors = listOf(
        DuckTheme.colors.sectionDashboard,
        DuckTheme.colors.sectionContacts,
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
                    .padding(start = 20.dp, end = 20.dp, top = 48.dp, bottom = 8.dp)
            ) {
                // Section title (left)
                Text(
                    text = sectionTitles[pagerState.currentPage],
                    fontSize = if (pagerState.currentPage == 0) 22.sp else 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DuckTheme.colors.textPrimary,
                    lineHeight = 32.sp,
                    modifier = Modifier.align(Alignment.CenterStart)
                )

                // User avatar (right)
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(DuckTheme.colors.accent)
                        .clickable { onOpenUserMenu() }
                        .align(Alignment.TopEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Profilo",
                        tint = DuckTheme.colors.textOnAccent,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                Box(modifier = Modifier.fillMaxSize()) {
                    when (page) {
                        0 -> dashboardContent()
                        1 -> contactsContent()
                        2 -> historyContent()
                    }
                }
            }

            // Spacer for bottom bar
            Spacer(modifier = Modifier.height(80.dp))
        }

        // ── Create group FAB (only on Duckers tab) ──
        AnimatedVisibility(
            visible = pagerState.currentPage == 1,
            enter = scaleIn(),
            exit = scaleOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 96.dp)
        ) {
            FloatingActionButton(
                onClick = onCreateGroup,
                modifier = Modifier.size(62.dp),
                shape = CircleShape,
                containerColor = DuckTheme.colors.accent,
                contentColor = DuckTheme.colors.textOnAccent,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 0.dp, pressedElevation = 0.dp, hoveredElevation = 0.dp, focusedElevation = 0.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Crea gruppo",
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        // ── Bottom bar (centered, wraps content like CiboHero) ──
        val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        val bottomPadding = maxOf(24.dp, navBarBottom)

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
                    val scale by animateFloatAsState(
                        targetValue = if (selected) 1.15f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "icon_scale_$index"
                    )

                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .scale(scale)
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
                            tint = if (selected) Color.Black
                            else DuckTheme.colors.bottomBarIcon,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // ── Customize button (bottom-right, separate) ──
        FloatingActionButton(
            onClick = onOpenCustomize,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = bottomPadding)
                .size(62.dp),
            shape = CircleShape,
            containerColor = DuckTheme.colors.customizeButton,
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 0.dp, pressedElevation = 0.dp, hoveredElevation = 0.dp, focusedElevation = 0.dp
            )
        ) {
            Icon(
                Icons.Default.MusicNote,
                contentDescription = "Personalizza",
                modifier = Modifier.size(26.dp)
            )
        }
    }
}
