package ru.netology.nework.util

import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.Instant

object DateUtils {

    private val timeFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
    private val dateFormat = DateTimeFormatter.ofPattern("dd MMM yyyy")

    fun formatTimeForOutput(inputTime: String): String =
        try {
            timeFormat.withZone(ZoneId.systemDefault()).format(Instant.parse(inputTime))
        } catch (e: Exception) {
            inputTime
        }

    fun formatDateForOutput(inputDate: String): String =
        try {
            dateFormat.withZone(ZoneId.systemDefault()).format(Instant.parse(inputDate))
        } catch (e: Exception) {
            inputDate
        }
}