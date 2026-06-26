package com.whereduck.app.ui.theme

import androidx.compose.ui.graphics.Color

// ═══════════════════════════════════════════════════════
// WhereTheDuck Color System
// Tema "L'Anatra" — bianco, verde, marrone, grigio, giallo
// Un unico set usato per light e dark (per ora)
// ═══════════════════════════════════════════════════════

// ── VERDE (Piumaggio anatra — accenti, azioni positive) ──
val DuckGreen50  = Color(0xFFE8F5E9)
val DuckGreen100 = Color(0xFFC8E6C9)
val DuckGreen200 = Color(0xFFA5D6A7)
val DuckGreen300 = Color(0xFF81C784)
val DuckGreen400 = Color(0xFF66BB6A)
val DuckGreen500 = Color(0xFF4CAF50)  // Base
val DuckGreen600 = Color(0xFF43A047)
val DuckGreen700 = Color(0xFF388E3C)
val DuckGreen800 = Color(0xFF2E7D32)
val DuckGreen900 = Color(0xFF1B5E20)

// ── MARRONE (Terroso — testo, elementi secondari) ──
val DuckBrown50  = Color(0xFFEFEBE9)
val DuckBrown100 = Color(0xFFD7CCC8)
val DuckBrown200 = Color(0xFFBCAAA4)
val DuckBrown300 = Color(0xFFA1887F)
val DuckBrown400 = Color(0xFF8D6E63)
val DuckBrown500 = Color(0xFF795548)  // Base
val DuckBrown600 = Color(0xFF6D4C41)
val DuckBrown700 = Color(0xFF5D4037)
val DuckBrown800 = Color(0xFF4E342E)
val DuckBrown900 = Color(0xFF3E2723)

// ── GIALLO (Becco/zampe — highlight, badge, dettagli speciali) ──
val DuckYellow50  = Color(0xFFFFF8E1)
val DuckYellow100 = Color(0xFFFFECB3)
val DuckYellow200 = Color(0xFFFFE082)
val DuckYellow300 = Color(0xFFFFD54F)
val DuckYellow400 = Color(0xFFFFCA28)
val DuckYellow500 = Color(0xFFFFC107)  // Base
val DuckYellow600 = Color(0xFFFFB300)
val DuckYellow700 = Color(0xFFFFA000)
val DuckYellow800 = Color(0xFFFF8F00)
val DuckYellow900 = Color(0xFFFF6F00)

// ── GRIGIO (Neutro — bordi, testo secondario, sfondi) ──
val DuckGrey50  = Color(0xFFFAFAFA)
val DuckGrey100 = Color(0xFFF5F5F5)
val DuckGrey200 = Color(0xFFEEEEEE)
val DuckGrey300 = Color(0xFFE0E0E0)
val DuckGrey400 = Color(0xFFBDBDBD)
val DuckGrey500 = Color(0xFF9E9E9E)  // Base
val DuckGrey600 = Color(0xFF757575)
val DuckGrey700 = Color(0xFF616161)
val DuckGrey800 = Color(0xFF424242)
val DuckGrey900 = Color(0xFF212121)

// ── ARANCIONE (Grafici, subiti) ──
val DuckOrange500 = Color(0xFFFF9800)  // Base

// ── BIANCO (Superfici) ──
val DuckWhite    = Color(0xFFFFFDF7)  // Caldo, leggermente crema
val DuckWhitePure = Color(0xFFFFFFFF)

// ═══════════════════════════════════════════════════════
// Colori funzionali
// ═══════════════════════════════════════════════════════

// Starnazzo levels
val StarnazzoLight  = Color(0xFF66BB6A)  // Verde — Grillo
val StarnazzoMedium = Color(0xFFFFCA28)  // Giallo — Anatra
val StarnazzoHeavy  = Color(0xFFEF5350)  // Rosso — Oca

// Starnazzo card (versione più chiara per distacco dallo sfondo)
val StarnazzoLightCard  = Color(0xFF8ED091)
val StarnazzoMediumCard = Color(0xFFFFD95A)
val StarnazzoHeavyCard  = Color(0xFFF47B7B)

// Errore / distruttivo
val DuckError    = Color(0xFFD32F2F)
val DuckErrorLight = Color(0xFFEF5350)

// Successo / positivo
val DuckSuccess  = Color(0xFF43A047)

// ═══════════════════════════════════════════════════════
// Componenti specifici
// ═══════════════════════════════════════════════════════

// Bottom bar
val BottomBarBackground = Color(0xFF2C2C2C)
val BottomBarIcon       = Color(0xFFB0B0B0)
val BottomBarSelected   = DuckYellow500

// Custom action button (suoni/personalizzazione)
val CustomizeButtonColor = DuckGreen500

// Chart / grafici
val ChartBarSent     = DuckGreen500
val ChartBarReceived = DuckOrange500
val ChartLabelColor  = DuckBrown400

// VIP
val VipCardBackground = DuckYellow100
val VipHeartColor     = Color(0xFFE53935)

// Add VIP button (subtle shades of dashboard bg DuckBrown50)
val VipAddCircle     = Color(0xFFD7D3CF)  // ~10% darker than DuckBrown50
val VipAddIcon       = Color(0xFFBFBBB7)  // ~20% darker than DuckBrown50

// Pill / motto chip
val PillCoral        = Color(0xFFFFB8A8)  // Corallo saturo — scheda utente
val PillCoralLight   = Color(0xFFFFE0D6)  // Corallo tenue — liste, settings
