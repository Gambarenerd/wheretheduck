package com.whereduck.app.ui.theme

import androidx.compose.ui.graphics.Color

// ═══════════════════════════════════════════════════════
// WhereTheDuck Color System
// Unico file colori — usato da Theme.kt per light e dark
// ═══════════════════════════════════════════════════════

// ── VERDE (Piumaggio anatra — accenti, azioni positive) ──
val DuckGreen100 = Color(0xFFC8E6C9)
val DuckGreen600 = Color(0xFF43A047)
val DuckGreen900 = Color(0xFF1B5E20)

// ── MARRONE (Terroso — testo, elementi secondari) ──
val DuckBrown50  = Color(0xFFEFEBE9)
val DuckBrown100 = Color(0xFFD7CCC8)
val DuckBrown200 = Color(0xFFBCAAA4)
val DuckBrown400 = Color(0xFF8D6E63)
val DuckBrown700 = Color(0xFF5D4037)
val DuckBrown900 = Color(0xFF3E2723)

// ── GIALLO (Becco/zampe — highlight, badge, dettagli speciali) ──
val DuckYellow100 = Color(0xFFFFECB3)
val DuckYellow200 = Color(0xFFFFE082)
val DuckYellow500 = Color(0xFFFFC107)  // Base
val DuckYellow700 = Color(0xFFFFA000)

// ── GRIGIO (Neutro — bordi, testo secondario, sfondi) ──
val DuckGrey100 = Color(0xFFF5F5F5)
val DuckGrey200 = Color(0xFFEEEEEE)
val DuckGrey300 = Color(0xFFE0E0E0)
val DuckGrey400 = Color(0xFFBDBDBD)
val DuckGrey600 = Color(0xFF757575)
val DuckGrey700 = Color(0xFF616161)

// ── ARANCIONE (Grafici, subiti) ──
val DuckOrange500 = Color(0xFFFF9800)

// ── BIANCO (Superfici) ──
val DuckWhite    = Color(0xFFFFFDF7)  // Caldo, leggermente crema
val DuckWhitePure = Color(0xFFFFFFFF)

// ═══════════════════════════════════════════════════════
// Colori funzionali
// ═══════════════════════════════════════════════════════

// Livelli Duck
val StarnazzoLight  = Color(0xFF60A700)  // Verde — Grillo
val StarnazzoMedium = Color(0xFFFFCA28)  // Giallo — Anatra
val StarnazzoHeavy  = Color(0xFFC62828)  // Rosso mattone — Capra

// Livelli Duck card (versione piu' chiara per distacco dallo sfondo)
val StarnazzoLightCard  = Color(0xFF88C240)
val StarnazzoMediumCard = Color(0xFFFFD95A)
val StarnazzoHeavyCard  = Color(0xFFD84B4B)

// Livelli Duck tenue (sfondo animale, card chiamata)
val StarnazzoLightTenue  = Color(0xFFDFEDCC)
val StarnazzoMediumTenue = Color(0xFFFFF3CD)
val StarnazzoHeavyTenue  = Color(0xFFF4D4D4)

// Errore / distruttivo
val DuckError = Color(0xFFD32F2F)

// ═══════════════════════════════════════════════════════
// Componenti specifici
// ═══════════════════════════════════════════════════════

// Bottom bar
val BottomBarBackground = Color(0xFF2C2C2C)
val BottomBarIcon       = Color(0xFFB0B0B0)
val BottomBarSelected   = DuckYellow500

// VIP
val VipHeartColor     = Color(0xFFE53935)
val VipAddCircle      = Color(0xFFD7D3CF)
val VipAddIcon        = Color(0xFFBFBBB7)

// Pill / motto chip
val PillCoral      = Color(0xFFFFB8A8)
val PillCoralLight = Color(0xFFFFE0D6)
