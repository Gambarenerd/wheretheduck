package com.whereduck.app.ui.premium

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.whereduck.app.R
import com.whereduck.app.billing.BillingManager
import com.whereduck.app.ui.settings.SettingsViewModel
import com.whereduck.app.ui.theme.DuckTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TierScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
    billingManager: BillingManager = hiltViewModel<TierViewModel>().billingManager
) {
    val uiState by viewModel.uiState.collectAsState()
    val isPremium = uiState.currentTier == "premium"
    val billingLoading by billingManager.isLoading.collectAsState()
    val billingError by billingManager.purchaseError.collectAsState()
    val productDetails by billingManager.productDetails.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val formattedPrice = billingManager.getFormattedPrice()

    LaunchedEffect(billingError) {
        billingError?.let {
            snackbarHostState.showSnackbar(it)
            billingManager.clearError()
        }
    }

    Scaffold(
        containerColor = DuckTheme.colors.sectionDashboard,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.plans_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.common_back))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // ── Free Plan Card ──
            PlanCard(
                planName = stringResource(R.string.plans_free_name),
                icon = Icons.Default.WorkspacePremium,
                iconColor = DuckTheme.colors.textSecondary,
                isCurrentPlan = !isPremium,
                features = listOf(
                    PlanFeature(stringResource(R.string.tier_free_ducks), true),
                    PlanFeature(stringResource(R.string.tier_free_reward), true),
                    PlanFeature(stringResource(R.string.tier_free_ads), false),
                    PlanFeature(stringResource(R.string.tier_free_unlimited), false)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Premium Plan Card ──
            PlanCard(
                planName = stringResource(R.string.plans_premium_name),
                icon = Icons.Default.Star,
                iconColor = DuckTheme.colors.accent,
                isCurrentPlan = isPremium,
                isPremiumCard = true,
                features = listOf(
                    PlanFeature(stringResource(R.string.tier_premium_unlimited), true),
                    PlanFeature(stringResource(R.string.tier_premium_no_ads), true),
                    PlanFeature(stringResource(R.string.tier_premium_support), true)
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── Action Button ──
            if (!isPremium) {
                Button(
                    onClick = {
                        val activity = context as? Activity
                        if (activity != null) {
                            billingManager.launchPurchaseFlow(activity)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    enabled = productDetails != null && !billingLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DuckTheme.colors.buttonPrimary,
                        contentColor = DuckTheme.colors.textOnButtonPrimary
                    )
                ) {
                    if (billingLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = DuckTheme.colors.textOnButtonPrimary
                        )
                    } else {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (formattedPrice != null)
                            "${stringResource(R.string.plans_upgrade)} — $formattedPrice"
                        else
                            stringResource(R.string.plans_upgrade),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            } else {
                OutlinedButton(
                    onClick = {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/account/subscriptions")
                        )
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Text(
                        text = stringResource(R.string.plans_manage),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

private data class PlanFeature(
    val text: String,
    val included: Boolean
)

@Composable
private fun PlanCard(
    planName: String,
    icon: ImageVector,
    iconColor: Color,
    isCurrentPlan: Boolean,
    isPremiumCard: Boolean = false,
    features: List<PlanFeature>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPremiumCard && isCurrentPlan)
                DuckTheme.colors.accent.copy(alpha = 0.1f)
            else DuckTheme.colors.cardBackground
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = planName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = DuckTheme.colors.textPrimary
                    )
                }
                if (isCurrentPlan) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = DuckTheme.colors.accent
                    ) {
                        Text(
                            text = stringResource(R.string.plans_current),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = DuckTheme.colors.textOnAccent
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            features.forEach { feature ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (feature.included) Icons.Default.Check else Icons.Default.Block,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = if (feature.included) DuckTheme.colors.positive
                               else DuckTheme.colors.textSecondary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = feature.text,
                        fontSize = 14.sp,
                        color = if (feature.included) DuckTheme.colors.textPrimary
                                else DuckTheme.colors.textSecondary
                    )
                }
            }
        }
    }
}
