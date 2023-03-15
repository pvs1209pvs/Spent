package com.pvs.spent.data

import androidx.room.Entity
import java.io.Serializable

@Entity(tableName = "fixed_exp_date_table", primaryKeys = ["month", "year", "ofUser"])
data class FixedExpDate(
    var month: Int,
    var year: Int,
    var ofUser: String = ""
) : Serializable