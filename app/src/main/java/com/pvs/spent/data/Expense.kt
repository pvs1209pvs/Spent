package com.pvs.spent.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.time.LocalDate

@Entity(tableName = "expense_table")
data class Expense(
    var ofUser: String = "",
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var title: String = "",
    var amount: Float = 0f,
    var ofCategory: String = "",
    var createdOn: LocalDate = LocalDate.now(),
    @ColumnInfo(defaultValue = "0") var backedUp: Int = 0
) : Serializable {

    init {
        title = title.trim()
    }

    fun isFromNow(): Boolean {
        val now = LocalDate.now()
        return createdOn.year == now.year && createdOn.monthValue == now.monthValue
    }

    override fun toString(): String {
        return "Expense(id=$id, title='$title', amount=$amount, ofCategory='$ofCategory', " +
                "createdOn='${createdOn.year},${createdOn.monthValue}', backup=$backedUp )"
    }

}