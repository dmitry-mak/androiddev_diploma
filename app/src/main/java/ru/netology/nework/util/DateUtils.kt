package ru.netology.nework.util

import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.Instant

object DateUtils {

    private val timeFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
    private val dateFormat = DateTimeFormatter.ofPattern("dd MMM yyyy")

    private val timeShortFormat = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm")
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

    fun formatTimeShort(inputTime: String): String =
        try {
            timeShortFormat.withZone(ZoneId.systemDefault()).format(Instant.parse(inputTime))
        } catch (e: Exception) {
            inputTime
        }

    fun formatMillisForInput(millis: Long): String=
        timeFormat.withZone(ZoneId.systemDefault()).format(Instant.ofEpochMilli(millis))
}