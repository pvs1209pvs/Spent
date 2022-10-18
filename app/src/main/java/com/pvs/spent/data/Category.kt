package com.pvs.spent.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.pvs.spent.R
import java.io.Serializable

@Entity(tableName = "category_table", primaryKeys = ["ofUser", "title"])
data class Category(
    var ofUser: String = "",
    var title: String = "",
    var aggregate: Float = 0f,
    var budget: Float = 0f,
    var icon: Int = R.drawable.cat_other,
    @ColumnInfo(defaultValue = "0") var isBackedUp : Int = 0
) : Serializable{

    init {
        title = title.trim()
    }

}