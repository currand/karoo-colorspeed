package com.currand60.karoocolorspeed.data

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.ColorFilter
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.FontFamily
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import io.hammerhead.karooext.models.ViewConfig
import kotlin.math.roundToInt
import com.currand60.karoocolorspeed.R

@SuppressLint("RestrictedApi")
@Composable
fun ColorSpeedView(
    context: Context,
    currentSpeed: Double,
    averageSpeed: Double,
    config: ViewConfig,
    title: String,
    description: String
) {

    val speedPercentageOfAverage: Double = if (currentSpeed > 0 && averageSpeed > 0) {
        (currentSpeed / averageSpeed) * 100.0
    } else {
        0.0
    }

    val alignment: androidx.glance.text.TextAlign = when (config.alignment) {
        ViewConfig.Alignment.CENTER -> androidx.glance.text.TextAlign.Center
        ViewConfig.Alignment.LEFT -> androidx.glance.text.TextAlign.Start
        ViewConfig.Alignment.RIGHT -> androidx.glance.text.TextAlign.End
    }

    val backgroundColor = when {
        currentSpeed <= 1.0 -> Color(context.getColor(R.color.hh_light_blue))
        speedPercentageOfAverage < 65.0 -> Color(context.getColor(R.color.hh_red))
        speedPercentageOfAverage < 95.0 -> Color(context.getColor(R.color.hh_yellow))
        speedPercentageOfAverage < 105.0 -> Color(context.getColor(R.color.hh_light_blue))
        else -> Color(context.getColor(R.color.hh_green))
    }

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(8.dp)
            .background(backgroundColor)
    ) {
        Row(
            modifier = GlanceModifier
                .height(20.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                provider = ImageProvider(
                    resId = R.drawable.icon_gauge,
                ),
                contentDescription = description,
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(ColorProvider(R.color.text_color)),
            )
            Text(
                text = title.uppercase(),
                style = TextStyle(
                    color = ColorProvider(R.color.text_color),
                    fontSize = TextUnit(16f, TextUnitType.Sp),
                    textAlign = alignment,
                    fontFamily = FontFamily.SansSerif
                )
            )
        }
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .defaultWeight()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = GlanceModifier
                    .fillMaxWidth(),
                text = ((currentSpeed * 10.0).roundToInt() / 10.0).toString(),
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
        title = "Speed",
        description = "Stuff",
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
        title = "Speed",
        description = "Stuff",
        config = ViewConfig(
            alignment = ViewConfig.Alignment.CENTER,
            textSize = 54,
            gridSize = Pair(30,20),
            viewSize = Pair(478,214),
            preview = true
        )
    )
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 150, heightDp = 90)
@Composable
fun previewColorSpeedZero() {
    ColorSpeedView(
        context = LocalContext.current,
        currentSpeed = 90.5,
        averageSpeed = 0.0,
        title = "Speed",
        description = "Stuff",
        config = ViewConfig(
            alignment = ViewConfig.Alignment.CENTER,
            textSize = 54,
            gridSize = Pair(30,20),
            viewSize = Pair(478,214),
            preview = true,
        )
    )
}