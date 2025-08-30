package com.currand60.karoocolorspeed.data

import android.content.Context
import androidx.compose.ui.unit.DpSize
import androidx.glance.appwidget.ExperimentalGlanceRemoteViewsApi
import androidx.glance.appwidget.GlanceRemoteViews
import com.currand60.karoocolorspeed.R
import com.currand60.karoocolorspeed.extension.streamDataFlow
import com.currand60.karoocolorspeed.extension.streamUserProfile
import io.hammerhead.karooext.KarooSystemService
import io.hammerhead.karooext.extension.DataTypeImpl
import io.hammerhead.karooext.internal.ViewEmitter
import io.hammerhead.karooext.models.DataPoint
import io.hammerhead.karooext.models.DataType
import io.hammerhead.karooext.models.StreamState
import io.hammerhead.karooext.models.UpdateGraphicConfig
import io.hammerhead.karooext.models.UpdateNumericConfig
import io.hammerhead.karooext.models.UserProfile
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
            val value = constantValue ?: (((0..17).random() * 10).toDouble() / 10.0)
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
                UpdateNumericConfig(DataType.Field.SPEED)
            )
        }
        val viewJob = dataScope.launch {
            val userProfileFlow = karooSystem.streamUserProfile()
            val speedFlow = if (!config.preview) karooSystem.streamDataFlow(DataType.Type.SPEED) else previewFlow()
            val averageSpeedFlow = if (!config.preview) karooSystem.streamDataFlow(DataType.Type.AVERAGE_SPEED) else previewFlow(10.0)
            combine(speedFlow, averageSpeedFlow, userProfileFlow) { speedState, averageSpeedState, userProfileState ->
                val speedUnits = when(userProfileState.preferredUnit.distance) {
                    UserProfile.PreferredUnit.UnitType.IMPERIAL -> 2.23694
                    else -> 3.6
                }
                if (speedState is StreamState.Streaming && averageSpeedState is StreamState.Streaming) {
                    Pair(
                        speedState.dataPoint.singleValue!!.toDouble() * speedUnits,
                        averageSpeedState.dataPoint.singleValue!!.toDouble() * speedUnits
                    )
                } else {
                    Pair(0.0, 0.0)
                }
            }.onEach {
                Timber.d("$TYPE_ID ${it.first}, average: ${it.second}")
            }.collect {
                val result = glance.compose(context, DpSize.Unspecified) {
                    ColorSpeedView(
                        context,
                        it.first,
                        it.second,
                        config,
                        "lap_speed_title",
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