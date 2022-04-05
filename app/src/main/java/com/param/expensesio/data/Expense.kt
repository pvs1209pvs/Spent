package com.param.expensesio.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.*

@Entity(tableName = "expense_table")
data class Expense(
    var ofUser: String,
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var title: String,
    var amount: Float,
    var ofCategory: String,
    var createdOn: Calendar
) : Serializable {

    fun isFromNow(): Boolean {
        val now = Calendar.getInstance()
        return createdOn.get(Calendar.MONTH) == now.get(Calendar.MONTH) &&
                createdOn.get(Calendar.YEAR) == now.get(Calendar.YEAR)
    }

    override fun toString(): String {
        return "Expense(id=$id, title='$title', amount=$amount, ofCategory='$ofCategory', " +
                "createdOn='${createdOn.get(Calendar.MONTH)},${createdOn.get(Calendar.YEAR)}')"
    }
}