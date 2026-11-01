package com.crosslens.app

import android.app.Application
import com.crosslens.app.di.AppInitializer
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class CrossLensApplication : Application() {

    @Inject
    lateinit var appInitializer: AppInitializer

    override fun onCreate() {
        super.onCreate()

        // Initialize Crashlytics only for release builds
        if (BuildConfig.FIREBASE_ENABLED && !BuildConfig.DEBUG &&
            com.google.firebase.FirebaseApp.getApps(this).isNotEmpty()) {
            FirebaseCrashlytics.getInstance().apply {
                // Enable crash reporting
                setCrashlyticsCollectionEnabled(false)

                // Set custom keys for debugging
                setCustomKey("build_type", BuildConfig.BUILD_TYPE)
                setCustomKey("version_name", BuildConfig.VERSION_NAME)
                setCustomKey("version_code", BuildConfig.VERSION_CODE)
            }
        }

        appInitializer.initialize()
        com.crosslens.app.data.sync.FeedSyncWorker.schedule(this)
    }
}
