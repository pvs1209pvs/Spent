package com.param.expensesio.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.param.expensesio.R
import java.io.Serializable

@Entity(tableName = "category_icon_table")
data class CategoryIcon(
    @PrimaryKey var title: String,
    var icon: Int= R.drawable.cat_other
) : Serializable