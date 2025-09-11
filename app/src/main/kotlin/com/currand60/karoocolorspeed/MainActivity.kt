package com.currand60.karoocolorspeed

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.currand60.karoocolorspeed.screens.MainScreen
import com.currand60.karoocolorspeed.theme.AppTheme
import timber.log.Timber

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            Timber.d("Timber initialized in Debug mode.")
        } else {
            Timber.d("Timber initialized in Release mode (no DebugTree planted).")
        }
        setContent {
            AppTheme {
                MainScreen()
            }
        }
    }
}
