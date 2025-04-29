package com.example.budgiebudgettracking.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.budgiebudgettracking.entities.MonthlyGoal

@Dao
interface MonthlyGoalDao {
    @Insert
    suspend fun insert(goal: MonthlyGoal)

    @Update
    suspend fun update(goal: MonthlyGoal)

    @Query(
        "SELECT * FROM monthly_goals WHERE yearMonth = :ym AND userId = :userId LIMIT 1"
    )
    fun getByYearMonth(ym: String, userId: Int): LiveData<MonthlyGoal?>
}