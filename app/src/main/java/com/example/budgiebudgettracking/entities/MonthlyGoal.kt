package com.example.budgiebudgettracking.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
	tableName = "monthly_goals",
	foreignKeys = [
	ForeignKey(
		entity = User::class,
		parentColumns = ["id"],
		childColumns = ["userId"],
		onDelete = ForeignKey.CASCADE
	)
	],
	indices = [
	Index(value = ["userId", "yearMonth"], unique = true)
	]
)
data class MonthlyGoal(
	@PrimaryKey(autoGenerate = true)
	val id: Int = 0,
	val userId: Int,
	val yearMonth: String,    // format "YYYY-MM"
	val minGoal: Double,
	val maxGoal: Double,
	val createdAt: Long = System.currentTimeMillis()
)
