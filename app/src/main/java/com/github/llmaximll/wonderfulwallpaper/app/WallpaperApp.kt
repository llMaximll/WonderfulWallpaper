package com.github.llmaximll.wonderfulwallpaper.app

import android.app.Application
import com.github.llmaximll.wonderfulwallpaper.BuildConfig
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class WallpaperApp : Application() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}