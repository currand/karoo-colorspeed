package com.currand60.karoocolorspeed.data

import android.annotation.SuppressLint
import androidx.compose.material.icons.Icons
import com.currand60.karoocolorspeed.R
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.layout.ContentScale
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.unit.ColorProvider


@SuppressLint("RestrictedApi")
@Composable
fun ArrowProvider (
    modifier: GlanceModifier = GlanceModifier,
    level: Int,
    color: Color,
){
    when (level) {
        0 ->
            Icons.Default
        1 ->
            Image(
                modifier = modifier,
                provider = ImageProvider(
                    resId = R.drawable.stat_minus_2_24px,
                ),
                contentDescription = "Target",
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(ColorProvider(color))
            )
        2 ->
            Image(
                modifier = modifier,
                provider = ImageProvider(
                    resId = R.drawable.stat_minus_1_24px,
                ),
                contentDescription = "Target",
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(ColorProvider(color))
            )
        3 ->
            Image(
                modifier = modifier,
                provider = ImageProvider(
                    resId = R.drawable.stat_0_24px,
                ),
                contentDescription = "Target",
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(ColorProvider(color))
            )

        4 ->
            Image(
                modifier = modifier,
                provider = ImageProvider(
                    resId = R.drawable.stat_1_24px,
                ),
                contentDescription = "Target",
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(ColorProvider(color))
            )
        5 ->
            Image(
                modifier = modifier,
                provider = ImageProvider(
                    resId = R.drawable.stat_2_24px,
                ),
                contentDescription = "Target",
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(ColorProvider(color))
            )

    }
}

@Suppress("unused")
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

@Suppress("unused")
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

@Suppress("unused")
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