package com.pvs.spent.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pvs.spent.data.FixedExpDate

@Dao
interface FixedExpDateDAO {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addFixedExpDate(fixedExpDate: FixedExpDate)

    @Query("SELECT * from fixed_exp_date_table WHERE month = :month AND year = :year AND ofUser = :ofUser")
    fun getFixedExpDate(month:Int, year:Int, ofUser:String) : LiveData<FixedExpDate>

}