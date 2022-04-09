package com.param.expensesio

import androidx.room.TypeConverter
import java.util.*

class Convertor {

    @TypeConverter
    fun calendarToString(calendar: Calendar) =
        calendar.get(Calendar.YEAR).toString() + "," +
                calendar.get(Calendar.MONTH).toString() + "," +
                calendar.get(Calendar.DATE).toString() + ","

    @TypeConverter
    fun stringToCalendar(calendar: String): Calendar {

        val splitted = calendar.split(",")

        val cal = Calendar.getInstance()
        cal.set(splitted[0].toInt(), splitted[1].toInt(), splitted[2].toInt())

        return cal
    }
}