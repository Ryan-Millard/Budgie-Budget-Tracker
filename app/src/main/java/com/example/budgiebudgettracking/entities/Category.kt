package com.example.budgiebudgettracking.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
	tableName = "categories",
	foreignKeys = [ForeignKey(
		entity = User::class,
		parentColumns = ["id"],
		childColumns = ["userId"],
		onDelete = ForeignKey.CASCADE
	)],
	indices = [
	Index(value = ["userId"]),
	Index(value = ["categoryName"], unique = true)
	]
)
data class Category(
	@PrimaryKey(autoGenerate = true)
	val id: Int = 0,
	val categoryName: String,

	// Non-imperative fields
	val description: String? = null,
	val hexColorCode: String? = null,
	val icon: String? = null,
	val userId: Int? = null  // Null for system-wide categories
)
