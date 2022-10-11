package com.pvs.expensesio.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.pvs.expensesio.data.Expense

@Dao
interface ExpenseDAO {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addExpense(expense: Expense)

    @Delete
    suspend fun delExpense(expense: Expense)

    @Query("DELETE FROM expense_table WHERE title = :title")
    suspend fun delExpenseByTitle(title: String)

    @Update
    suspend fun updateExpense(expense: Expense): Int

    @Transaction
    suspend fun updateExpenseBatch(list: List<Expense>) {
        list.forEach { updateExpense(it) }
    }

    @Query("UPDATE expense_table SET amount = :newAmount WHERE title = :title AND ofUser = :ofUser")
    suspend fun updateTotal(title: String, newAmount: Float, ofUser: String)

    @Query("UPDATE expense_table SET ofCategory = :newCategory WHERE title = :title AND ofUser = :ofUser")
    suspend fun updateCategoryOf(title: String, newCategory: String, ofUser: String)

    @Query("SELECT * FROM expense_table WHERE title = :title AND ofUser = :ofUser AND createdOn LIKE :y||','||:m||','||'%'")
    fun readExpense(title: String, ofUser: String, y: Int, m: Int): Expense? // change this

    @Query("SELECT * FROM expense_table WHERE id = :id AND ofUser = :ofUser")
    suspend fun readExpense(id: Int, ofUser: String): Expense?

    @Query("SELECT * FROM expense_table WHERE ofUser = :ofUser")
    fun readAllExpense(ofUser: String): LiveData<List<Expense>>

    @Query("SELECT * FROM expense_table WHERE ofCategory = :categoryTitle AND ofUser = :ofUser AND createdOn LIKE :y||','||:m||','||'%'")
    fun readAllExpense(
        categoryTitle: String,
        y: Int,
        m: Int,
        ofUser: String
    ): LiveData<List<Expense>>

    @Query("SELECT * FROM expense_table WHERE ofCategory = :categoryTitle AND ofUser = :ofUser")
    fun readAllExpenseList(categoryTitle: String, ofUser: String): List<Expense>

    @Query("SELECT * FROM expense_table WHERE ofUser = :ofUser AND createdOn LIKE :y||','||:m||','||'%'")
    fun readAllExpenseFromNow(ofUser: String, y: Int, m: Int): LiveData<List<Expense>>

    @Query("SELECT * FROM expense_table WHERE ofUser = :ofUser AND backedUp = 0")
    fun getUnbackedUpExpenses(ofUser: String): LiveData<List<Expense>>

    @Query("SELECT COUNT(DISTINCT id) from expense_table WHERE ofUser = :ofUser AND createdOn LIKE :y||','||:m||','||'%' ")
    fun expenseCount(ofUser: String, y: Int, m: Int): LiveData<Int>

    @Query("SELECT COUNT(DISTINCT id) from expense_table WHERE ofUser = :ofUser AND backedUp = 0 AND createdOn NOT LIKE :y||','||:m||','||'%' ")
    fun expenseCountOld(ofUser: String, y: Int, m: Int): Int

    @Query("SELECT * FROM expense_table WHERE ofCategory = :category AND ofUser = :ofUser AND createdOn LIKE :y||','||:m||','||'%' ORDER BY amount DESC")
    fun orderExpenseAmountHighestFirst(
        category: String,
        y: Int,
        m: Int,
        ofUser: String
    ): LiveData<List<Expense>>

    @Query("SELECT * FROM expense_table WHERE ofCategory = :category AND ofUser = :ofUser AND createdOn LIKE :y||','||:m||','||'%' ORDER BY amount ASC")
    fun orderExpenseAmountLowestFirst(
        category: String,
        y: Int,
        m: Int,
        ofUser: String
    ): LiveData<List<Expense>>


}