package com.example.budgiebudgettracking.entities

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Wrapper to fetch a Transaction together with its Category.
 */
data class TransactionWithCategory(
	@Embedded val transaction: Transaction,
	@Relation(
		parentColumn = "categoryId",
		entityColumn = "id"
	)
	val category: Category?
)
