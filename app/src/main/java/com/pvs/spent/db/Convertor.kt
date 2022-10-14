package com.pvs.spent.db

import android.util.Log
import androidx.room.TypeConverter
import java.time.LocalDate
import javax.crypto.spec.IvParameterSpec

class Convertor {

    @TypeConverter
    fun calendarToString(date: LocalDate) = "${date.year},${date.monthValue},${date.dayOfMonth},"

    @TypeConverter
    fun stringToCalendar(dateString: String): LocalDate {
        val yearMonthDay = dateString.split(",")
        Log.d(javaClass.canonicalName, "stringToCalender $yearMonthDay")
        return LocalDate.of(yearMonthDay[0].toInt(), yearMonthDay[1].toInt(), yearMonthDay[2].toInt())
    }

    @TypeConverter
    fun ivToString(iv:IvParameterSpec) = iv.iv.joinToString(prefix = "", postfix = "")

    @TypeConverter
    fun stringToIv(string: String): IvParameterSpec {
        val byteArray = string.split(",").map { it.trim().toByte() }.toByteArray()
        return IvParameterSpec(byteArray)
    }

}