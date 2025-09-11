package com.currand60.karoocolorspeed.extension

import com.currand60.karoocolorspeed.KarooSystemServiceProvider
import com.currand60.karoocolorspeed.BuildConfig
import com.currand60.karoocolorspeed.data.CurrentVsLapAverageSpeed
import com.currand60.karoocolorspeed.data.CurrentVsRideAverageSpeed
import com.currand60.karoocolorspeed.data.LapVsTargetColorSpeed
import com.currand60.karoocolorspeed.data.CurrentLapVsLLAverageSpeed
import com.currand60.karoocolorspeed.data.SpeedVsTargetColorSpeed
import io.hammerhead.karooext.KarooSystemService
import io.hammerhead.karooext.extension.KarooExtension
import io.hammerhead.karooext.models.OnStreamState
import io.hammerhead.karooext.models.StreamState
import io.hammerhead.karooext.models.UserProfile
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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

fun KarooSystemService.streamDataFlow(dataTypeId: String): Flow<StreamState> {
    return callbackFlow {
        val listenerId = addConsumer(OnStreamState.StartStreaming(dataTypeId)) { event: OnStreamState ->
            trySendBlocking(event.state)
        }
        awaitClose {
            removeConsumer(listenerId)
        }
    }
}

fun KarooSystemService.streamUserProfile(): Flow<UserProfile> {
    return callbackFlow {
        val listenerId = addConsumer { userProfile: UserProfile ->
            trySendBlocking(userProfile)
        }
        awaitClose {
            removeConsumer(listenerId)
        }
    }
}

