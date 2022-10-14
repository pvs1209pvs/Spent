package com.pvs.spent.data

import java.io.Serializable

data class CreationPeriod(var year: Int=0, var monthValue: Int=0, var dayOfMonth: Int=0) : Serializable {

    companion object {

        fun now(): CreationPeriod {
            val t = java.time.LocalDate.now()
            return CreationPeriod(t.year, t.monthValue, t.dayOfMonth)
        }
    }

}