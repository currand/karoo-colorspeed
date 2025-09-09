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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import timber.log.Timber

@Composable
fun PercentConfigField(
    label: String,
    initialValue: Int,
    units: String,
    onValueParsed: (newValueString: String, parsedValue: Int?, isValid: Boolean) -> Unit,
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
            val parsedValue = newValue.toIntOrNull() // Attempt to parse to Int
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
fun MainScreen() {

    val context = LocalContext.current
    val configManager = ConfigurationManager(context)
    val coroutineScope = rememberCoroutineScope()
    val karooSystem = KarooSystemService(context)

    val scrollState = rememberScrollState()
    val isScrolledToBottom = scrollState.value == scrollState.maxValue

    var originalConfig by remember { mutableStateOf(ConfigData.DEFAULT) }
    var currentConfig by remember { mutableStateOf(ConfigData.DEFAULT) }

    var stoppedError by remember { mutableStateOf(false) }
    var speedPercent1Error by remember { mutableStateOf(false) }
    var speedPercent2Error by remember { mutableStateOf(false) }
    var speedPercentTargetLowError by remember { mutableStateOf(false) }
    var speedPercentTargetHighError by remember { mutableStateOf(false) }
    var speedPercent4Error by remember { mutableStateOf(false) }
    var speedPercent5Error by remember { mutableStateOf(false) }
    
    var useTargetSpeed by remember { mutableStateOf(false) }

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

    val configIsGood by remember (currentConfig) {
        derivedStateOf {
            currentConfig.validate()
        }
    }

    LaunchedEffect(loadedConfig) {
        originalConfig = loadedConfig
        currentConfig = loadedConfig
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
                text = "Color Speed Settings",
                textAlign = TextAlign.Left,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Enter the percentage of either average or target speed " +
                        "for this level and color",
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
                PercentConfigField(
                    label = "Stopped",
                    initialValue = currentConfig.stoppedValue,
                    prefix = "<",
                    units = when (karooDistanceUnit) {
                        UserProfile.PreferredUnit.UnitType.IMPERIAL -> "mph"
                        else -> "kmh"
                    },
                    onValueParsed = { newValueString, parsedValue, isValid ->
                        stoppedError = !isValid
                        if (isValid && parsedValue != null) {
                            val attemptedConfig = currentConfig.copy(stoppedValue = parsedValue)
                            if (attemptedConfig.validate()) {
                                currentConfig = currentConfig.copy(stoppedValue = parsedValue)
                        } else {
                            stoppedError = true
                        }
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
                    prefix = "<",
                    units = "%",
                    onValueParsed = { newValueString, parsedValue, isValid ->
                        speedPercent1Error = !isValid
                        if (isValid && parsedValue != null) {
                            val attemptedConfig = currentConfig.copy(speedPercentLevel1 = parsedValue)
                            if (attemptedConfig.validate()) {
                                currentConfig = currentConfig.copy(speedPercentLevel1 = parsedValue)
                            } else {
                                speedPercent1Error = true
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(0.8f)
                        .padding(start = 5.dp, end = 5.dp),
                    isError = speedPercent1Error,
                    errorSupportingText = "Please enter a valid number below the next highest level",
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
                        speedPercent2Error = !isValid
                        if (isValid && parsedValue != null) {
                            val attemptedConfig = currentConfig.copy(speedPercentLevel2 = parsedValue)
                            if (attemptedConfig.validate()) {
                                currentConfig = currentConfig.copy(speedPercentLevel2 = parsedValue)
                            } else {
                                speedPercent2Error = true
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(0.8f)
                        .padding(start = 5.dp, end = 5.dp),
                    isError = speedPercent2Error,
                    errorSupportingText = "Please enter a valid number below the next highest level",
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
                    label = "Target low",
                    initialValue = currentConfig.speedPercentMiddleTargetLow,
                    units = "%",
                    onValueParsed = { newValueString, parsedValue, isValid ->
                        speedPercentTargetLowError = !isValid
                        if (isValid && parsedValue != null) {
                            val attemptedConfig = currentConfig.copy(speedPercentMiddleTargetLow = parsedValue)
                            if (attemptedConfig.validate()) {
                                currentConfig = currentConfig.copy(speedPercentMiddleTargetLow = parsedValue)
                            } else {
                                speedPercentTargetLowError = true
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(0.4f)
                        .padding(start = 5.dp, end = 5.dp),
                    isError = speedPercentTargetLowError,
                    errorSupportingText = "Please enter a valid number below the next highest level",
                )
                PercentConfigField(
                    label = "Target high",
                    initialValue = currentConfig.speedPercentMiddleTargetHigh,
                    units = "%",
                    onValueParsed = { newValueString, parsedValue, isValid ->
                        speedPercentTargetHighError = !isValid
                        if (isValid && parsedValue != null) {
                            val attemptedConfig = currentConfig.copy(speedPercentMiddleTargetHigh = parsedValue)
                            if (attemptedConfig.validate()) {
                                currentConfig = currentConfig.copy(speedPercentMiddleTargetHigh = parsedValue)
                            } else {
                                speedPercentTargetHighError = true
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(0.4f)
                        .padding(start = 5.dp, end = 5.dp),
                    isError = speedPercentTargetHighError,
                    errorSupportingText = "Please enter a valid number below the next highest level",
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
                        speedPercent4Error = !isValid
                        if (isValid && parsedValue != null) {
                            val attemptedConfig = currentConfig.copy(speedPercentLevel4 = parsedValue)
                            if (attemptedConfig.validate()) {
                                currentConfig = currentConfig.copy(speedPercentLevel4 = parsedValue)
                            } else {
                                speedPercent4Error = true
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(0.8f)
                        .padding(start = 5.dp, end = 5.dp),
                    isError = speedPercent4Error,
                    errorSupportingText = "Please enter a valid number below the next highest level",
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
                    prefix = ">",
                    units = "%",
                    onValueParsed = { newValueString, parsedValue, isValid ->
                        speedPercent5Error = !isValid
                        if (isValid && parsedValue != null) {
                            val attemptedConfig = currentConfig.copy(speedPercentLevel5 = parsedValue)
                            if (attemptedConfig.validate()) {
                                currentConfig = currentConfig.copy(speedPercentLevel5 = parsedValue)
                            } else {
                                speedPercent5Error = true
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(0.8f)
                        .padding(start = 5.dp, end = 5.dp),
                    isError = speedPercent5Error,
                    errorSupportingText = "Please enter a valid number below the next highest level",
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
                    text = "Display arrows next to the speed values?",
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
                    text = "Change background colors based on speed?",
                )
            }
            Row(modifier = Modifier.fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically
            )
            {
                Switch(
                    checked = currentConfig.useTargetSpeed,
                    onCheckedChange = { isChecked ->
                        useTargetSpeed = isChecked
                        currentConfig = currentConfig.copy(useTargetSpeed = isChecked)
                    }
                )
                Text(
                    modifier = Modifier
                        .padding(start = 5.dp)
                        .align(Alignment.CenterVertically),
                    text = "Use a target speed instead of the average speed?",
                )
            }
            OutlinedTextField(
                value = currentConfig.targetSpeed.toString(),
                onValueChange = { newValue ->
                    val parsedValue = newValue.toIntOrNull()
                    if (parsedValue != null && parsedValue >= 0) {
                        currentConfig = currentConfig.copy(targetSpeed = parsedValue)
                    }
                },
                label = { Text("Target Speed") },
                placeholder = { Text(currentConfig.targetSpeed.toString()) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                enabled = useTargetSpeed
            )


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
