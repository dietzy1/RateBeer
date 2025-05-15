package dk.grp30.ratebeer

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class RateBeerApp : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}