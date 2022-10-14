package com.pvs.spent.data

import java.io.Serializable

data class LocalDate(var year: Int=0, var monthValue: Int=0, var dayOfMonth: Int=0) : Serializable {



    companion object {

        fun now(): LocalDate {
            val t = java.time.LocalDate.now()
            return LocalDate(t.year, t.monthValue, t.dayOfMonth)
        }
    }

}