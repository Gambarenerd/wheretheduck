package com.whereduck.app.ads

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RewardCreditsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PREFS_NAME = "reward_credits"
        private const val KEY_DUCK_CREDITS = "duck_credits"
        const val REWARD_AMOUNT = 5
        const val INITIAL_CREDITS = 5
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _duckCredits = MutableStateFlow(
        prefs.getInt(KEY_DUCK_CREDITS, INITIAL_CREDITS)
    )
    val duckCredits: StateFlow<Int> = _duckCredits.asStateFlow()

    fun grantReward() {
        val newCredits = _duckCredits.value + REWARD_AMOUNT
        prefs.edit().putInt(KEY_DUCK_CREDITS, newCredits).apply()
        _duckCredits.value = newCredits
    }

    fun useDuck(): Boolean {
        val current = _duckCredits.value
        if (current <= 0) return false
        val remaining = current - 1
        prefs.edit().putInt(KEY_DUCK_CREDITS, remaining).apply()
        _duckCredits.value = remaining
        return true
    }

    fun hasDuckCredits(): Boolean = _duckCredits.value > 0
}
