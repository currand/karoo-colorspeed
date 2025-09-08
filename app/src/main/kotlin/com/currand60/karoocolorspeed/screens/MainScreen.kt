package com.currand60.karoocolorspeed.screens


import android.opengl.ETC1.isValid
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.currand60.karoocolorspeed.R
import com.currand60.karoocolorspeed.data.ConfigData
import com.currand60.karoocolorspeed.extension.streamUserProfile
import com.currand60.karoocolorspeed.managers.ConfigurationManager
import io.hammerhead.karooext.KarooSystemService
import io.hammerhead.karooext.models.UserProfile
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import timber.log.Timber

private fun checkConfigData(config: ConfigData): Boolean {
    return config.stoppedValue <= 0 &&
        config.speedPercentLevel1 < config.speedPercentLevel2 &&
        config.speedPercentLevel2 < config.speedPercentMiddleTargetLow &&
        config.speedPercentMiddleTargetLow < config.speedPercentMiddleTargetHigh &&
        config.speedPercentMiddleTargetHigh < config.speedPercentLevel4 &&
        config.speedPercentLevel4 < config.speedPercentLevel5
}

@Composable
fun PercentConfigField(
    label: String,
    initialValue: Int,
    units: String,
    onValueParsed: (newValueString: String, parsedValue: Int?, isValid: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean,
    errorSupportingText: String? = null,
    compValLow: Int = 0,
    compValHigh: Int = 1,
) {
    // Local state for the text field's raw string input.
    // `rememberSaveable` preserves the input across configuration changes (e.g., screen rotation).
    var textInput by remember { mutableStateOf(initialValue.toString()) }

    // Update textInput if initialValue changes externally (e.g., if parent updates currentConfig)
    LaunchedEffect(initialValue) {
        if (textInput != initialValue.toString()) {
            textInput = initialValue.toString()
        }
    }

    OutlinedTextField(
        value = textInput,
        onValueChange = { newValue ->
            textInput = newValue // Always update the local string state
            val parsedValue = newValue.toIntOrNull() // Attempt to parse to Int
            val isValid = parsedValue != null && compValLow < compValHigh // Check if parsing was successful
            onValueParsed(newValue, parsedValue, isValid) // Notify the parent
        },
        label = { Text(text = label) },
        modifier = modifier,
        placeholder = { Text(initialValue.toString()) },
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
fun MainScreen() {

    val context = LocalContext.current
    val configManager = ConfigurationManager(context)
    val coroutineScope = rememberCoroutineScope()
    val karooSystem = KarooSystemService(context)

    var fieldHasChanges by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val isScrolledToBottom = scrollState.value == scrollState.maxValue

    var originalConfig by remember { mutableStateOf(ConfigData.DEFAULT) }
    var currentConfig by remember { mutableStateOf(ConfigData.DEFAULT) }

    var stoppedInput by remember { mutableStateOf(currentConfig.stoppedValue.toString()) }
    var stoppedError by remember { mutableStateOf(false) }
    var speedPercent1Input by remember { mutableStateOf(currentConfig.speedPercentLevel1.toString()) }
    var speedPercent1Error by remember { mutableStateOf(false) }
    var speedPercent2Input by remember { mutableStateOf(currentConfig.speedPercentLevel2.toString()) }
    var speedPercent2Error by remember { mutableStateOf(false) }
    var speedPercentTargetLowInput by remember { mutableStateOf(currentConfig.speedPercentMiddleTargetLow.toString()) }
    var speedPercentTargetLowError by remember { mutableStateOf(false) }
    var speedPercentTargetHighInput by remember { mutableStateOf(currentConfig.speedPercentMiddleTargetHigh.toString()) }
    var speedPercentTargetHighError by remember { mutableStateOf(false) }
    var speedPercent4Input by remember { mutableStateOf(currentConfig.speedPercentLevel4.toString()) }
    var speedPercent4Error by remember { mutableStateOf(false) }
    var speedPercent5Input by remember { mutableStateOf(currentConfig.speedPercentLevel5.toString()) }
    var speedPercent5Error by remember { mutableStateOf(false) }

    val loadedConfig by produceState(initialValue = ConfigData.DEFAULT, key1 = configManager) {
        Timber.d("Starting to load initial config via produceState.")
        value = configManager.getConfig()
        Timber.d("Initial config loaded: $value")
    }

    val karooDistanceUnit by produceState(initialValue = UserProfile.PreferredUnit.UnitType.IMPERIAL, key1 = karooSystem) {
        Timber.d("Starting to load Karoo FTP via produceState.")
        karooSystem.streamUserProfile()
            .distinctUntilChanged()
            .collect { profile ->
                value = profile.preferredUnit.distance
            }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp)
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = "Color Speed Settings",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(8.dp))
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
                    label = "Stopped",
                    initialValue = currentConfig.stoppedValue,
                    units = when (karooDistanceUnit) {
                        UserProfile.PreferredUnit.UnitType.IMPERIAL -> "mp/h"
                        else -> "km/h"
                    },
                    onValueParsed = { newValueString, parsedValue, isValid ->
                        stoppedError = !isValid
                        if (isValid && parsedValue != null) {
                            currentConfig = currentConfig.copy(stoppedValue = parsedValue)
                        }
                    },
                    modifier = Modifier
                        .weight(0.8f)
                        .padding(start = 5.dp, end = 5.dp),
                    isError = stoppedError,
                    errorSupportingText = "Please enter a valid number",
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
                    label = "Well below target",
                    initialValue = currentConfig.speedPercentLevel1,
                    units = "%",
                    onValueParsed = { newValueString, parsedValue, isValid ->
                        speedPercent1Input = newValueString
                        speedPercent1Error = !isValid
                        if (isValid && parsedValue != null) {
                            currentConfig = currentConfig.copy(speedPercentLevel1 = parsedValue)
                        }
                    },
                    modifier = Modifier
                        .weight(0.8f)
                        .padding(start = 5.dp, end = 5.dp),
                    isError = speedPercent1Error,
                    errorSupportingText = "Please enter a valid number below the next highest level",
                    compValLow = speedPercent1Input.toInt(),
                    compValHigh = currentConfig.speedPercentLevel2
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
                    label = "Below target",
                    initialValue = currentConfig.speedPercentLevel2,
                    units = "%",
                    onValueParsed = { newValueString, parsedValue, isValid ->
                        speedPercent2Input = newValueString
                        speedPercent2Error = !isValid
                        if (isValid && parsedValue != null) {
                            currentConfig = currentConfig.copy(speedPercentLevel2 = parsedValue)
                        }
                    },
                    modifier = Modifier
                        .weight(0.8f)
                        .padding(start = 5.dp, end = 5.dp),
                    isError = speedPercent2Error,
                    errorSupportingText = "Please enter a valid number below the next highest level",
                    compValLow = speedPercent2Input.toInt(),
                    compValHigh = currentConfig.speedPercentMiddleTargetLow
                )
            }
            Row(
                modifier = Modifier.fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .weight(0.2f)
                        .aspectRatio(1f)
                        .border(BorderStroke(1.dp, Color.Black), CircleShape)
                )
                PercentConfigField(
                    label = "Target low",
                    initialValue = currentConfig.speedPercentMiddleTargetLow,
                    units = "%",
                    onValueParsed = { newValueString, parsedValue, isValid ->
                        speedPercentTargetLowInput = newValueString
                        speedPercentTargetLowError = !isValid
                        if (isValid && parsedValue != null) {
                            currentConfig = currentConfig.copy(speedPercentMiddleTargetLow = parsedValue)
                        }
                    },
                    modifier = Modifier
                        .weight(0.4f)
                        .padding(start = 5.dp, end = 5.dp),
                    isError = speedPercentTargetLowError,
                    errorSupportingText = "Please enter a valid number below the next highest level",
                    compValLow = speedPercentTargetLowInput.toInt(),
                    compValHigh = currentConfig.speedPercentMiddleTargetLow
                )
                PercentConfigField(
                    label = "Target high",
                    initialValue = currentConfig.speedPercentMiddleTargetHigh,
                    units = "%",
                    onValueParsed = { newValueString, parsedValue, isValid ->
                        speedPercentTargetHighInput = newValueString
                        speedPercentTargetHighError = !isValid
                        if (isValid && parsedValue != null) {
                            currentConfig = currentConfig.copy(speedPercentMiddleTargetHigh = parsedValue)
                        }
                    },
                    modifier = Modifier
                        .weight(0.4f)
                        .padding(start = 5.dp, end = 5.dp),
                    isError = speedPercentTargetHighError,
                    errorSupportingText = "Please enter a valid number below the next highest level",
                    compValLow = speedPercentTargetHighInput.toInt(),
                    compValHigh = currentConfig.speedPercentMiddleTargetHigh
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
                    label = "Above target",
                    initialValue = currentConfig.speedPercentLevel4,
                    units = "%",
                    onValueParsed = { newValueString, parsedValue, isValid ->
                        speedPercent4Input = newValueString
                        speedPercent4Error = !isValid
                        if (isValid && parsedValue != null) {
                            currentConfig = currentConfig.copy(speedPercentLevel4 = parsedValue)
                        }
                    },
                    modifier = Modifier
                        .weight(0.8f)
                        .padding(start = 5.dp, end = 5.dp),
                    isError = speedPercent4Error,
                    errorSupportingText = "Please enter a valid number below the next highest level",
                    compValLow = speedPercent4Input.toInt(),
                    compValHigh = currentConfig.speedPercentLevel4
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
                    label = "Well above target",
                    initialValue = currentConfig.speedPercentLevel5,
                    units = "%",
                    onValueParsed = { newValueString, parsedValue, isValid ->
                        speedPercent5Input = newValueString
                        speedPercent5Error = !isValid
                        if (isValid && parsedValue != null) {
                            currentConfig = currentConfig.copy(speedPercentLevel5 = parsedValue)
                        }
                    },
                    modifier = Modifier
                        .weight(0.8f)
                        .padding(start = 5.dp, end = 5.dp),
                    isError = speedPercent5Error,
                    errorSupportingText = "Please enter a valid number below the next highest level",
                    compValLow = speedPercent5Input.toInt(),
                    compValHigh = currentConfig.speedPercentLevel5
                )
            }
            // Save Button
            FilledTonalButton(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .height(50.dp)
                    .padding(vertical = 8.dp),
                onClick = {
                    val configToSave = currentConfig

                    if (checkConfigData(configToSave)) {
                        coroutineScope.launch {
                            Timber.d("Attempting to save config: $configToSave")
                            configManager.saveConfig(configToSave)
                            Timber.i("Configuration save initiated.")
                        }
                    } else {
                        Timber.w("Save blocked due to input validation errors.")
                    }
                },
                enabled = checkConfigData(currentConfig)
            ) {
                Icon(Icons.Default.Check, contentDescription = "Save")
                Spacer(modifier = Modifier.width(5.dp))
                Text("Save")
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

@Preview(name = "karoo", device = "spec:width=480px,height=800px,dpi=300")
@Composable
private fun PreviewTabLayout() {
    MainScreen()
}
