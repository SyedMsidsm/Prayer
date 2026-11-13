package com.syedmsidsm.prayerreminder.ui.onboarding

import android.net.Uri
import android.provider.Settings
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.syedmsidsm.prayerreminder.R
import kotlinx.coroutines.delay

private val DeepSanctuary = Color(0xFF03080C)

/**
 * Brand Landing Animation Screen.
 * Plays the official high-resolution brand animation video (R.raw.prayerreminder_landing)
 * provided by the design specification. Tapping anywhere allows instant skip.
 */
@Composable
fun BrandIntroScreen(
    onFinished: () -> Unit
) {
    val context = LocalContext.current
    val animatorDurationScale = Settings.Global.getFloat(
        context.contentResolver,
        Settings.Global.ANIMATOR_DURATION_SCALE,
        1.0f
    )
    val animationsEnabled = animatorDurationScale > 0f

    LaunchedEffect(animationsEnabled) {
        if (!animationsEnabled) {
            delay(500)
            onFinished()
        }
    }

    if (!animationsEnabled) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DeepSanctuary)
        )
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepSanctuary)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onFinished() },
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { ctx ->
                VideoView(ctx).apply {
                    val uri = Uri.parse("android.resource://${ctx.packageName}/${R.raw.prayerreminder_landing}")
                    setVideoURI(uri)
                    setOnPreparedListener { mp ->
                        mp.isLooping = false
                        start()
                    }
                    setOnCompletionListener {
                        onFinished()
                    }
                    setOnErrorListener { _, _, _ ->
                        onFinished()
                        true
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
