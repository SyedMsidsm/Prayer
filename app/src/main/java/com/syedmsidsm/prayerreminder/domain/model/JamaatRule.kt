package com.syedmsidsm.prayerreminder.domain.model

import java.time.LocalTime

sealed interface JamaatRule {
    data class AfterAzan(val minutes: Int = 15) : JamaatRule
    data class Fixed(val time: LocalTime) : JamaatRule
}
