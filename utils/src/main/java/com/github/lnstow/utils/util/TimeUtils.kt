package com.github.lnstow.utils.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

fun Calendar.toTimeString(formatStr: String = "yyyy-MM-dd"): String {
    val dateFormat = SimpleDateFormat(formatStr)
    return dateFormat.format(this.time)
}

fun String.toTimeCalendar(formatStr: String = "yyyy-MM-dd"): Calendar {
    val calendar = Calendar.getInstance()
    calendar.time = toTimeDate(formatStr)
    return calendar
}

fun String.toTimeDate(formatStr: String = "yyyy-MM-dd"): Date {
    val dateFormat = SimpleDateFormat(formatStr)
    val date = dateFormat.parse(this)
    return date ?: Date()
}

val Date.yearFriendly get() = year + 1900
val Date.monthFriendly get() = month + 1
val Calendar.year get() = get(Calendar.YEAR)
val Calendar.month get() = get(Calendar.MONTH)
val Calendar.monthFriendly get() = month + 1
val Calendar.day get() = get(Calendar.DAY_OF_MONTH)