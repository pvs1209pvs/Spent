package com.param.expensesio.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.Exclude
import com.param.expensesio.Convertor
import java.io.Serializable
import java.util.*

@Entity(tableName = "expense_table")
data class Expense(
    var ofUser: String = "",
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var title: String = "",
    var amount: Float = 0f,
    var ofCategory: String = "",
    @JvmField var createdOn: Calendar = Calendar.getInstance()
) : Serializable {

    fun isFromNow(): Boolean {
        val now = Calendar.getInstance()
        return createdOn.get(Calendar.MONTH) == now.get(Calendar.MONTH) &&
                createdOn.get(Calendar.YEAR) == now.get(Calendar.YEAR)
    }

    @Exclude
    public fun getCreatedOn() = Convertor().calendarToString(createdOn)

    @Exclude
    public fun setCreatedOn( c: Calendar) {
        this.createdOn = c
    }

    override fun toString(): String {
        return "Expense(id=$id, title='$title', amount=$amount, ofCategory='$ofCategory', " +
                "createdOn='${createdOn.get(Calendar.MONTH)},${createdOn.get(Calendar.YEAR)}')"
    }
}