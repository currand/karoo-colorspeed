package com.currand60.karoocolorspeed.data

import kotlinx.serialization.Serializable

@Serializable
data class ConfigData(
    val useBackgroundColors: Boolean,
    val useArrows: Boolean,
    val useTargetSpeed: Boolean,
    val stoppedValue: Double,
    val speedPercentLevel1: Double,
    val speedPercentLevel2: Double,
    val speedPercentLevel3: Double,
    val speedPercentLevel4: Double,
    val speedPercentLevel5: Double,
    val targetSpeed: Double,
) {
    companion object {
        /**
         * Provides default configuration values.
         * These are used when no settings are found or when resetting to defaults.
         */
        val DEFAULT = ConfigData(
            useBackgroundColors = true,
            useArrows = true,
            useTargetSpeed = false,
            stoppedValue = 2.0,
            speedPercentLevel1 = 50.0,
            speedPercentLevel2 = 65.0,
            speedPercentLevel3 = 95.0,
            speedPercentLevel4 = 105.0,
            speedPercentLevel5 = 110.0,
            targetSpeed = 0.0,
        )
    }
}