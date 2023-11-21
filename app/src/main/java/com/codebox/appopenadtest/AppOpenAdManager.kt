package com.codebox.appopenadtest

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import timber.log.Timber
import java.util.Date

class AppOpenAdManager {
    var isShowingAd = false
    private var loadTime: Long = 0
    private var isLoadingAd = false
    private var appOpenAd: AppOpenAd? = null

    /** This is not test id */
    private val AD_UNIT_ID = "ca-app-pub-3738872441062004/4286754084"

    /** Shows the ad if one isn't already showing. */
    fun showAdIfAvailable(activity: Activity) {
        // If the app open ad is already showing, do not show the ad again.
        if (isShowingAd) {
            Timber.d("The app open ad is already showing.")
            return
        }

        // If the app open ad is not available yet, invoke the callback then load the ad.
        if (!isAdAvailable()) {
            Timber.d("The app open ad is not ready yet.")
            loadAd(activity)
            return
        }

        val contentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                // Called when full screen content is dismissed.
                // Set the reference to null so isAdAvailable() returns false.
                Timber.d("Ad dismissed fullscreen content.")
                appOpenAd = null
                isShowingAd = false

                loadAd(activity)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                // Called when fullscreen content failed to show.
                // Set the reference to null so isAdAvailable() returns false.
                Timber.d(adError.message)
                appOpenAd = null
                isShowingAd = false

                loadAd(activity)
            }

            override fun onAdShowedFullScreenContent() {
                // Called when fullscreen content is shown.
                Timber.d("Ad showed fullscreen content.")
            }
        }

        appOpenAd?.fullScreenContentCallback = contentCallback
        isShowingAd = true
        appOpenAd?.show(activity)
    }

    fun loadAd(context: Context) {
        if (isLoadingAd) {
            Timber.d("The app open ad is loading.")
            return
        }

        if (isAdAvailable()) {
            Timber.d("The app open ad available.")
            return
        }

        isLoadingAd = true
        val request = AdRequest.Builder().build()
        val callback = object : AppOpenAd.AppOpenAdLoadCallback() {
            override fun onAdLoaded(ad: AppOpenAd) {
                Timber.d("Ad was loaded.")
                appOpenAd = ad
                isLoadingAd = false
                loadTime = Date().time
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                Timber.d(loadAdError.message)
                isLoadingAd = false
            }
        }

        AppOpenAd.load(
            context, AD_UNIT_ID,
            request, callback
        )
    }

    private fun wasLoadTimeLessThan4HoursAgo(): Boolean {
        val dateDifference: Long = Date().time - loadTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * 4
    }

    private fun isAdAvailable(): Boolean {
        return appOpenAd != null && wasLoadTimeLessThan4HoursAgo()
    }
}