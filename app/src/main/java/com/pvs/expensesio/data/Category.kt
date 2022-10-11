package com.pvs.expensesio.data

import androidx.room.Entity
import com.pvs.expensesio.R
import java.io.Serializable

@Entity(tableName = "category_table", primaryKeys = ["ofUser", "title"])
data class Category(
    var ofUser: String = "",
    var title: String = "",
    var total: Float = 0f,
    var budget: Float = 0f,
    var icon: Int = R.drawable.cat_other
) : Serializable{

    init {
        title = title.trim()
    }

}