package com.currand60.karoocolorspeed.managers

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.currand60.karoocolorspeed.data.ConfigData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class ConfigurationManager (
    private val context: Context,
){


    companion object {
        private val USE_BG_COLOR_KEY = booleanPreferencesKey("useBackgroundColors")
        private val USE_ARROWS_KEY = booleanPreferencesKey("useArrows")
        private val USE_TARGET_SPEED_KEY = booleanPreferencesKey("useTargetSpeed")
        private val STOPPED_VALUE_KEY = intPreferencesKey("stoppedValue")
        private val SPEED_PERCENT_LEVEL1_KEY = intPreferencesKey("speedPercentLevel1")
        private val SPEED_PERCENT_LEVEL2_KEY = intPreferencesKey("speedPercentLevel2")
        private val SPEED_PERCENT_MIDDLE_TARGET_LOW_KEY = intPreferencesKey("speedPercentMiddleTargetLow")
        private val SPEED_PERCENT_MIDDLE_TARGET_HIGH_KEY = intPreferencesKey("speedPercentMiddleTargetHigh")
        private val SPEED_PERCENT_LEVEL4_KEY = intPreferencesKey("speedPercentLevel4")
        private val SPEED_PERCENT_LEVEL5_KEY = intPreferencesKey("speedPercentLevel5")
        private val TARGET_SPEED_KEY = intPreferencesKey("targetSpeed")

    }

    suspend fun saveConfig(config: ConfigData) {
        Timber.d("Attempting to save configuration to DataStore: $config")
        context.dataStore.edit { preferences ->
            preferences[USE_BG_COLOR_KEY] = config.useBackgroundColors
            preferences[USE_ARROWS_KEY] = config.useArrows
            preferences[USE_TARGET_SPEED_KEY] = config.useTargetSpeed
            preferences[STOPPED_VALUE_KEY] = config.stoppedValue
            preferences[SPEED_PERCENT_LEVEL1_KEY] = config.speedPercentLevel1
            preferences[SPEED_PERCENT_LEVEL2_KEY] = config.speedPercentLevel2
            preferences[SPEED_PERCENT_MIDDLE_TARGET_LOW_KEY] = config.speedPercentMiddleTargetLow
            preferences[SPEED_PERCENT_MIDDLE_TARGET_HIGH_KEY] = config.speedPercentMiddleTargetHigh
            preferences[SPEED_PERCENT_LEVEL4_KEY] = config.speedPercentLevel4
            preferences[SPEED_PERCENT_LEVEL5_KEY] = config.speedPercentLevel5
            preferences[TARGET_SPEED_KEY] = config.targetSpeed


        }
        Timber.i("Configuration successfully saved to DataStore.")
    }

    suspend fun getConfig(): ConfigData {
        Timber.d("Attempting to retrieve configuration from DataStore.")
        return context.dataStore.data.map { preferences ->
            val config = ConfigData(
                useBackgroundColors = preferences[USE_BG_COLOR_KEY] ?: ConfigData.DEFAULT.useBackgroundColors,
                useArrows = preferences[USE_ARROWS_KEY] ?: ConfigData.DEFAULT.useArrows,
                useTargetSpeed = preferences[USE_TARGET_SPEED_KEY] ?: ConfigData.DEFAULT.useTargetSpeed,
                stoppedValue = preferences[STOPPED_VALUE_KEY] ?: ConfigData.DEFAULT.stoppedValue,
                speedPercentLevel1 = preferences[SPEED_PERCENT_LEVEL1_KEY] ?: ConfigData.DEFAULT.speedPercentLevel1,
                speedPercentLevel2 = preferences[SPEED_PERCENT_LEVEL2_KEY] ?: ConfigData.DEFAULT.speedPercentLevel2,
                speedPercentMiddleTargetLow = preferences[SPEED_PERCENT_MIDDLE_TARGET_LOW_KEY] ?: ConfigData.DEFAULT.speedPercentMiddleTargetLow,
                speedPercentMiddleTargetHigh = preferences[SPEED_PERCENT_MIDDLE_TARGET_HIGH_KEY] ?: ConfigData.DEFAULT.speedPercentMiddleTargetHigh,
                speedPercentLevel4 = preferences[SPEED_PERCENT_LEVEL4_KEY] ?: ConfigData.DEFAULT.speedPercentLevel4,
                speedPercentLevel5 = preferences[SPEED_PERCENT_LEVEL5_KEY] ?: ConfigData.DEFAULT.speedPercentLevel5,
                targetSpeed = preferences[TARGET_SPEED_KEY] ?: ConfigData.DEFAULT.targetSpeed,
            )
            Timber.d("Retrieved configuration: $config")
            config
        }.first()
    }

    fun getConfigFlow(): Flow<ConfigData> {
        return context.dataStore.data.map { preferences ->
            ConfigData(
                useBackgroundColors = preferences[USE_BG_COLOR_KEY] ?: ConfigData.DEFAULT.useBackgroundColors,
                useArrows = preferences[USE_ARROWS_KEY] ?: ConfigData.DEFAULT.useArrows,
                useTargetSpeed = preferences[USE_TARGET_SPEED_KEY] ?: ConfigData.DEFAULT.useTargetSpeed,
                stoppedValue = preferences[STOPPED_VALUE_KEY] ?: ConfigData.DEFAULT.stoppedValue,
                speedPercentLevel1 = preferences[SPEED_PERCENT_LEVEL1_KEY] ?: ConfigData.DEFAULT.speedPercentLevel1,
                speedPercentLevel2 = preferences[SPEED_PERCENT_LEVEL2_KEY] ?: ConfigData.DEFAULT.speedPercentLevel2,
                speedPercentMiddleTargetLow = preferences[SPEED_PERCENT_MIDDLE_TARGET_LOW_KEY] ?: ConfigData.DEFAULT.speedPercentMiddleTargetLow,
                speedPercentMiddleTargetHigh = preferences[SPEED_PERCENT_MIDDLE_TARGET_HIGH_KEY] ?: ConfigData.DEFAULT.speedPercentMiddleTargetHigh,                speedPercentLevel4 = preferences[SPEED_PERCENT_LEVEL4_KEY] ?: ConfigData.DEFAULT.speedPercentLevel4,
                speedPercentLevel5 = preferences[SPEED_PERCENT_LEVEL5_KEY] ?: ConfigData.DEFAULT.speedPercentLevel5,
                targetSpeed = preferences[TARGET_SPEED_KEY] ?: ConfigData.DEFAULT.targetSpeed,
            )
        }.distinctUntilChanged()
    }
}