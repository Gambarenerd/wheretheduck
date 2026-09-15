package com.whereduck.app.ui.theme

import android.app.Activity
import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
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

    // Starnazzo tenue (sfondo cerchio animale)
    val starnazzoLightTenue: Color,
    val starnazzoMediumTenue: Color,
    val starnazzoHeavyTenue: Color,
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
        starnazzoLightTenue = Color.Unspecified,
        starnazzoMediumTenue = Color.Unspecified,
        starnazzoHeavyTenue = Color.Unspecified,
    )
}

// ── LIGHT ──
private val LightColors = DuckCustomColors(
    // Sfondo
    appBackground = DuckWhite,
    cardBackground = DuckWhitePure,
    cardBackgroundVariant = DuckGrey100,

    // Testo
    textPrimary = DuckBrown900,
    textSecondary = DuckGrey600,
    textOnAccent = DuckBrown900,

    // Accenti
    accent = DuckYellow500,
    accentLight = DuckYellow200,
    accentDark = DuckYellow700,

    // Azioni
    positive = StarnazzoLight,
    negative = DuckError,
    warning = DuckYellow700,

    // Bottom bar
    bottomBarBackground = BottomBarBackground,
    bottomBarIcon = BottomBarIcon,
    bottomBarSelected = BottomBarSelected,

    // Personalizzazione button
    customizeButton = StarnazzoLight,

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
    chartBarSent = StarnazzoLight,
    chartBarReceived = DuckOrange500,
    chartLabel = DuckBrown400,

    // Semantica UI
    buttonPrimary = StarnazzoLight,
    highlight = DuckYellow500,

    // VIP
    vipCardBackground = DuckYellow100,
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

    // Starnazzo tenue
    starnazzoLightTenue = StarnazzoLightTenue,
    starnazzoMediumTenue = StarnazzoMediumTenue,
    starnazzoHeavyTenue = StarnazzoHeavyTenue,
)

// ── DARK ──
// Palette calda: superfici marrone scuro, testo crema caldo
// Contrasti verificati WCAG AA:
//   textPrimary (#EDE5DC) su DarkSurface (#2C2421) → ~8.5:1
//   textPrimary (#EDE5DC) su DarkCard (#382F2B)    → ~6.5:1
//   textSecondary (#A89890) su DarkSurface          → ~4.5:1
//   textSecondary (#A89890) su DarkCard             → ~3.5:1 (large text OK)
private val DarkColors = DuckCustomColors(
    // Sfondo
    appBackground = DarkSurface,
    cardBackground = DarkCard,
    cardBackgroundVariant = DarkCardVariant,

    // Testo
    textPrimary = Color(0xFFEDE5DC),    // Crema caldo — alto contrasto
    textSecondary = Color(0xFFA89890),  // Marrone chiaro smorzato
    textOnAccent = DuckBrown900,         // Marrone scuro su arancione

    // Accenti — arancione chiaro caldo nel dark
    accent = Color(0xFFFFB74D),
    accentLight = Color(0xFFE68A00),
    accentDark = Color(0xFFFFB74D),

    // Azioni
    positive = StarnazzoLight,
    negative = DuckError,
    warning = Color(0xFFFFB74D),

    // Bottom bar
    bottomBarBackground = Color(0xFF261F1C),  // Scura, stacca dallo sfondo
    bottomBarIcon = Color(0xFF8A7F79),        // Marrone smorzato
    bottomBarSelected = Color(0xFFFFB74D),

    // Personalizzazione button
    customizeButton = StarnazzoLight,

    // Starnazzo
    starnazzoLight = StarnazzoLight,
    starnazzoMedium = StarnazzoMedium,
    starnazzoHeavy = Color(0xFFE57373),  // Rosso salmone caldo per dark

    // Bordi e divisori
    outline = DarkElevated,              // #3A3331 — caldo
    divider = DarkCardVariant,           // #302A28 — caldo

    // Section backgrounds
    sectionDashboard = DarkSurface,
    sectionContacts = DarkSurface,
    sectionHistory = DarkSurface,

    // Titoli sezione
    sectionTitle = Color(0xFFD7C8BE),    // Beige chiaro, dalla famiglia DuckBrown

    // Chart / grafici
    chartBarSent = StarnazzoLight,
    chartBarReceived = DuckOrange500,
    chartLabel = Color(0xFFA89890),       // Coerente con textSecondary

    // Semantica UI
    buttonPrimary = StarnazzoLight,
    highlight = Color(0xFFFFB74D),

    // VIP
    vipCardBackground = Color(0xFF3D2A15), // Arancione scuro caldo
    vipHeart = Color(0xFFE57373),          // Rosso salmone caldo, coerente col dark
    vipAddCircle = DarkElevated,           // #3A3331
    vipAddIcon = Color(0xFF8A7F79),        // Marrone smorzato

    // Bottoni — testo/icone
    textOnButtonPrimary = DuckWhitePure,

    // Stati disabilitati
    disabledBackground = DarkCardVariant,  // #302A28
    disabledContent = Color(0xFF6A605A),   // Marrone spento

    // Overlay
    scrim = Color(0x993E3530),             // Stesso marrone del surface, 60%

    // Input fields
    inputBackground = DarkCard,
    inputBorder = Color(0xFF564E4A),       // Marrone medio

    // Pill
    pillBackground = Color(0xFF564038),    // Marrone-corallo
    pillBackgroundLight = Color(0xFF4A352D), // Leggermente più scuro

    // Starnazzo tenue
    starnazzoLightTenue = StarnazzoLightTenueDark,
    starnazzoMediumTenue = StarnazzoMediumTenueDark,
    starnazzoHeavyTenue = StarnazzoHeavyTenueDark,
)

// Material3 scheme — Light
private val LightMaterialScheme = lightColorScheme(
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

// Material3 scheme — Dark (arancione chiaro come accento)
private val DarkMaterialScheme = darkColorScheme(
    primary = Color(0xFFFFB74D),
    onPrimary = DarkSurface,
    primaryContainer = Color(0xFF3D2A15),
    onPrimaryContainer = Color(0xFFFFB74D),
    secondary = Color(0xFFFFB74D),
    onSecondary = DarkSurface,
    secondaryContainer = Color(0xFF3D2A15),
    onSecondaryContainer = Color(0xFFFFB74D),
    tertiary = DuckBrown200,
    onTertiary = DarkSurface,
    background = DarkSurface,
    onBackground = Color(0xFFEDE5DC),
    surface = DarkCard,
    onSurface = Color(0xFFEDE5DC),
    surfaceVariant = DarkCardVariant,
    onSurfaceVariant = Color(0xFFA89890),
    error = DuckError,
    onError = DuckWhitePure,
    outline = Color(0xFF564E4A),
    outlineVariant = DarkCardVariant,
)

// ═══════════════════════════════════════════════════════
// Theme accessor
// ═══════════════════════════════════════════════════════

object DuckTheme {
    val colors: DuckCustomColors
        @Composable
        get() = LocalDuckColors.current
}

object ThemeState {
    var isDark: MutableState<Boolean> = mutableStateOf(false)

    fun init(context: Context) {
        val prefs = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
        isDark.value = prefs.getString("theme", "LIGHT") == "DARK"
    }
}

@Composable
fun WhereTheDuckTheme(
    darkTheme: Boolean = ThemeState.isDark.value,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    val materialScheme = if (darkTheme) DarkMaterialScheme else LightMaterialScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colors.appBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalDuckColors provides colors) {
        MaterialTheme(
            colorScheme = materialScheme,
            typography = Typography,
            content = content
        )
    }
}
