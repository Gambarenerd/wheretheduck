package com.whereduck.app.ui.premium

import androidx.lifecycle.ViewModel
import com.whereduck.app.billing.BillingManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TierViewModel @Inject constructor(
    val billingManager: BillingManager
) : ViewModel()
