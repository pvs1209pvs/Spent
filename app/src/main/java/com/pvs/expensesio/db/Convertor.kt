package com.pvs.expensesio.db

import androidx.room.TypeConverter
import java.time.LocalDate

class Convertor {

    @TypeConverter
    fun calendarToString(date: LocalDate) = "${date.year},${date.monthValue},${date.dayOfMonth},"

    @TypeConverter
    fun stringToCalendar(dateString: String): LocalDate {
        val yearMonthDay = dateString.split(",")
        return LocalDate.of(yearMonthDay[0].toInt(), yearMonthDay[1].toInt(), yearMonthDay[2].toInt())
    }

}