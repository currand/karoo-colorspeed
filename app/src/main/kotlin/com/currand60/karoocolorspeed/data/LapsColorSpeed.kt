package com.currand60.karoocolorspeed.data

import android.content.Context
import androidx.compose.ui.unit.DpSize
import androidx.glance.appwidget.ExperimentalGlanceRemoteViewsApi
import androidx.glance.appwidget.GlanceRemoteViews
import com.currand60.karoocolorspeed.R
import com.currand60.karoocolorspeed.extension.streamDataFlow
import io.hammerhead.karooext.KarooSystemService
import io.hammerhead.karooext.extension.DataTypeImpl
import io.hammerhead.karooext.internal.ViewEmitter
import io.hammerhead.karooext.models.DataPoint
import io.hammerhead.karooext.models.DataType
import io.hammerhead.karooext.models.StreamState
import io.hammerhead.karooext.models.UpdateGraphicConfig
import io.hammerhead.karooext.models.UpdateNumericConfig
import io.hammerhead.karooext.models.ViewConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber


@OptIn(ExperimentalGlanceRemoteViewsApi::class)
class LapsColorSpeed(
    private val karooSystem: KarooSystemService,
    extension: String
) : DataTypeImpl(extension, TYPE_ID) {
    private val glance = GlanceRemoteViews()

    companion object {
        const val TYPE_ID = "lapscolorspeed"
    }

    private val dataScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private fun previewFlow(constantValue: Double? = null): Flow<StreamState> = flow {
        while (true) {
            val value = constantValue ?: (((0..100).random() * 10).toDouble() / 10.0)
            emit(StreamState.Streaming(
                DataPoint(
                    dataTypeId,
                    mapOf(DataType.Field.SINGLE to value),
                    extension
                )
            ))
            delay(1000)
        }
    }.flowOn(Dispatchers.IO)

//

    override fun startView(context: Context, config: ViewConfig, emitter: ViewEmitter) {
        val configJob = dataScope.launch {
            emitter.onNext(
                UpdateGraphicConfig(showHeader = false)
            )
            emitter.onNext(
                UpdateNumericConfig(DataType.Type.SPEED)
            )
        }
        val viewJob = dataScope.launch {
                    val speedFlow = if (!config.preview) karooSystem.streamDataFlow(DataType.Type.AVERAGE_SPEED_LAP) else previewFlow()
                    val averageSpeedFlow = if (!config.preview) karooSystem.streamDataFlow(DataType.Type.AVERAGE_SPEED_LAST_LAP) else previewFlow(50.0)
                    combine(speedFlow, averageSpeedFlow) { speedState, averageSpeedState ->
                        if (speedState is StreamState.Streaming && averageSpeedState is StreamState.Streaming) {
                            Pair(
                                speedState.dataPoint.singleValue!!,
                                averageSpeedState.dataPoint.singleValue!!
                            )
                        } else {
                            Pair(0.0, 0.0)
                        }
                    }.onEach {
                        Timber.d("speed: ${it.first}, average: ${it.second}")
                    }.collect {
                        val result = glance.compose(context, DpSize.Unspecified) {
                            ColorSpeedView(
                                context,
                                it.first,
                                it.second,
                                config,
                                context.getString(R.string.lap_speed_title),
                                context.getString(R.string.lap_speed_description)
                            )
                        }
                        emitter.updateView(result.remoteViews)
                    }
                }
        emitter.setCancellable {
            viewJob.cancel()
            configJob.cancel()
        }
    }
}