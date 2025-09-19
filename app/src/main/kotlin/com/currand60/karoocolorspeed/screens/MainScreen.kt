package com.currand60.karoocolorspeed.screens


import androidx.activity.ComponentActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.currand60.karoocolorspeed.KarooSystemServiceProvider
import com.currand60.karoocolorspeed.R
import com.currand60.karoocolorspeed.data.ConfigData
import com.currand60.karoocolorspeed.managers.ConfigurationManager
import io.hammerhead.karooext.models.UserProfile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import timber.log.Timber
import kotlin.math.roundToInt

private fun Double.roundValue(): Double {
    return (this * 10).roundToInt() / 10.0
}

@Composable
fun PercentConfigField(
    label: String,
    initialValue: Double,
    units: String,
    onValueParsed: (newValueString: String, parsedValue: Double?, isValid: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean,
    errorSupportingText: String? = null,
    prefix: String? = null,
) {

    var textInput by remember { mutableStateOf(initialValue.toString()) }

    LaunchedEffect(initialValue) {
        if (textInput != initialValue.toString()) {
            textInput = initialValue.toString()
        }
    }

    OutlinedTextField(
        value = textInput,
        onValueChange = { newValue ->
            textInput = newValue // Always update the local string state
            val parsedValue = newValue.toDoubleOrNull()
            val isValid = parsedValue != null
            onValueParsed(newValue, parsedValue, isValid) // Notify the parent
        },
        label = { Text(text = label) },
        modifier = modifier,
        placeholder = { Text(initialValue.toString()) },
        prefix = if (prefix != null) { { Text(prefix) } } else (null),
        suffix = { Text(units) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true, // Restrict to a single line
        isError = isError, // Error state controlled by the parent
        supportingText = {
            if (isError && errorSupportingText != null) {
                Text(errorSupportingText) // Display error message if applicable
            }
        }
    )
}

@Composable
fun MainScreen(
    configManager: ConfigurationManager = koinInject(),
    karooSystem: KarooSystemServiceProvider = koinInject()
) {

    val context = LocalContext.current
//    val configManager: ConfigurationManager = koinInject()
    val coroutineScope = rememberCoroutineScope()
//    val karooSystem: KarooSystemServiceProvider = koinInject()

    val scrollState = rememberScrollState()
    val isScrolledToBottom = scrollState.value == scrollState.maxValue



    val loadedConfig by produceState(initialValue = ConfigData.DEFAULT, key1 = configManager) {
        Timber.d("Starting to load initial config via produceState.")
        value = configManager.getConfigFlow().first()
        Timber.d("Initial config loaded: $value")
    }

    val karooDistanceUnit by produceState(initialValue = UserProfile.PreferredUnit.UnitType.IMPERIAL, key1 = karooSystem) {
        Timber.d("Starting to load Karoo distance units via produceState.")
        karooSystem.streamUserProfile()
            .collect { profile ->
                value = profile.preferredUnit.distance
                Timber.d("Karoo distance units loaded: $value")
            }
    }


    var originalConfig by remember { mutableStateOf(ConfigData.DEFAULT) }
    var currentConfig by remember { mutableStateOf(ConfigData.DEFAULT) }
    var speedPercent1Error by remember { mutableStateOf(false) }
    var speedPercent2Error by remember { mutableStateOf(false) }
    var speedPercentTargetLowError by remember { mutableStateOf(false) }
    var speedPercentTargetHighError by remember { mutableStateOf(false) }
    var speedPercent4Error by remember { mutableStateOf(false) }
    var speedPercent5Error by remember { mutableStateOf(false) }

    var speedMultiplier by remember { mutableDoubleStateOf(if (karooDistanceUnit == UserProfile.PreferredUnit.UnitType.IMPERIAL) 2.23694 else 3.6) }

    val configIsGood by remember (currentConfig) {
        derivedStateOf {
            currentConfig.validate()
        }
    }

    var stoppedSpeedInput by remember(loadedConfig, speedMultiplier) {
        mutableStateOf(loadedConfig.stoppedValue.times(speedMultiplier).roundValue().toString())
    }
    var stoppedSpeedError by remember { mutableStateOf(false) }

    var targetSpeedInput by remember(loadedConfig, speedMultiplier) {
        mutableStateOf(loadedConfig.targetSpeed.times(speedMultiplier).roundValue().toString())
    }
    var targetSpeedError by remember { mutableStateOf(false) }

    LaunchedEffect(loadedConfig, karooDistanceUnit) {
        speedMultiplier = when(karooDistanceUnit) {
            UserProfile.PreferredUnit.UnitType.IMPERIAL -> 2.23694
            else -> 3.6
        }
        originalConfig = loadedConfig
        currentConfig = loadedConfig
    }

    LaunchedEffect(currentConfig) {
        Timber.d("Current config updated: $currentConfig")
        if (currentConfig.validate()) {
            speedPercent1Error = false
            speedPercent2Error = false
            speedPercent4Error = false
            speedPercent5Error = false
            speedPercentTargetLowError = false
            speedPercentTargetHighError = false
            stoppedSpeedError = false
            targetSpeedError = false
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .padding(5.dp)
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text(
                text = context.getString(R.string.color_speed_settings_title),
                textAlign = TextAlign.Left,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = context.getString(R.string.color_speed_settings_description),
                textAlign = TextAlign.Left,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .weight(0.2f)
                        .aspectRatio(1f)
                        .border(BorderStroke(1.dp, Color.Black), CircleShape)
                )
                OutlinedTextField(
                    value = stoppedSpeedInput,
                    onValueChange = { newValue ->
                        stoppedSpeedInput = newValue
                        val parsedValue = newValue.toDoubleOrNull()
                        if (parsedValue != null) {
                            currentConfig = currentConfig.copy(stoppedValue = parsedValue.div(speedMultiplier).roundValue())
                            stoppedSpeedError = false
                        } else {
                            stoppedSpeedError = true
                        }
                    },
                    modifier = Modifier
                        .weight(0.8f)
                        .padding(start = 5.dp, end = 5.dp),
                    suffix = {
                        val suffixText = when (karooDistanceUnit) {
                            UserProfile.PreferredUnit.UnitType.IMPERIAL -> "mph"
                            else -> "kmh"
                        }
                        Text(suffixText)
                    },
                    label = { Text(context.getString(R.string.stopped_speed)) },
                    placeholder = {
                        val placeholderText = loadedConfig
                            .stoppedValue
                            .times(speedMultiplier)
                            .roundValue()
                            .toString()
                        Text(placeholderText)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = stoppedSpeedError,
                    supportingText = {
                        if (stoppedSpeedError) Text(context.getString(R.string.enter_valid_number))
                    }
                )
            }
            Row(
                modifier = Modifier.fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .weight(0.2f)
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .background(color = Color(context.getColor(R.color.dark_red)), CircleShape)
                )
                PercentConfigField(
                    label = context.getString(R.string.well_below_target),
                    initialValue = currentConfig.speedPercentLevel1.toDouble(),
                    prefix = "<",
                    units = "%",
                    onValueParsed = { newValueString, parsedValue, isValid ->
                        speedPercent1Error = !isValid
                        if (isValid && parsedValue != null) {
                            val attemptedConfig = currentConfig.copy(speedPercentLevel1 = parsedValue.toInt())
                            if (attemptedConfig.validate()) {
                                speedPercent1Error = false
                                currentConfig = currentConfig.copy(speedPercentLevel1 = parsedValue.toInt())
                            } else {
                                speedPercent1Error = true
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(0.8f)
                        .padding(start = 5.dp, end = 5.dp),
                    isError = speedPercent1Error,
                    errorSupportingText = context.getString(R.string.enter_number_below_next_highest_level),
                )
            }
            Row(
                modifier = Modifier.fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .weight(0.2f)
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .background(color = Color(context.getColor(R.color.orange)), CircleShape)
                )
                PercentConfigField(
                    label = context.getString(R.string.below_target),
                    initialValue = currentConfig.speedPercentLevel2.toDouble(),
                    units = "%",
                    onValueParsed = { newValueString, parsedValue, isValid ->
                        speedPercent2Error = !isValid
                        if (isValid && parsedValue != null) {
                            val attemptedConfig = currentConfig.copy(speedPercentLevel2 = parsedValue.toInt())
                            if (attemptedConfig.validate()) {
                                speedPercent2Error = false
                                currentConfig = currentConfig.copy(speedPercentLevel2 = parsedValue.toInt())
                            } else {
                                speedPercent2Error = true
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(0.8f)
                        .padding(start = 5.dp, end = 5.dp),
                    isError = speedPercent2Error,
                    errorSupportingText = context.getString(R.string.enter_number_below_next_highest_level),
                )
            }
            Row(
                modifier = Modifier.fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .weight(0.2f)
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .background(color = Color(context.getColor(R.color.middle)), CircleShape)
                )
                PercentConfigField(
                    label = context.getString(R.string.target_low),
                    initialValue = currentConfig.speedPercentMiddleTargetLow.toDouble(),
                    units = "%",
                    onValueParsed = { newValueString, parsedValue, isValid ->
                        speedPercentTargetLowError = !isValid
                        if (isValid && parsedValue != null) {
                            val attemptedConfig = currentConfig.copy(speedPercentMiddleTargetLow = parsedValue.toInt())
                            if (attemptedConfig.validate()) {
                                speedPercentTargetLowError = false
                                currentConfig = currentConfig.copy(speedPercentMiddleTargetLow = parsedValue.toInt())
                            } else {
                                speedPercentTargetLowError = true
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(0.4f)
                        .padding(start = 5.dp, end = 5.dp),
                    isError = speedPercentTargetLowError,
                    errorSupportingText = context.getString(R.string.enter_number_below_next_highest_level),
                )
                PercentConfigField(
                    label = context.getString(R.string.target_high),
                    initialValue = currentConfig.speedPercentMiddleTargetHigh.toDouble(),
                    units = "%",
                    onValueParsed = { newValueString, parsedValue, isValid ->
                        speedPercentTargetHighError = !isValid
                        if (isValid && parsedValue != null) {
                            val attemptedConfig = currentConfig.copy(speedPercentMiddleTargetHigh = parsedValue.toInt())
                            if (attemptedConfig.validate()) {
                                speedPercentTargetHighError = false
                                currentConfig = currentConfig.copy(speedPercentMiddleTargetHigh = parsedValue.toInt())
                            } else {
                                speedPercentTargetHighError = true
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(0.4f)
                        .padding(start = 5.dp, end = 5.dp),
                    isError = speedPercentTargetHighError,
                    errorSupportingText = context.getString(R.string.enter_number_below_next_highest_level),
                )
            }
            Row(
                modifier = Modifier.fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .weight(0.2f)
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .background(color = Color(context.getColor(R.color.light_green)), CircleShape)
                )
                PercentConfigField(
                    label = context.getString(R.string.above_target),
                    initialValue = currentConfig.speedPercentLevel4.toDouble(),
                    units = "%",
                    onValueParsed = { newValueString, parsedValue, isValid ->
                        speedPercent4Error = !isValid
                        if (isValid && parsedValue != null) {
                            val attemptedConfig = currentConfig.copy(speedPercentLevel4 = parsedValue.toInt())
                            if (attemptedConfig.validate()) {
                                speedPercent4Error = false
                                currentConfig = currentConfig.copy(speedPercentLevel4 = parsedValue.toInt())
                            } else {
                                speedPercent4Error = true
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(0.8f)
                        .padding(start = 5.dp, end = 5.dp),
                    isError = speedPercent4Error,
                    errorSupportingText = context.getString(R.string.enter_number_below_next_highest_level),
                )
            }
            Row(
                modifier = Modifier.fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .weight(0.2f)
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .background(color = Color(context.getColor(R.color.dark_green)), CircleShape)
                )
                PercentConfigField(
                    label = context.getString(R.string.well_above_target),
                    initialValue = currentConfig.speedPercentLevel5.toDouble(),
                    prefix = ">",
                    units = "%",
                    onValueParsed = { newValueString, parsedValue, isValid ->
                        speedPercent5Error = !isValid
                        if (isValid && parsedValue != null) {
                            val attemptedConfig = currentConfig.copy(speedPercentLevel5 = parsedValue.toInt())
                            if (attemptedConfig.validate()) {
                                speedPercent5Error = false
                                currentConfig = currentConfig.copy(speedPercentLevel5 = parsedValue.toInt())
                            } else {
                                speedPercent5Error = true
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(0.8f)
                        .padding(start = 5.dp, end = 5.dp),
                    isError = speedPercent5Error,
                    errorSupportingText = context.getString(R.string.enter_number_below_next_highest_level),
                )
            }
            Row(modifier = Modifier.fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically
            )
            {
                Switch(
                    checked = currentConfig.useArrows,
                    onCheckedChange = { isChecked ->
                        currentConfig = currentConfig.copy(useArrows = isChecked)
                    }
                )
                Text(
                    modifier = Modifier
                        .padding(start = 5.dp)
                        .align(Alignment.CenterVertically),
                    text = context.getString(R.string.display_errors),
                )
            }
            Row(modifier = Modifier.fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically
            )
            {
                Switch(
                    checked = currentConfig.useBackgroundColors,
                    onCheckedChange = { isChecked ->
                        currentConfig = currentConfig.copy(useBackgroundColors = isChecked)
                    }
                )
                Text(
                    modifier = Modifier
                        .padding(start = 5.dp)
                        .align(Alignment.CenterVertically),
                    text = context.getString(R.string.background_colors),
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = targetSpeedInput,
                    onValueChange = { newValue ->
                        targetSpeedInput = newValue
                        val parsedValue = newValue.toDoubleOrNull()
                        if (parsedValue != null) {
                            currentConfig = currentConfig.copy(targetSpeed = parsedValue.div(speedMultiplier).roundValue())
                            targetSpeedError = false
                        } else {
                            targetSpeedError = true
                        }
                    },
                    suffix = {
                        val suffixText = when (karooDistanceUnit) {
                            UserProfile.PreferredUnit.UnitType.IMPERIAL -> "mph"
                            else -> "kmh"
                        }
                        Text(suffixText)
                    },
                    label = { Text(context.getString(R.string.target_speed)) },
                    placeholder = {
                        val placeholderText = loadedConfig
                            .targetSpeed
                            .times(speedMultiplier)
                            .roundValue()
                            .toString()
                        Text(placeholderText)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = targetSpeedError,
                    supportingText = {
                        if (stoppedSpeedError) Text(context.getString(R.string.enter_valid_number))
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            // Save Button
            FilledTonalButton(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .height(50.dp)
                    .padding(vertical = 8.dp),
                onClick = {
                    val configToSave = currentConfig
                    if (currentConfig.validate()) {
                        coroutineScope.launch {
                            Timber.d("Attempting to save config: $configToSave")
                            configManager.saveConfig(configToSave)
                            Timber.i("Configuration save initiated.")
                        }
                    } else {
                        Timber.w("Save blocked due to input validation errors.")
                    }
                },
                enabled = configIsGood && currentConfig != originalConfig
            ) {
                Icon(Icons.Default.Check, contentDescription = "Save")
                Spacer(modifier = Modifier.width(5.dp))
                Text(context.getString(R.string.save))
            }

        }
        Spacer(modifier = Modifier.height(20.dp))
        if (isScrolledToBottom) {
            Image(
                painter = painterResource(id = R.drawable.back), // Load your drawable
                contentDescription = "Back", // For accessibility
                modifier = Modifier
                    .align(Alignment.BottomStart) // Aligns the image to the bottom-left
                    .padding(bottom = 10.dp) // Add some padding from the screen edges
                    .size(54.dp) // Set a suitable size for the clickable area and image
                    .clickable {
                        // Use LocalContext to find the activity and trigger back press
                        val activity =
                            context as? ComponentActivity // Or FragmentActivity
                        activity?.onBackPressedDispatcher?.onBackPressed()
                    }
            )
        }
    }
}

@Preview(locale = "en", name = "karoo", device = "spec:width=480px,height=800px,dpi=300")
@Composable
private fun Preview_MyComposable_Enabled() {
    val context = LocalContext.current
    MainScreen(
        configManager = ConfigurationManager(context),
        karooSystem = KarooSystemServiceProvider(context)
    )
}