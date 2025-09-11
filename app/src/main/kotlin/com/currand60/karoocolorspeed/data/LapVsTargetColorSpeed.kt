package com.currand60.karoocolorspeed.data

import android.content.Context
import androidx.compose.ui.unit.DpSize
import androidx.glance.appwidget.ExperimentalGlanceRemoteViewsApi
import androidx.glance.appwidget.GlanceRemoteViews
import com.currand60.karoocolorspeed.KarooSystemServiceProvider
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
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import com.currand60.karoocolorspeed.R
import com.currand60.karoocolorspeed.managers.ConfigurationManager
import io.hammerhead.karooext.models.UserProfile
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalGlanceRemoteViewsApi::class)
class LapVsTargetColorSpeed(
    private val karooSystem: KarooSystemServiceProvider,
    extension: String
) : DataTypeImpl(extension, TYPE_ID) {

    private val glance = GlanceRemoteViews()

    companion object {
        const val TYPE_ID = "lapvstarget"
    }

    private val dataScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private fun previewFlow(constantValue: Double? = null): Flow<StreamState> = flow {
        while (true) {
            val value = constantValue ?: (((0..17).random() * 10).toDouble() / 10.0)
            emit(StreamState.Streaming(
                DataPoint(
                    dataTypeId,
                    mapOf(DataType.Field.SPEED to value),
                    extension
                )
            ))
            delay(1000)
        }
    }.flowOn(Dispatchers.IO)

    override fun startView(context: Context, config: ViewConfig, emitter: ViewEmitter) {
        val configJob = dataScope.launch {
            emitter.onNext(
                UpdateGraphicConfig(showHeader = false)
            )
            emitter.onNext(
                UpdateNumericConfig(DataType.Field.SPEED)
            )
        }
        val viewJob = dataScope.launch {
            val colorConfig = ConfigurationManager(context).getConfigFlow().first()
            val userProfileState = karooSystem.streamUserProfile().first()

            val speedFlow = if (!config.preview) karooSystem.streamDataFlow(DataType.Type.AVERAGE_SPEED_LAP) else previewFlow()
                speedFlow.collect { streamState ->
                    when (streamState) {
                        is StreamState.Streaming -> {
                            val speedUnits = when(userProfileState.preferredUnit.distance) {
                                UserProfile.PreferredUnit.UnitType.IMPERIAL -> 2.23694
                                else -> 3.6
                            }
                            val result = glance.compose(context, DpSize.Unspecified) {
                                ColorSpeedView(
                                    context,
                                    (streamState.dataPoint.singleValue ?: 0.0) * speedUnits,
                                    colorConfig.targetSpeed * speedUnits,
                                    config,
                                    colorConfig,
                                    "lap_vs_target_title",
                                    context.getString(R.string.lap_vs_target_description),
                                    speedUnits
                                )
                            }
                            emitter.updateView(result.remoteViews)
                        }
                        else -> {
                            streamState
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