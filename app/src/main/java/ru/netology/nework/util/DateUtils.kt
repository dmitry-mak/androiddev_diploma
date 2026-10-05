package ru.netology.nework.util

import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.Instant
import java.util.Locale


object DateUtils {

    private val timeFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
    private val dateFormat = DateTimeFormatter.ofPattern("dd MMM yyyy")

    private val timeShortFormat = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm")

    private val jobDateFormat = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH)
    private val chipDateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy")


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

    fun formatJobPeriod(start: String, finish: String?): String = buildString {
        append(formatDateForOutput(start))
        append(" - ")
        append(finish?.let { formatDateForOutput(it) } ?: "НВ")
    }

    private fun formatJobDate(input: String): String = try {
        jobDateFormat.withZone(ZoneId.systemDefault()).format(Instant.parse(input))
    } catch (e: Exception) {
        input
    }

    fun formatChipDate(millis: Long): String =
        chipDateFormat.withZone(ZoneId.systemDefault()).format(
            Instant.ofEpochMilli(millis)
        )

    fun isoFromMillis(millis: Long): String = Instant.ofEpochMilli(millis).toString()

    fun formatMillisForInput(millis: Long): String =
        timeFormat.withZone(ZoneId.systemDefault()).format(Instant.ofEpochMilli(millis))
}