package com.syedmsidsm.prayerreminder.reliability

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReliabilityTest {

    @Test
    fun xiaomiGuidance_returnsAutostartAndBatterySaver() {
        val guidance = OemSettingsLauncher.getGuidanceForManufacturer("Xiaomi")
        assertTrue(guidance.isKnownBrand)
        assertEquals("Xiaomi / Redmi / POCO", guidance.brandName)
        assertTrue(guidance.steps.any { it.contains("Autostart") })
        assertTrue(guidance.steps.any { it.contains("No restrictions") })
    }

    @Test
    fun redmiGuidance_matchesXiaomiGroup() {
        val guidance = OemSettingsLauncher.getGuidanceForManufacturer("Redmi")
        assertTrue(guidance.isKnownBrand)
        assertEquals("Xiaomi / Redmi / POCO", guidance.brandName)
    }

    @Test
    fun pocoGuidance_matchesXiaomiGroup() {
        val guidance = OemSettingsLauncher.getGuidanceForManufacturer("POCO")
        assertTrue(guidance.isKnownBrand)
        assertEquals("Xiaomi / Redmi / POCO", guidance.brandName)
    }

    @Test
    fun samsungGuidance_returnsUnrestrictedAndSleepingApps() {
        val guidance = OemSettingsLauncher.getGuidanceForManufacturer("Samsung")
        assertTrue(guidance.isKnownBrand)
        assertEquals("Samsung", guidance.brandName)
        assertTrue(guidance.steps.any { it.contains("Unrestricted") })
        assertTrue(guidance.steps.any { it.contains("Sleeping") })
    }

    @Test
    fun oppoGuidance_returnsAutoLaunchAndBackgroundActivity() {
        val guidance = OemSettingsLauncher.getGuidanceForManufacturer("OPPO")
        assertTrue(guidance.isKnownBrand)
        assertEquals("OPPO / Realme / OnePlus", guidance.brandName)
        assertTrue(guidance.steps.any { it.contains("background activity") })
    }

    @Test
    fun realmeGuidance_matchesOppoGroup() {
        val guidance = OemSettingsLauncher.getGuidanceForManufacturer("Realme")
        assertTrue(guidance.isKnownBrand)
        assertEquals("OPPO / Realme / OnePlus", guidance.brandName)
    }

    @Test
    fun oneplusGuidance_matchesOppoGroup() {
        val guidance = OemSettingsLauncher.getGuidanceForManufacturer("OnePlus")
        assertTrue(guidance.isKnownBrand)
        assertEquals("OPPO / Realme / OnePlus", guidance.brandName)
    }

    @Test
    fun vivoGuidance_returnsHighBackgroundPower() {
        val guidance = OemSettingsLauncher.getGuidanceForManufacturer("Vivo")
        assertTrue(guidance.isKnownBrand)
        assertEquals("Vivo / iQOO", guidance.brandName)
        assertTrue(guidance.steps.any { it.contains("High background power") })
    }

    @Test
    fun iqooGuidance_matchesVivoGroup() {
        val guidance = OemSettingsLauncher.getGuidanceForManufacturer("iQOO")
        assertTrue(guidance.isKnownBrand)
        assertEquals("Vivo / iQOO", guidance.brandName)
    }

    @Test
    fun huaweiGuidance_returnsAppLaunchInstructions() {
        val guidance = OemSettingsLauncher.getGuidanceForManufacturer("Huawei")
        assertTrue(guidance.isKnownBrand)
        assertEquals("Huawei / Honor", guidance.brandName)
        assertTrue(guidance.steps.any { it.contains("App launch") })
    }

    @Test
    fun honorGuidance_matchesHuaweiGroup() {
        val guidance = OemSettingsLauncher.getGuidanceForManufacturer("Honor")
        assertTrue(guidance.isKnownBrand)
        assertEquals("Huawei / Honor", guidance.brandName)
    }

    @Test
    fun unknownManufacturer_returnsGenericStepsAndCapitalizedName() {
        val guidance = OemSettingsLauncher.getGuidanceForManufacturer("google")
        assertFalse(guidance.isKnownBrand)
        assertEquals("Google", guidance.brandName)
        assertTrue(guidance.steps.any { it.contains("PrayerReminder") })
        assertTrue(guidance.steps.any { it.contains("Don't optimize") || it.contains("Unrestricted") })
    }
}
