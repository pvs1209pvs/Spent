package com.pvs.spent.dao

import androidx.room.ColumnInfo

data class CategoryWithTotal(
    @ColumnInfo(name = "ofUser") var ofUser: String,
    @ColumnInfo(name = "title") var title: String,
    @ColumnInfo(name = "aggregate") var aggregate: Float,
    @ColumnInfo(name = "budget") var budget: Float,
    @ColumnInfo(name = "icon") var icon: Int
)