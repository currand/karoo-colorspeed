package com.currand60.karoocolorspeed.data

import android.content.Context
import androidx.compose.ui.unit.DpSize
import androidx.glance.appwidget.ExperimentalGlanceRemoteViewsApi
import androidx.glance.appwidget.GlanceRemoteViews
import com.currand60.karoocolorspeed.extension.streamDataFlow
import io.hammerhead.karooext.KarooSystemService
import io.hammerhead.karooext.extension.DataTypeImpl
import io.hammerhead.karooext.internal.ViewEmitter
import io.hammerhead.karooext.models.DataType
import io.hammerhead.karooext.models.StreamState
import io.hammerhead.karooext.models.UpdateGraphicConfig
import io.hammerhead.karooext.models.UpdateNumericConfig
import io.hammerhead.karooext.models.ViewConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.lang.Thread.sleep
import javax.inject.Inject

@OptIn(ExperimentalGlanceRemoteViewsApi::class)
class ColorSpeed(
    extension: String
) : DataTypeImpl(extension, TYPE_ID) {
    private val glance = GlanceRemoteViews()

    @Inject
    lateinit var karooSystem: KarooSystemService

    companion object {
        const val TYPE_ID = "colorspeed"
    }

    private val dataScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

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
            when (config.preview) {
                true -> {
                    repeat(Int.MAX_VALUE) {
                        val speed = (0..100).random()
                        val averageSpeed = (0..100).random()
                        sleep((2000..3000).random().toLong())
                        val result = glance.compose(context, DpSize.Unspecified) {
                            ColorSpeedView(
                                context,
                                speed.toDouble(),
                                averageSpeed.toDouble(),
                                config
                            )
                        }
                        emitter.updateView(result.remoteViews)
                    }
                }

                false -> {
                    val speedFlow = karooSystem.streamDataFlow(DataType.Type.SPEED)
                    val averageSpeedFlow = karooSystem.streamDataFlow(DataType.Type.AVERAGE_SPEED)
                    combine(speedFlow, averageSpeedFlow) { speed, averageSpeed ->
                        if (speed is StreamState.Streaming && averageSpeed is StreamState.Streaming) {
                            Pair(
                                speed.dataPoint.singleValue!!,
                                averageSpeed.dataPoint.singleValue!!)
                        } else {
                            Pair(0,0)
                        }
                    }.collect {
                        val result = glance.compose(context, DpSize.Unspecified) {
                            ColorSpeedView(
                                context,
                                it.first.toDouble(),
                                it.second.toDouble(),
                                config
                            )
                        }
                        emitter.updateView(result.remoteViews)
                    }
                }
            }
        }
        emitter.setCancellable {
            viewJob.cancel()
            configJob.cancel()
        }
    }
}