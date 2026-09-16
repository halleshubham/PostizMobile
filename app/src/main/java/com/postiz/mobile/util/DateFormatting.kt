package com.postiz.mobile.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

/**
 * The API always deals in UTC instants (`2025-01-01T10:00:00.000Z`). Every
 * display path must convert to the device's zone here rather than showing
 * the raw string, or "9am" silently means UTC 9am to a user in IST.
 */
fun formatLocalSchedule(isoUtc: String?): String {
    if (isoUtc.isNullOrBlank()) return "–"
    val zoned = try {
        Instant.parse(isoUtc).atZone(ZoneId.systemDefault())
    } catch (e: DateTimeParseException) {
        return isoUtc
    }
    val today = LocalDate.now(ZoneId.systemDefault())
    val pattern = if (zoned.toLocalDate() == today) "h:mma" else "EEE h:mma"
    return zoned.format(DateTimeFormatter.ofPattern(pattern, Locale.getDefault())).lowercase(Locale.getDefault())
}
