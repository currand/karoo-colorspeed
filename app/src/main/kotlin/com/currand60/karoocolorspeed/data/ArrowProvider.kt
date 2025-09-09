package com.currand60.karoocolorspeed.data

import android.annotation.SuppressLint
import com.currand60.karoocolorspeed.R
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.layout.wrapContentSize
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.unit.ColorProvider
import io.hammerhead.karooext.models.ViewConfig


@SuppressLint("RestrictedApi")
@Composable
fun ArrowProvider (
    context: Context,
    level: Int,
    color: Color
){
    val barConfig = SpeedArrows.DEFAULT

    val arrowModifier = GlanceModifier.width(28.dp).height(48.dp)
    val barModifier = GlanceModifier.width(28.dp).height(48.dp).padding(1.dp)

    when (level) {
        0 ->
            Image(
                modifier = barModifier,
                provider = ImageProvider(
                    resId = R.drawable.stat_0_24px,
                ),
                contentDescription = "Target",
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.tint(ColorProvider(color))
            )
        1 ->
            Image(
                modifier = arrowModifier,
                provider = ImageProvider(
                    resId = barConfig.level1.first,
                ),
                contentDescription = "Target",
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.tint(ColorProvider(color))
            )
        2 ->
            Image(
                modifier = arrowModifier,
                provider = ImageProvider(
                    resId = barConfig.level2.first,
                ),
                contentDescription = "Target",
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.tint(ColorProvider(color))
            )
        3 ->
            Image(
                modifier = arrowModifier,
                provider = ImageProvider(
                    resId = barConfig.level3.first,
                ),
                contentDescription = "Target",
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.tint(ColorProvider(color))
            )

        in (4..5) ->
            Image(
                modifier = arrowModifier,
                provider = ImageProvider(
                    resId = barConfig.level4.first,
                ),
                contentDescription = "Target",
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.tint(ColorProvider(color))
            )

    }
}

@SuppressLint("RestrictedApi")
@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 150, heightDp = 90)
@Composable
fun PreviewBarsTarget(){
    val context = LocalContext.current

    ArrowProvider(context = context,
        level = 0,
        color = Color(context.getColor(R.color.white))
    )
}

@SuppressLint("RestrictedApi")
@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 150, heightDp = 90)
@Composable
fun PreviewBars1(){
    val context = LocalContext.current
    ArrowProvider(context = context,
        level = 1,
        color = Color(context.getColor(R.color.black))

    )
}