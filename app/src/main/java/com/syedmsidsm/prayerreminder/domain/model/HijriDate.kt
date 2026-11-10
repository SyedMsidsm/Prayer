package com.syedmsidsm.prayerreminder.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class HijriDate(
    val day: Int,
    val month: Int,
    val monthName: String,
    val year: Int
)
