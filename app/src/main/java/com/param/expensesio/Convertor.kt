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

        val splited = calendar.split(",")

        val cal = Calendar.getInstance()
        cal.set(splited[0].toInt(), splited[1].toInt(), splited[2].toInt())

        return cal
    }
}