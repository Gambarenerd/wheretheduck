package com.whereduck.app.data.model

import com.whereduck.app.R

enum class StarnazzoLevel(
    val key: String,
    val displayName: String,
    val emoji: String,
    val animalName: String,
    val defaultAnimal: String,
    val vibrationPattern: LongArray,
    val toneFrequency: Int,
    val soundRes: Int? = null
) {
    LIGHT("light", "Leggero", "\uD83E\uDD97", "Grillo", "cricket",
        longArrayOf(0, 200, 100, 200, 100, 200), 2000),
    MEDIUM("medium", "Medio", "\uD83E\uDD86", "Anatra", "duck",
        longArrayOf(0, 400, 200, 400, 200, 400), 1000,
        soundRes = R.raw.magiaz_duck_405695),
    HEAVY("heavy", "Pesante", "\uD83E\uDEB3", "Oca Arrabbiata", "goose",
        longArrayOf(0, 800, 200, 800, 200, 800), 500);

    companion object {
        fun fromKey(key: String): StarnazzoLevel {
            return entries.find { it.key == key } ?: MEDIUM
        }
    }
}
