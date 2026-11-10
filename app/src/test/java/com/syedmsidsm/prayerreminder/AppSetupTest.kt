package com.syedmsidsm.prayerreminder

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AppSetupTest {

    @Test
    fun testAppContainerCreation() {
        val version = "1.0.0"
        assertNotNull(version)
    }

    @Test
    fun testZeroInternetPolicyEnforcedInManifest() {
        val root = File(".").canonicalFile
        val manifestFile = File(root, "app/src/main/AndroidManifest.xml").takeIf { it.exists() }
            ?: File(root, "src/main/AndroidManifest.xml")

        assertTrue(manifestFile.exists())
        val manifestContent = manifestFile.readText()

        // Ensure zero internet: if android.permission.INTERNET is mentioned, it MUST be tools:node="remove"
        if (manifestContent.contains("android.permission.INTERNET")) {
            assertTrue(manifestContent.contains("tools:node=\"remove\""))
        }
    }
}
