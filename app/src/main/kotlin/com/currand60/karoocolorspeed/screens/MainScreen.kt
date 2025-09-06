package com.currand60.karoocolorspeed.screens


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.currand60.karoocolorspeed.data.ConfigData
import com.currand60.karoocolorspeed.managers.ConfigurationManager
import io.hammerhead.karooext.KarooSystemService
import timber.log.Timber

@Composable
fun MainScreen() {

    val context = LocalContext.current
    val configManager = ConfigurationManager(context)
    val karooSystem = KarooSystemService(context)

    val scrollState = rememberScrollState()
    val isScrolledToBottom = scrollState.value == scrollState.maxValue

    val currentConfig by remember { mutableStateOf(ConfigData.DEFAULT) }


    val loadedConfig by produceState(initialValue = ConfigData.DEFAULT, key1 = configManager) {
        Timber.d("Starting to load initial config via produceState.")
        value = configManager.getConfig()
        Timber.d("Initial config loaded: $value")
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column (
            modifier = Modifier
                .padding(10.dp)
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(text="W' Balance Settings",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
    }

}

@Preview(name = "karoo", device = "spec:width=480px,height=800px,dpi=300")
@Composable
private fun PreviewTabLayout() {
    MainScreen()
}
