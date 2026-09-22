package com.crosslens.app

import android.app.Application
import com.crosslens.app.di.AppInitializer
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class CrossLensApplication : Application() {

    @Inject
    lateinit var appInitializer: AppInitializer

    override fun onCreate() {
        super.onCreate()
        appInitializer.initialize()
    }
}
