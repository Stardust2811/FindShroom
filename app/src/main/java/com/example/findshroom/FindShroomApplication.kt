package com.example.findshroom

import android.app.Application
import com.yandex.mapkit.MapKitFactory
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FindShroomApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Yandex MapKit is configured with API key from BuildConfig.MAPKIT_API_KEY
        MapKitFactory.setApiKey(BuildConfig.MAPKIT_API_KEY)
        MapKitFactory.initialize(this)
    }
}
