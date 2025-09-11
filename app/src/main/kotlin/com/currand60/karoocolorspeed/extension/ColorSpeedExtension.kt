package com.currand60.karoocolorspeed.extension

import com.currand60.karoocolorspeed.KarooSystemServiceProvider
import com.currand60.karoocolorspeed.BuildConfig
import com.currand60.karoocolorspeed.data.CurrentVsLapAverageSpeed
import com.currand60.karoocolorspeed.data.CurrentVsRideAverageSpeed
import com.currand60.karoocolorspeed.data.LapVsTargetColorSpeed
import com.currand60.karoocolorspeed.data.CurrentLapVsLLAverageSpeed
import com.currand60.karoocolorspeed.data.SpeedVsTargetColorSpeed
import io.hammerhead.karooext.extension.KarooExtension
import org.koin.android.ext.android.inject
import timber.log.Timber
import kotlin.getValue


class ColorSpeedExtension : KarooExtension("karoocolorspeed", BuildConfig.VERSION_NAME) {

    private val karooSystem: KarooSystemServiceProvider by inject()


    override val types by lazy {
        listOf(
            CurrentVsRideAverageSpeed(karooSystem, extension),
            CurrentLapVsLLAverageSpeed(karooSystem, extension),
            CurrentVsLapAverageSpeed(karooSystem, extension),
            SpeedVsTargetColorSpeed(karooSystem, extension),
            LapVsTargetColorSpeed(karooSystem, extension)
        )
    }

    override fun onCreate() {
        super.onCreate()
        karooSystem.karooSystemService.connect { connected ->
            Timber.d("Karoo connected: $connected")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        karooSystem.karooSystemService.disconnect()
        Timber.d("Karoo disconnected")
    }
}

