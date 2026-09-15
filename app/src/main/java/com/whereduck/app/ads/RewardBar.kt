package com.whereduck.app.ads

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.whereduck.app.R
import com.whereduck.app.ui.theme.DuckTheme

@Composable
fun RewardBar(
    adManager: AdManager,
    creditsManager: RewardCreditsManager,
    modifier: Modifier = Modifier
) {
    val credits by creditsManager.duckCredits.collectAsState()
    val isAdReady by adManager.isRewardedAdReady.collectAsState()
    val activity = LocalContext.current as? Activity

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = DuckTheme.colors.cardBackground
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.reward_duck_icon),
                        fontSize = 28.sp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.reward_ducks_available),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = DuckTheme.colors.sectionTitle
                        )
                        Text(
                            text = stringResource(R.string.reward_counter, credits),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = DuckTheme.colors.accentDark
                        )
                    }
                }

                Button(
                    onClick = {
                        activity?.let { act ->
                            adManager.showRewardedAd(
                                activity = act,
                                onRewardEarned = { creditsManager.grantReward() }
                            )
                        }
                    },
                    enabled = isAdReady,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DuckTheme.colors.buttonPrimary,
                        contentColor = DuckTheme.colors.textOnButtonPrimary,
                        disabledContainerColor = DuckTheme.colors.disabledBackground,
                        disabledContentColor = DuckTheme.colors.disabledContent
                    )
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.reward_watch_ad),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

        }
    }
}
