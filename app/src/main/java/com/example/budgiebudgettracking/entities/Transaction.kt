package com.example.budgiebudgettracking.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
	tableName = "transactions",
	foreignKeys = [
	ForeignKey(
		entity = User::class,
		parentColumns = ["id"],
		childColumns = ["userId"],
		onDelete = ForeignKey.CASCADE
	),
	ForeignKey(
		entity = Category::class,
		parentColumns = ["id"],
		childColumns = ["categoryId"],
		onDelete = ForeignKey.SET_NULL
	)
	],
	indices = [
	Index(value = ["userId"]),
	Index(value = ["categoryId"])
	]
)
data class Transaction(
	@PrimaryKey(autoGenerate = true)
	val id: Int = 0,
	val amount: Double,
	val description: String? = null,
	val isExpense: Boolean,
	val date: Long = System.currentTimeMillis(),
	val userId: Int,
	val categoryId: Int? = null,
	val receiptImagePath: String? = null,
	// Additional fields that might be useful
	val isRecurring: Boolean = false,
	val createdAt: Long = System.currentTimeMillis(),
	val updatedAt: Long = System.currentTimeMillis()
)
