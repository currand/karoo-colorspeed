package com.currand60.karoocolorspeed.data

import android.annotation.SuppressLint
import com.currand60.karoocolorspeed.R
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.layout.ContentScale
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.unit.ColorProvider


@SuppressLint("RestrictedApi")
@Composable
fun ArrowProvider (
    level: Int,
    color: Color
){
    val arrowModifier = GlanceModifier.width(28.dp).height(48.dp)
    val barModifier = GlanceModifier.width(28.dp).height(48.dp).padding(1.dp)

    when (level) {
        0 ->
            Image(
                modifier = barModifier,
                provider = ImageProvider(
                    resId = R.drawable.check_indeterminate_small_24px,
                ),
                contentDescription = "Target",
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.tint(ColorProvider(Color.Transparent))
            )
        1 ->
            Image(
                modifier = arrowModifier,
                provider = ImageProvider(
                    resId = R.drawable.stat_minus_2_24px,
                ),
                contentDescription = "Target",
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.tint(ColorProvider(color))
            )
        2 ->
            Image(
                modifier = arrowModifier,
                provider = ImageProvider(
                    resId = R.drawable.stat_minus_1_24px,
                ),
                contentDescription = "Target",
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.tint(ColorProvider(color))
            )
        3 ->
            Image(
                modifier = arrowModifier,
                provider = ImageProvider(
                    resId = R.drawable.stat_0_24px,
                ),
                contentDescription = "Target",
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.tint(ColorProvider(color))
            )

        4 ->
            Image(
                modifier = arrowModifier,
                provider = ImageProvider(
                    resId = R.drawable.stat_1_24px,
                ),
                contentDescription = "Target",
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.tint(ColorProvider(color))
            )
        5 ->
            Image(
                modifier = arrowModifier,
                provider = ImageProvider(
                    resId = R.drawable.stat_2_24px,
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

    ArrowProvider(
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
    ArrowProvider(
        level = 1,
        color = Color(context.getColor(R.color.black))

    )
}

@SuppressLint("RestrictedApi")
@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 150, heightDp = 90)
@Composable
fun PreviewBars3(){
    val context = LocalContext.current
    ArrowProvider(
        level = 3,
        color = Color(context.getColor(R.color.black))

    )
}