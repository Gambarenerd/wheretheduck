package com.whereduck.app.ads

import android.app.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.whereduck.app.R
import com.whereduck.app.ui.theme.DuckTheme

@Composable
fun OutOfDucksDialog(
    adManager: AdManager,
    creditsManager: RewardCreditsManager,
    onDismiss: () -> Unit
) {
    val isAdReady by adManager.isRewardedAdReady.collectAsState()
    val activity = LocalContext.current as? Activity

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.out_of_ducks_title),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.out_of_ducks_icon),
                    fontSize = 48.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.out_of_ducks_body),
                    textAlign = TextAlign.Center,
                    color = DuckTheme.colors.textSecondary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    activity?.let { act ->
                        adManager.showRewardedAd(
                            activity = act,
                            onRewardEarned = {
                                creditsManager.grantReward()
                                onDismiss()
                            }
                        )
                    }
                },
                enabled = isAdReady,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DuckTheme.colors.buttonPrimary,
                    contentColor = DuckTheme.colors.textOnButtonPrimary
                )
            ) {
                Text(
                    text = stringResource(R.string.out_of_ducks_watch),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}
