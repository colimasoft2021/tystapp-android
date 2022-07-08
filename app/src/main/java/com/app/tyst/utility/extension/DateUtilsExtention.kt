package com.app.tyst.utility.extension

import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

fun Date.toDDMMYYYYStr(): String {
    val format = SimpleDateFormat("dd/MM/yyyy")
    return format.format(this)
}

fun Date.toMMDDYYYStr(): String {
    val format = SimpleDateFormat("MMM dd yyyy")
    return format.format(this)
}

fun String.toMMDDYYYDate(): Date {
    return try {
        SimpleDateFormat("MMM dd yyyy").parse(this)
    } catch (ex: Exception) {
        Calendar.getInstance().time
    }
}

fun Date.toHH_MM_AStr(): String {
    val format = SimpleDateFormat("hh:mm a")
    return format.format(this)
}

fun Date.toAmPm(): String {
    val format = SimpleDateFormat("hh:mm a")
    return format.format(this)
}

fun String.toAmPm(): Date {
    val format = SimpleDateFormat("hh:mm a")
    return format.parse(this)
}

fun Date.to24HoursFormat(): String {
    val format = SimpleDateFormat("HH:mm")
    return format.format(this)
}

fun String.to24HoursFormat(): Date {
    val format = SimpleDateFormat("HH:mm")
    return format.parse(this)
}

fun String.from24HoursTo12Hours(): String {
    try {
        val sdf = SimpleDateFormat("HH:mm")
        val dateObj = sdf.parse(this)
        return SimpleDateFormat("hh:mm a").format(dateObj)
    } catch (e: ParseException) {
        e.printStackTrace()
    }
    return this
}

fun String.toMonthDayYearString(): String {
    var theDateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd")
    var date = Date()
    try {
        date = theDateFormat.parse(this)
    } catch (exception: Exception) {

    }
//    theDateFormat = SimpleDateFormat("MMM dd, yyyy")
//    return theDateFormat.format(date)

    val format = SimpleDateFormat("MMM dd, yyyy")
    return format.format(date)
}

fun convertDate(inputDate: String): String {
    var theDateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd")
    var date: Date? = null
    try {
        date = theDateFormat.parse(inputDate)
    } catch (exception: Exception) {

    }

    theDateFormat = SimpleDateFormat("MMM dd, yyyy")
    return theDateFormat.format(date)
}

fun Date.toServerDateFormatString(): String {
    val format = SimpleDateFormat("yyyy-MM-dd")
    return format.format(this)
}

fun String?.toServerDateFormatString(): String {
    if (this.isNullOrEmpty())
        return ""
    val theDateFormat: DateFormat = SimpleDateFormat("MMM dd yyyy")
    var date = Date()
    try {
        date = theDateFormat.parse(this)
    } catch (exception: Exception) {

    }

    val format = SimpleDateFormat("yyyy-MM-dd")
    return format.format(date)
}

fun String?.fromServerDateToYYYYMMDD(): String {

    if (this.isNullOrEmpty())
        return ""
    return try {
        val date = SimpleDateFormat("yyyy-MM-dd").parse(this)
        SimpleDateFormat("dd MMM yyyy").format(date)
    } catch (ex: Exception) {
        ""
    }
}

fun String?.fromServerDateToMMMYY(): String {

    if (this.isNullOrEmpty())
        return ""
    return try {
        val date = SimpleDateFormat("yyyy-MM-dd").parse(this)
        SimpleDateFormat("MMM yyyy").format(date)
    } catch (ex: Exception) {
        ""
    }
}

/**
 * Method to get days hours minutes seconds from milliseconds
 * @param millisUntilFinished Long
 * @return String
 */
fun Long.timeToMinuteSecond(): String {
    var millisUntilFinished: Long = this
    val days = TimeUnit.MILLISECONDS.toDays(millisUntilFinished)
    millisUntilFinished -= TimeUnit.DAYS.toMillis(days)

    val hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
    millisUntilFinished -= TimeUnit.HOURS.toMillis(hours)

    val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
    millisUntilFinished -= TimeUnit.MINUTES.toMillis(minutes)

    val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished)

    // Format the string
    return String.format(
            Locale.getDefault(),
            "%02d", seconds
    )
}