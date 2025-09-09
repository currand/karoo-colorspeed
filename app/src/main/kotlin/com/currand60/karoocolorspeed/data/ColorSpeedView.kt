package com.currand60.karoocolorspeed.data

import android.R.attr.contentDescription
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
import androidx.glance.layout.fillMaxHeight
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
    colorConfig: ConfigData,
    titleResource: String,
    description: String
) {

    val barConfig = SpeedArrows.DEFAULT

    val speedPercentageOfAverage: Int = if (currentSpeed > 0 && averageSpeed > 0) {
        ((currentSpeed / averageSpeed) * 100.0).toInt()
    } else {
        0
    }

    val alignment: androidx.glance.text.TextAlign = when (config.alignment) {
        ViewConfig.Alignment.CENTER -> androidx.glance.text.TextAlign.Center
        ViewConfig.Alignment.LEFT -> androidx.glance.text.TextAlign.Start
        ViewConfig.Alignment.RIGHT -> androidx.glance.text.TextAlign.End
    }

    val (backgroundColor, textColor) = if (colorConfig.useBackgroundColors) {
        when {
            currentSpeed <= colorConfig.stoppedValue -> Pair(Color.Transparent, Color(context.getColor(R.color.text_color)))
            speedPercentageOfAverage < colorConfig.speedPercentLevel1 -> Pair(Color(context.getColor(R.color.dark_red)), Color(context.getColor(R.color.text_color_max)))
            speedPercentageOfAverage < colorConfig.speedPercentLevel2 -> Pair(Color(context.getColor(R.color.orange)), Color(context.getColor(R.color.text_color)))
            speedPercentageOfAverage in (colorConfig.speedPercentMiddleTargetLow..colorConfig.speedPercentMiddleTargetHigh) -> Pair(Color(context.getColor(R.color.middle)), Color(context.getColor(R.color.text_color)))
            speedPercentageOfAverage < colorConfig.speedPercentLevel4 -> Pair(Color(context.getColor(R.color.light_green)), Color(context.getColor(R.color.text_color)))
            speedPercentageOfAverage < colorConfig.speedPercentLevel5 -> Pair(Color(context.getColor(R.color.dark_green)), Color(context.getColor(R.color.text_color_max)))
            else -> Pair(Color(context.getColor(R.color.dark_green)), Color(context.getColor(R.color.text_color_max)))
        }
    } else {
        Pair(Color.Transparent, Color(context.getColor(R.color.text_color)))
    }

    val barLevel: Int = when {
        currentSpeed <= colorConfig.stoppedValue -> 0
        speedPercentageOfAverage < colorConfig.speedPercentLevel1 -> 1
        speedPercentageOfAverage < colorConfig.speedPercentLevel2 -> 2
        speedPercentageOfAverage in (colorConfig.speedPercentMiddleTargetLow..colorConfig.speedPercentMiddleTargetHigh) -> 0
        speedPercentageOfAverage < colorConfig.speedPercentLevel4 -> 3
        speedPercentageOfAverage < colorConfig.speedPercentLevel5 -> 4
        else -> 5
    }



    val finalTitle: String = if (config.gridSize.first == 60) {
        val titleId = context.resources.getIdentifier(titleResource, "string", context.packageName)
        val title = context.getString(titleId)
        title.uppercase()
    } else {
        val titleId = context.resources.getIdentifier("${titleResource}_short", "string", context.packageName)
        val title = context.getString(titleId)
        title.uppercase()
    }

    val finalTextSize: Float = if (colorConfig.useArrows) {
        config.textSize.toFloat() - 10f
    } else {
        config.textSize.toFloat()
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
                colorFilter = ColorFilter.tint(ColorProvider(textColor)),
            )
            Text(
                text = finalTitle.uppercase(),
                style = TextStyle(
                    color = ColorProvider(textColor),
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
            horizontalAlignment = Alignment.Start
        ) {
            if (colorConfig.useArrows)
            {
                ArrowProvider(
                    context = context,
                    level = barLevel,
                    color = textColor
                )

            }
            Text(
                modifier = GlanceModifier
                    .defaultWeight(),
                text = ((currentSpeed * 10.0).roundToInt() / 10.0).toString(),
                style = TextStyle(
                    color = ColorProvider(textColor),
                    fontSize = TextUnit(finalTextSize, TextUnitType.Sp),
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
fun PreviewColorSpeedUnder10() {
    ColorSpeedView(
        context = LocalContext.current,
        currentSpeed = 4.0,
        averageSpeed = 100.0,
        titleResource = "lap_speed_title",
        description = "Stuff",
        config = ViewConfig(
            alignment = ViewConfig.Alignment.RIGHT,
            textSize = 50,
            gridSize = Pair(30,15),
            viewSize = Pair(238,148),
            preview = true
        ),
        colorConfig = ConfigData.DEFAULT
    )
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 150, heightDp = 90)
@Composable
fun PreviewColorSpeedUnder() {
    ColorSpeedView(
        context = LocalContext.current,
        currentSpeed = 55.0,
        averageSpeed = 100.0,
        titleResource = "lap_speed_title",
        description = "Stuff",
        config = ViewConfig(
            alignment = ViewConfig.Alignment.RIGHT,
            textSize = 50,
            gridSize = Pair(30,15),
            viewSize = Pair(238,148),
            preview = true
        ),
        colorConfig = ConfigData.DEFAULT
    )
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 150, heightDp = 90)
@Composable
fun PreviewColorSpeedOrange() {
    ColorSpeedView(
        context = LocalContext.current,
        currentSpeed = 75.0,
        averageSpeed = 100.0,
        titleResource = "avg_speed_title",
        description = "Stuff",
        config = ViewConfig(
            alignment = ViewConfig.Alignment.CENTER,
            textSize = 54,
            gridSize = Pair(30,20),
            viewSize = Pair(478,214),
            preview = true
        ),
        colorConfig = ConfigData.DEFAULT
    )
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 150, heightDp = 90)
@Composable
fun PreviewColorSpeedSteady() {
    ColorSpeedView(
        context = LocalContext.current,
        currentSpeed = 52.5,
        averageSpeed = 50.0,
        titleResource = "speed_title",
        description = "Stuff",
        config = ViewConfig(
            alignment = ViewConfig.Alignment.LEFT,
            textSize = 54,
            gridSize = Pair(60,20),
            viewSize = Pair(478,214),
            preview = true,
        ),
        colorConfig = ConfigData.DEFAULT
    )
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 150, heightDp = 90)
@Composable
fun PreviewColorSpeedOver() {
    ColorSpeedView(
        context = LocalContext.current,
        currentSpeed = 125.5,
        averageSpeed = 100.0,
        titleResource = "lap_speed_title",
        description = "Stuff",
        config = ViewConfig(
            alignment = ViewConfig.Alignment.RIGHT,
            textSize = 45,
            gridSize = Pair(30,15),
            viewSize = Pair(238,148),
            preview = true
        ),
        colorConfig = ConfigData.DEFAULT
    )
}
@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 150, heightDp = 90)
@Composable
fun PreviewNoBackgroundColors() {
    ColorSpeedView(
        context = LocalContext.current,
        currentSpeed = 125.5,
        averageSpeed = 100.0,
        titleResource = "lap_speed_title",
        description = "Stuff",
        config = ViewConfig(
            alignment = ViewConfig.Alignment.RIGHT,
            textSize = 45,
            gridSize = Pair(30, 15),
            viewSize = Pair(238, 148),
            preview = true
        ),
        colorConfig = ConfigData.DEFAULT.copy(useBackgroundColors = false)
    )
}

