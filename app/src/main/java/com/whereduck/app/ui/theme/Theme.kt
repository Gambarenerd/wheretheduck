package com.whereduck.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ═══════════════════════════════════════════════════════
// Custom Color System (come CiboHero)
// ═══════════════════════════════════════════════════════

@Immutable
data class DuckCustomColors(
    // Sfondo
    val appBackground: Color,
    val cardBackground: Color,
    val cardBackgroundVariant: Color,

    // Testo
    val textPrimary: Color,
    val textSecondary: Color,
    val textOnAccent: Color,

    // Accenti
    val accent: Color,
    val accentLight: Color,
    val accentDark: Color,

    // Azioni
    val positive: Color,
    val negative: Color,
    val warning: Color,

    // Bottom bar
    val bottomBarBackground: Color,
    val bottomBarIcon: Color,
    val bottomBarSelected: Color,

    // Personalizzazione button
    val customizeButton: Color,

    // Starnazzo
    val starnazzoLight: Color,
    val starnazzoMedium: Color,
    val starnazzoHeavy: Color,

    // Bordi e divisori
    val outline: Color,
    val divider: Color,

    // Section backgrounds (per cambio sfondo tra sezioni)
    val sectionDashboard: Color,
    val sectionContacts: Color,
    val sectionHistory: Color,

    // Titoli sezione
    val sectionTitle: Color,

    // Chart / grafici
    val chartBarSent: Color,
    val chartBarReceived: Color,
    val chartLabel: Color,

    // Semantica UI
    val buttonPrimary: Color,
    val highlight: Color,

    // VIP
    val vipCardBackground: Color,
    val vipHeart: Color,
    val vipAddCircle: Color,
    val vipAddIcon: Color,

    // Bottoni — testo/icone
    val textOnButtonPrimary: Color,

    // Stati disabilitati
    val disabledBackground: Color,
    val disabledContent: Color,

    // Overlay
    val scrim: Color,

    // Input fields
    val inputBackground: Color,
    val inputBorder: Color,

    // Pill (motto, tag)
    val pillBackground: Color,
    val pillBackgroundLight: Color,
)

val LocalDuckColors = staticCompositionLocalOf {
    DuckCustomColors(
        appBackground = Color.Unspecified,
        cardBackground = Color.Unspecified,
        cardBackgroundVariant = Color.Unspecified,
        textPrimary = Color.Unspecified,
        textSecondary = Color.Unspecified,
        textOnAccent = Color.Unspecified,
        accent = Color.Unspecified,
        accentLight = Color.Unspecified,
        accentDark = Color.Unspecified,
        positive = Color.Unspecified,
        negative = Color.Unspecified,
        warning = Color.Unspecified,
        bottomBarBackground = Color.Unspecified,
        bottomBarIcon = Color.Unspecified,
        bottomBarSelected = Color.Unspecified,
        customizeButton = Color.Unspecified,
        starnazzoLight = Color.Unspecified,
        starnazzoMedium = Color.Unspecified,
        starnazzoHeavy = Color.Unspecified,
        outline = Color.Unspecified,
        divider = Color.Unspecified,
        sectionDashboard = Color.Unspecified,
        sectionContacts = Color.Unspecified,
        sectionHistory = Color.Unspecified,
        sectionTitle = Color.Unspecified,
        chartBarSent = Color.Unspecified,
        chartBarReceived = Color.Unspecified,
        chartLabel = Color.Unspecified,
        buttonPrimary = Color.Unspecified,
        highlight = Color.Unspecified,
        vipCardBackground = Color.Unspecified,
        vipHeart = Color.Unspecified,
        vipAddCircle = Color.Unspecified,
        vipAddIcon = Color.Unspecified,
        textOnButtonPrimary = Color.Unspecified,
        disabledBackground = Color.Unspecified,
        disabledContent = Color.Unspecified,
        scrim = Color.Unspecified,
        inputBackground = Color.Unspecified,
        inputBorder = Color.Unspecified,
        pillBackground = Color.Unspecified,
        pillBackgroundLight = Color.Unspecified,
    )
}

// Un unico set per ora (light e dark identici, separati in futuro)
private val AppColors = DuckCustomColors(
    // Sfondo
    appBackground = DuckWhite,
    cardBackground = DuckWhitePure,
    cardBackgroundVariant = DuckGrey100,

    // Testo
    textPrimary = DuckBrown900,
    textSecondary = DuckGrey600,
    textOnAccent = DuckWhitePure,

    // Accenti
    accent = DuckYellow500,
    accentLight = DuckYellow200,
    accentDark = DuckYellow700,

    // Azioni
    positive = DuckGreen500,
    negative = DuckError,
    warning = DuckYellow700,

    // Bottom bar
    bottomBarBackground = BottomBarBackground,
    bottomBarIcon = BottomBarIcon,
    bottomBarSelected = BottomBarSelected,

    // Personalizzazione button
    customizeButton = CustomizeButtonColor,

    // Starnazzo
    starnazzoLight = StarnazzoLight,
    starnazzoMedium = StarnazzoMedium,
    starnazzoHeavy = StarnazzoHeavy,

    // Bordi e divisori
    outline = DuckGrey300,
    divider = DuckGrey200,

    // Section backgrounds
    sectionDashboard = DuckBrown50,
    sectionContacts = DuckBrown50,
    sectionHistory = DuckBrown50,

    // Titoli sezione
    sectionTitle = DuckBrown700,

    // Chart / grafici
    chartBarSent = ChartBarSent,
    chartBarReceived = ChartBarReceived,
    chartLabel = ChartLabelColor,

    // Semantica UI
    buttonPrimary = DuckGreen500,
    highlight = DuckYellow500,

    // VIP
    vipCardBackground = VipCardBackground,
    vipHeart = VipHeartColor,
    vipAddCircle = VipAddCircle,
    vipAddIcon = VipAddIcon,

    // Bottoni — testo/icone
    textOnButtonPrimary = DuckWhitePure,

    // Stati disabilitati
    disabledBackground = DuckGrey200,
    disabledContent = DuckGrey400,

    // Overlay
    scrim = Color(0x66523A2E),  // DuckBrown900-ish al 40%

    // Input fields
    inputBackground = DuckWhitePure,
    inputBorder = DuckBrown200,

    // Pill
    pillBackground = PillCoral,
    pillBackgroundLight = PillCoralLight,
)

// Material3 scheme (necessario per componenti Material)
private val AppMaterialScheme = lightColorScheme(
    primary = DuckGreen600,
    onPrimary = DuckWhitePure,
    primaryContainer = DuckGreen100,
    onPrimaryContainer = DuckGreen900,
    secondary = DuckYellow500,
    onSecondary = DuckBrown900,
    secondaryContainer = DuckYellow100,
    onSecondaryContainer = DuckBrown900,
    tertiary = DuckBrown400,
    onTertiary = DuckWhitePure,
    background = DuckWhite,
    onBackground = DuckBrown900,
    surface = DuckWhitePure,
    onSurface = DuckBrown900,
    surfaceVariant = DuckGrey100,
    onSurfaceVariant = DuckGrey700,
    error = DuckError,
    onError = DuckWhitePure,
    outline = DuckBrown200,
    outlineVariant = DuckGrey200,
)

// ═══════════════════════════════════════════════════════
// Theme accessor
// ═══════════════════════════════════════════════════════

object DuckTheme {
    val colors: DuckCustomColors
        @Composable
        get() = LocalDuckColors.current
}

@Composable
fun WhereTheDuckTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    CompositionLocalProvider(LocalDuckColors provides AppColors) {
        MaterialTheme(
            colorScheme = AppMaterialScheme,
            typography = Typography,
            content = content
        )
    }
}
