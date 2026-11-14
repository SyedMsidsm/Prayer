package com.syedmsidsm.prayerreminder.ui.onboarding

import android.annotation.SuppressLint
import android.provider.Settings
import android.view.MotionEvent
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay

/**
 * Brand Landing Animation Screen – Sanctuary Mosque Edition.
 * Full-bleed immersive display of the official Sanctuary mosque emblem animation.
 * Features exact SVG vector paths, stroke-draw squircle, dawn glow, rising dome & minarets,
 * falling gold balconies/finials, springing crescent moon, twinkling celestial stars,
 * illuminating arched sanctuary window, and cascading brand title with tagline.
 * Supports instant tap-to-skip and automatic seamless transition.
 */
@SuppressLint("SetJavaScriptEnabled")
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

    var isFinished by remember { mutableStateOf(false) }
    val finishOnce = remember(onFinished) {
        {
            if (!isFinished) {
                isFinished = true
                onFinished()
            }
        }
    }

    LaunchedEffect(animationsEnabled) {
        if (!animationsEnabled) {
            delay(500)
            finishOnce()
            return@LaunchedEffect
        }
        // Safety timeout fallback: transition at 3.5s if not already skipped
        delay(3500)
        finishOnce()
    }

    // Force LTR layout direction so mosque logo elements (crescent, minarets) are never mirrored in RTL
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF030A0E))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { finishOnce() }
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    WebView(ctx).apply {
                        setBackgroundColor(0xFF030A0E.toInt())
                        setLayerType(View.LAYER_TYPE_HARDWARE, null)
                        isHorizontalScrollBarEnabled = false
                        isVerticalScrollBarEnabled = false
                        overScrollMode = View.OVER_SCROLL_NEVER

                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            useWideViewPort = false
                            loadWithOverviewMode = false
                            cacheMode = WebSettings.LOAD_DEFAULT
                            allowFileAccess = true
                        }

                        addJavascriptInterface(object {
                            @JavascriptInterface
                            fun onFinished() {
                                post { finishOnce() }
                            }
                        }, "Android")

                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                view?.setBackgroundColor(0xFF030A0E.toInt())
                            }
                        }

                        setOnTouchListener { _, event ->
                            if (event.action == MotionEvent.ACTION_UP) {
                                finishOnce()
                            }
                            false
                        }

                        loadUrl("file:///android_asset/prayerreminder-landing.html")
                    }
                },
                update = { webView ->
                    // Keep background solid dark AMOLED black
                    webView.setBackgroundColor(0xFF030A0E.toInt())
                }
            )
        }
    }
}
