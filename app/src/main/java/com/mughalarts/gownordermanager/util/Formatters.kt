package com.mughalarts.gownordermanager.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

/** The ONLY two payment statuses in the app. */
object PaymentStatus {
    const val PENDING = "Pending"
    const val PAID = "Paid"

    fun from(totalAmount: Long, advanceAmount: Long): String {
        return if (totalAmount > 0 && advanceAmount == totalAmount) PAID else PENDING
    }
}

fun formatOrderNumber(number: Int): String = String.format(Locale.US, "GO-%04d", number)

fun formatMoney(value: Long): String = String.format(Locale.US, "Rs %,d", value)

private val dateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)

/** Dates are stored as UTC-midnight milliseconds (this is also what the date picker returns). */
fun formatDate(millis: Long): String =
    dateFormatter.format(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC))

fun todayMillis(): Long =
    LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
