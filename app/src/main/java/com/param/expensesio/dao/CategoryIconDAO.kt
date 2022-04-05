package com.param.expensesio.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.param.expensesio.data.CategoryIcon

@Dao
interface CategoryIconDAO {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addCategoryIcon(categoryIcon: CategoryIcon)

    @Query("SELECT * FROM category_icon_table WHERE title = :title")
    fun getCategoryIcon(title: String): LiveData<CategoryIcon>

    @Query("SELECT * FROM category_icon_table")
    fun readAllIcons() : LiveData<List<CategoryIcon>>

}