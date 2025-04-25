package com.example.budgiebudgettracking.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.budgiebudgettracking.entities.MonthlyGoal

@Dao
interface MonthlyGoalDao {
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insert(goal: MonthlyGoal)

	@Update
	suspend fun update(goal: MonthlyGoal)

	@Delete
	suspend fun delete(goal: MonthlyGoal)

	@Query("SELECT * FROM monthly_goals WHERE userId = :userId AND yearMonth = :yearMonth LIMIT 1")
	suspend fun getGoalForMonth(userId: Int, yearMonth: String): MonthlyGoal?

	@Query("SELECT * FROM monthly_goals WHERE userId = :userId ORDER BY yearMonth DESC")
	fun getAllGoalsLive(userId: Int): LiveData<List<MonthlyGoal>>
}
