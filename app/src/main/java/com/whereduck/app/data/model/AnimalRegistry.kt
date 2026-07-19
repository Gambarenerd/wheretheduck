package com.whereduck.app.data.model

import android.content.Context
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.whereduck.app.R

data class AnimalInfo(
    val key: String,
    val emoji: String,
    val name: String,
    val soundRes: Int? = null,
    val drawableRes: Int? = null,
    val topPadding: Dp = 0.dp
)

/**
 * Central registry of all animals available in this app version.
 * Used by both sender (to send animalType) and receiver (to resolve sound/emoji).
 */
object AnimalRegistry {

    private val allAnimals: Map<String, AnimalInfo> = mapOf(
        "cricket" to AnimalInfo("cricket", "\uD83E\uDD97", "Grillo", R.raw.cricket),
        "mosquito" to AnimalInfo("mosquito", "\uD83E\uDD9F", "Zanzara", R.raw.mosquito),
        "robot" to AnimalInfo("robot", "\uD83E\uDD16", "Robot", R.raw.robot, R.drawable.roboto, 35.dp),
        "duck" to AnimalInfo("duck", "\uD83E\uDD86", "Anatra", R.raw.magiaz_duck_405695, R.drawable.duck_icon),
        "goat" to AnimalInfo("goat", "\uD83D\uDC10", "Capra Pazza", R.raw.goat),
        "godzilla" to AnimalInfo("godzilla", "\uD83E\uDD96", "Godzilla", R.raw.godzilla),
    )

    /** Look up an animal by key. Returns null if not found (e.g. newer version animal). */
    fun findAnimal(key: String): AnimalInfo? = allAnimals[key]

    /** Get the sound resource for an animal, with fallback to level default. */
    fun getSoundRes(animalKey: String, level: StarnazzoLevel): Int? {
        return findAnimal(animalKey)?.soundRes ?: level.soundRes
    }

    /** Get the emoji for an animal, with fallback to level default. */
    fun getEmoji(animalKey: String, level: StarnazzoLevel): String {
        return findAnimal(animalKey)?.emoji ?: level.emoji
    }

    // ── Persistence: selected animal per level ──

    private const val PREFS_NAME = "animal_prefs"

    fun getSelectedAnimal(context: Context, level: StarnazzoLevel): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString("animal_${level.key}", level.defaultAnimal) ?: level.defaultAnimal
    }

    fun setSelectedAnimal(context: Context, level: StarnazzoLevel, animalKey: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString("animal_${level.key}", animalKey).apply()
    }
}
