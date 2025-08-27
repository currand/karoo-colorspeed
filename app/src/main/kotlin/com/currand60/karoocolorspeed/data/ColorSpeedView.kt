package com.currand60.karoocolorspeed.data

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.FontFamily
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import io.hammerhead.karooext.models.ViewConfig
import com.currand60.karoocolorspeed.R

@SuppressLint("RestrictedApi")
@Composable
fun ColorSpeedView(
    context: Context,
    currentSpeed: Double,
    averageSpeed: Double,
    config: ViewConfig
) {
    val speedPercentageOfAverage: Double = if (averageSpeed > 0) {
        (currentSpeed / averageSpeed) * 100.0
    } else {
        if (currentSpeed > 0) 200.0 else 100.0 // Arbitrary high value for "well above"
    }

    val alignment: androidx.glance.text.TextAlign = when (config.alignment) {
        ViewConfig.Alignment.CENTER -> androidx.glance.text.TextAlign.Center
        ViewConfig.Alignment.LEFT -> androidx.glance.text.TextAlign.Start
        ViewConfig.Alignment.RIGHT -> androidx.glance.text.TextAlign.End
    }

    val backgroundColor = when {
        // Current speed is significantly below average (e.g., less than 85% of average)
        speedPercentageOfAverage < 85.0 -> Color(context.getColor(R.color.red))
        // Current speed is somewhat below average (e.g., between 85% and 95% of average)
        speedPercentageOfAverage < 95.0 -> Color(context.getColor(R.color.yellow))
        // Current speed is at or above average (e.g., 95% or more of average)
        else -> Color(context.getColor(R.color.green))
    }

    val width: Double = (config.gridSize.second / 60.0) * config.viewSize.first
    val height: Double = (config.gridSize.first / 60.0) * config.viewSize.second

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(8.dp)
    ) {
        Row(
            modifier = GlanceModifier
                .height(24.dp)
                .background(backgroundColor)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                provider = ImageProvider(
                    resId = R.drawable.icon_gauge
                ),
                contentDescription = context.getString(R.string.speed_title),
                modifier = GlanceModifier
                    .fillMaxHeight()
            )
            Text(
                text = context.getString(R.string.speed_title).uppercase(),
                style = TextStyle(
                    color = ColorProvider(R.color.text_color),
                    fontSize = TextUnit(18f, TextUnitType.Sp),
                    textAlign = alignment,
                    fontFamily = FontFamily.SansSerif
                )
            )
        }
        Row(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(backgroundColor),
//            verticalAlignment = Alignment.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = GlanceModifier.fillMaxWidth(),
                text = currentSpeed.toString(),
                style = TextStyle(
                    color = ColorProvider(R.color.text_color),
                    fontSize = TextUnit(config.textSize.toFloat(), TextUnitType.Sp),
                    textAlign = alignment,
                    fontFamily = FontFamily.Monospace,
                )
            )
        }
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 150, heightDp = 90)
@Composable
fun previewColorSpeedUnder() {
    ColorSpeedView(
        context = LocalContext.current,
        currentSpeed = 40.5,
        averageSpeed = 50.95,
        config = ViewConfig(
            alignment = ViewConfig.Alignment.RIGHT,
            textSize = 50,
            gridSize = Pair(30,15),
            viewSize = Pair(238,148),
            preview = true
        )
    )
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 150, heightDp = 90)
@Composable
fun previewColorSpeedOver() {
    ColorSpeedView(
        context = LocalContext.current,
        currentSpeed = 50.95,
        averageSpeed = 40.5,
        config = ViewConfig(
            alignment = ViewConfig.Alignment.CENTER,
            textSize = 54,
            gridSize = Pair(30,20),
            viewSize = Pair(478,214),
            preview = true
        )
    )
}