package com.syedmsidsm.prayerreminder.ui.onboarding

import androidx.compose.runtime.Composable
import com.syedmsidsm.prayerreminder.ui.landing.LandingScreen

/**
 * Brand Landing Animation Screen.
 * Full-bleed immersive display of the official mosque emblem animation.
 * Features exact SVG vector paths, crescent sweep, 5 prayer dots, gold pulse ring,
 * and cascading brand title with Arabic & Urdu subtitles.
 * Supports instant tap-to-skip and automatic seamless transition.
 */
@Composable
fun BrandIntroScreen(
    onFinished: () -> Unit
) {
    LandingScreen(onFinished = onFinished)
}
