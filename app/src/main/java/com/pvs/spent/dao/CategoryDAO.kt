package com.pvs.spent.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.pvs.spent.data.Category

@Dao
interface CategoryDAO {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addCategory(category: Category)

    @Query("DELETE FROM category_table WHERE title = :categoryTitle AND ofUser = :ofUser")
    suspend fun delCategory(categoryTitle: String, ofUser: String)

    @Update
    suspend fun updateCategory(category: Category)

    @Query("UPDATE category_table SET isBackedUp = 1 WHERE title = :title AND ofUser = :ofUser")
    suspend fun updateCategoryIsBackup(title: String, ofUser: String)

    @Query("UPDATE category_table SET total = :newTotal WHERE title = :title AND ofUser = :ofUser ")
    suspend fun updateCategoryTotal(title: String, newTotal: Float, ofUser: String)

    @Query("UPDATE category_table SET budget = :newBudget WHERE title = :title AND ofUser = :ofUser")
    suspend fun updateCategoryBudget(title: String, newBudget: Float, ofUser: String)

    @Query("SELECT * FROM category_table WHERE ofUser = :ofUser")
    fun readCategory(ofUser: String): LiveData<MutableList<Category>>

    @Query("SELECT * FROM category_table WHERE ofUser = :ofUser AND isBackedUp = :isBackedUp")
    fun readCategory(ofUser: String, isBackedUp:Int): List<Category>

    @Query("SELECT * FROM category_table WHERE title = :title AND ofUser = :ofUser")
    suspend fun readCategory(title: String, ofUser: String): Category

    @Query(
        "SELECT " +
                "ofUser," +
                "title," +
                "(SELECT SUM(amount) FROM expense_table WHERE ofCategory = category_table.title AND expense_table.ofUser = :user AND createdOn LIKE :y||','||:m||','||'%') AS total," +
                "budget," +
                "icon " +
                "FROM category_table " +
                "WHERE ofUser = :user"
    )
    fun categoryWithTotal(user: String, y: Int, m: Int): LiveData<List<CategoryWithTotal>>

    @Query("SELECT * FROM category_table WHERE title NOT LIKE 'Misc' AND ofUser = :ofUser ORDER BY total ASC")
    fun orderTotalLowestFirst(ofUser: String): LiveData<List<Category>>

    @Query("SELECT * FROM category_table WHERE title NOT LIKE 'Misc' AND ofUser = :ofUser ORDER BY total DESC")
    fun orderTotalHighestFirst(ofUser: String): LiveData<List<Category>>

    @Query("SELECT * FROM category_table WHERE title NOT LIKE 'Misc' AND ofUser = :ofUser ORDER BY budget ASC")
    fun orderBudgetLowestFirst(ofUser: String): LiveData<List<Category>>

    @Query("SELECT * FROM category_table WHERE title NOT LIKE 'Misc' AND ofUser = :ofUser ORDER BY budget DESC")
    fun orderBudgetHighestFirst(ofUser: String): LiveData<List<Category>>


}