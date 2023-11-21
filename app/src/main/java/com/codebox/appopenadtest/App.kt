package com.codebox.appopenadtest

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import timber.log.Timber

class App : Application(), Application.ActivityLifecycleCallbacks, LifecycleObserver {

    private var currentActivity: Activity? = null
    private lateinit var appOpenAdManager: AppOpenAdManager

    /** LifecycleObserver method that shows the app open ad when the app moves to foreground. */
    private var lifecycleEventObserver = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_START) {
            // Show the ad (if available) when the app moves to foreground.
            currentActivity?.let {
                appOpenAdManager.showAdIfAvailable(it)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        registerActivityLifecycleCallbacks(this)

        setupTimber()
        setupMobileAds()

        // Add LifecycleEventObserver
        val lifecycle = ProcessLifecycleOwner.get().lifecycle
        lifecycle.addObserver(lifecycleEventObserver)

        // Initialize AppOpenAdManager
        appOpenAdManager = AppOpenAdManager()
    }

    private fun setupTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            Timber.d("TimberInitializer is initialized.")
        }
    }

    private fun setupMobileAds() {
        // Request AdMob configurations
        val requestConfig = RequestConfiguration.Builder().apply {
            if (BuildConfig.DEBUG) {
                setTestDeviceIds(
                    listOf("5E32F2948B7F6B6FCFD27E68B38D097F")
                )
            }
        }
        MobileAds.setRequestConfiguration(requestConfig.build())

        // Initialize AdMob
        MobileAds.initialize(this) { initializationStatus ->
            initializationStatus.adapterStatusMap.forEach { (adapterClass, status) ->
                Timber.d(
                    "Adapter name: $adapterClass, Description: ${status.description}, Latency: ${status.latency}"
                )
            }
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {
        // Updating the currentActivity only when an ad is not showing.
        if (!appOpenAdManager.isShowingAd) {
            currentActivity = activity
        }
    }

    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}