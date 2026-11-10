package com.syedmsidsm.prayerreminder.domain

object HijriNames {
    val englishMonths = listOf(
        "Muharram", "Safar", "Rabi al-Awwal", "Rabi al-Thani",
        "Jumada al-Ula", "Jumada al-Akhirah", "Rajab", "Sha'ban",
        "Ramadan", "Shawwal", "Dhul Qa'dah", "Dhul Hijjah"
    )

    val arabicMonths = listOf(
        "محرم", "صفر", "ربيع الأول", "ربيع الآخر",
        "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
        "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
    )

    val urduMonths = listOf(
        "محرم", "صفر", "ربیع الاول", "ربیع الثانی",
        "جمادی الاول", "جمادی الثانی", "رجب", "شعبان",
        "رمضان", "شوال", "ذوالقعدہ", "ذوالحجہ"
    )

    fun getMonthName(monthNumber: Int, languageCode: String): String {
        val index = (monthNumber - 1).coerceIn(0, 11)
        return when (languageCode) {
            "ar" -> arabicMonths[index]
            "ur" -> urduMonths[index]
            else -> englishMonths[index]
        }
    }
}
