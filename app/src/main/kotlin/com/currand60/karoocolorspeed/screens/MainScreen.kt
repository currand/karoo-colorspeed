package com.currand60.karoocolorspeed.screens


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.currand60.karoocolorspeed.R
import com.currand60.karoocolorspeed.data.ConfigData
import com.currand60.karoocolorspeed.extension.streamUserProfile
import com.currand60.karoocolorspeed.managers.ConfigurationManager
import io.hammerhead.karooext.KarooSystemService
import io.hammerhead.karooext.models.UserProfile
import kotlinx.coroutines.flow.distinctUntilChanged
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
    onValueParsed: (newValueString: String, parsedValue: Int?, isValid: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean,
    errorSupportingText: String? = null
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
            val isValid = parsedValue != null // Check if parsing was successful
            onValueParsed(newValue, parsedValue, isValid) // Notify the parent
        },
        label = { Text(text = label) },
        modifier = modifier,
        placeholder = { Text(initialValue.toString()) },
        suffix = { Text("%") },
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
    val karooSystem = KarooSystemService(context)

    var fieldHasChanges by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val isScrolledToBottom = scrollState.value == scrollState.maxValue

    var originalConfig by remember { mutableStateOf(ConfigData.DEFAULT) }
    var currentConfig by remember { mutableStateOf(ConfigData.DEFAULT) }
    var stoppedInput by remember { mutableStateOf(currentConfig.stoppedValue.toString()) }
    var stoppedError by remember { mutableStateOf(false) }

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
                OutlinedTextField(
                    value = stoppedInput, // Bound to the string input state
                    label= { Text(text="Stopped") },
                    modifier = Modifier
                        .weight(0.8f)
                        .padding(start = 5.dp, end = 5.dp),
                    onValueChange = { newValue ->
                        fieldHasChanges = (newValue != currentConfig.stoppedValue.toString())
                        stoppedInput = newValue // Always update the string state first
                        val parsedValue = newValue.toIntOrNull()
                        if (parsedValue != null) {
                            currentConfig =
                                currentConfig.copy(stoppedValue = parsedValue) // Update numerical state only if valid
                            stoppedError = false
                        } else {
                            // If parsing fails, set error, but the UI still shows the (invalid) newValue
                            stoppedError = true
                        }
                    },
                    placeholder = { Text("${currentConfig.stoppedValue}") },
                    suffix = {
                        when (karooDistanceUnit) {
                            UserProfile.PreferredUnit.UnitType.IMPERIAL -> Text("mp/h")
                            else -> Text("km/h")
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = stoppedError,
                    supportingText = {
                        if (stoppedError) {
                            Text("Please enter a valid number")
                        }
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
                OutlinedTextField(
                    value = stoppedInput, // Bound to the string input state
                    label= { Text(text="Well below target") },
                    modifier = Modifier
                        .weight(0.8f)
                        .padding(start = 5.dp, end = 5.dp),
                    onValueChange = { newValue ->
                        fieldHasChanges = (newValue != currentConfig.stoppedValue.toString())
                        stoppedInput = newValue // Always update the string state first
                        val parsedValue = newValue.toIntOrNull()
                        if (parsedValue != null) {
                            currentConfig =
                                currentConfig.copy(stoppedValue = parsedValue) // Update numerical state only if valid
                            stoppedError = false
                        } else {
                            // If parsing fails, set error, but the UI still shows the (invalid) newValue
                            stoppedError = true
                        }
                    },
                    placeholder = { Text("${currentConfig.stoppedValue}") },
                    suffix = {
                        when (karooDistanceUnit) {
                            UserProfile.PreferredUnit.UnitType.IMPERIAL -> Text("mp/h")
                            else -> Text("km/h")
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = stoppedError,
                    supportingText = {
                        if (stoppedError) {
                            Text("Please enter a valid number")
                        }
                    }
                )
            }
        }
    }
}

@Preview(name = "karoo", device = "spec:width=480px,height=800px,dpi=300")
@Composable
private fun PreviewTabLayout() {
    MainScreen()
}
