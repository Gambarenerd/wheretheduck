package com.whereduck.app.ads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.whereduck.app.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "AdManager"
    }

    private var rewardedAd: RewardedAd? = null
    private var isLoadingAd = false
    private var retryCount = 0
    private val maxRetries = 3
    private val handler = Handler(Looper.getMainLooper())

    private val _isRewardedAdReady = MutableStateFlow(false)
    val isRewardedAdReady: StateFlow<Boolean> = _isRewardedAdReady.asStateFlow()

    fun createAdRequest(): AdRequest = AdRequest.Builder().build()

    fun preloadRewardedAd() {
        retryCount = 0
        handler.removeCallbacksAndMessages(null)
        loadAdInternal()
    }

    private fun loadAdInternal() {
        if (rewardedAd != null || isLoadingAd) return

        isLoadingAd = true
        RewardedAd.load(
            context,
            BuildConfig.ADMOB_REWARDED_ID,
            createAdRequest(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "Rewarded ad loaded")
                    rewardedAd = ad
                    isLoadingAd = false
                    retryCount = 0
                    _isRewardedAdReady.value = true
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "Ad failed to load: ${error.message} (attempt ${retryCount + 1}/$maxRetries)")
                    rewardedAd = null
                    isLoadingAd = false
                    _isRewardedAdReady.value = false

                    if (retryCount < maxRetries) {
                        val delayMs = (retryCount + 1) * 5_000L
                        retryCount++
                        handler.postDelayed({ loadAdInternal() }, delayMs)
                    }
                }
            }
        )
    }

    fun showRewardedAd(activity: Activity, onRewardEarned: () -> Unit, onAdNotReady: (() -> Unit)? = null) {
        val ad = rewardedAd
        if (ad == null) {
            onAdNotReady?.invoke()
            preloadRewardedAd()
            return
        }

        ad.show(activity) {
            onRewardEarned()
        }
        rewardedAd = null
        _isRewardedAdReady.value = false
        preloadRewardedAd()
    }
}
