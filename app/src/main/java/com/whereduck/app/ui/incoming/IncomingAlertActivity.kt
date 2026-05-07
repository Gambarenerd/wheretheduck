package com.whereduck.app.ui.incoming

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.whereduck.app.data.model.StarnazzoLevel
import com.whereduck.app.service.StarnazzoSoundService
import com.whereduck.app.ui.theme.WhereTheDuckTheme

class IncomingAlertActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setShowWhenLocked(true)
        setTurnScreenOn(true)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        )

        val fromName = intent.getStringExtra("fromName") ?: "Qualcuno"
        val levelKey = intent.getStringExtra("level") ?: "medium"
        val level = StarnazzoLevel.fromKey(levelKey)

        setContent {
            WhereTheDuckTheme {
                IncomingAlertScreen(
                    fromName = fromName,
                    level = level,
                    onDismiss = { silenceAndFinish() }
                )
            }
        }
    }

    private fun silenceAndFinish() {
        val stopIntent = Intent(this, StarnazzoSoundService::class.java).apply {
            action = StarnazzoSoundService.ACTION_STOP
        }
        startService(stopIntent)
        finish()
    }

    @Deprecated("Use OnBackPressedDispatcher")
    override fun onBackPressed() {
        silenceAndFinish()
        @Suppress("DEPRECATION")
        super.onBackPressed()
    }
}
