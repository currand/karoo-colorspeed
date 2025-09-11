package com.currand60.karoocolorspeed.data

data class ConfigData(
    val useBackgroundColors: Boolean,
    val useArrows: Boolean,
    val stoppedValue: Double,
    val speedPercentLevel1: Int,
    val speedPercentLevel2: Int,
    val speedPercentMiddleTargetLow: Int,
    val speedPercentMiddleTargetHigh: Int,
    val speedPercentLevel4: Int,
    val speedPercentLevel5: Int,
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
            stoppedValue = 0.9,
            speedPercentLevel1 = 50,
            speedPercentLevel2 = 65,
            speedPercentMiddleTargetLow = 95,
            speedPercentMiddleTargetHigh = 105,
            speedPercentLevel4 = 110,
            speedPercentLevel5 = 120,
            targetSpeed = 8.94,
        )
    }

    fun validate(): Boolean {
        return stoppedValue >= 0 &&
                targetSpeed >= 0 &&
                speedPercentLevel1 > 0 &&
                speedPercentLevel2 > 0 &&
                speedPercentMiddleTargetLow > 0 &&
                speedPercentMiddleTargetHigh > 0 &&
                speedPercentLevel4 > 0 &&
                speedPercentLevel5 > 0 &&
                speedPercentLevel1 < speedPercentLevel2 &&
                speedPercentLevel2 < speedPercentMiddleTargetLow &&
                speedPercentMiddleTargetLow < speedPercentMiddleTargetHigh &&
                speedPercentMiddleTargetHigh < speedPercentLevel4 &&
                speedPercentLevel4 < speedPercentLevel5
    }
}