package com.currand60.karoocolorspeed.extension

import com.currand60.karoocolorspeed.BuildConfig
import com.currand60.karoocolorspeed.data.ColorSpeed
import io.hammerhead.karooext.KarooSystemService
import io.hammerhead.karooext.extension.KarooExtension
import javax.inject.Inject
import dagger.hilt.android.AndroidEntryPoint
import io.hammerhead.karooext.models.KarooEvent
import io.hammerhead.karooext.models.OnStreamState
import io.hammerhead.karooext.models.StreamState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow


@AndroidEntryPoint
class ColorSpeedExtension : KarooExtension("karoocolorspeed", BuildConfig.VERSION_NAME) {

    @Inject
    lateinit var karooSystem: KarooSystemService

    override val types by lazy {
        listOf(
            ColorSpeed(extension)
        )
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

inline fun <reified T : KarooEvent> KarooSystemService.consumerFlow(): Flow<T> {
    return callbackFlow {
        val listenerId = addConsumer<T> {
            trySend(it)
        }
        awaitClose {
            removeConsumer(listenerId)
        }
    }
}