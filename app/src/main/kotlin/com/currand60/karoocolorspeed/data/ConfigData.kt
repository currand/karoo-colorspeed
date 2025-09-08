package com.currand60.karoocolorspeed.data

import kotlinx.serialization.Serializable

@Serializable
data class ConfigData(
    val useBackgroundColors: Boolean,
    val useArrows: Boolean,
    val useTargetSpeed: Boolean,
    val stoppedValue: Int,
    val speedPercentLevel1: Int,
    val speedPercentLevel2: Int,
    val speedPercentMiddleTargetLow: Int,
    val speedPercentMiddleTargetHigh: Int,
    val speedPercentLevel4: Int,
    val speedPercentLevel5: Int,
    val targetSpeed: Int,
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
            stoppedValue = 2,
            speedPercentLevel1 = 50,
            speedPercentLevel2 = 65,
            speedPercentMiddleTargetLow = 95,
            speedPercentMiddleTargetHigh = 105,
            speedPercentLevel4 = 110,
            speedPercentLevel5 = 120,
            targetSpeed = 0,
        )
    }
}