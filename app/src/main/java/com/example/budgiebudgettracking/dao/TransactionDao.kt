package com.example.budgiebudgettracking.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.Transaction as RoomTransaction
import com.example.budgiebudgettracking.entities.Transaction
import com.example.budgiebudgettracking.entities.TransactionWithCategory
import java.util.Date

@Dao
interface TransactionDao {
	@Insert
	suspend fun insert(transaction: Transaction): Long

	@Update
	suspend fun update(transaction: Transaction)

	@Delete
	suspend fun delete(transaction: Transaction)

	@Query("SELECT * FROM transactions WHERE id = :transactionId")
	suspend fun getTransactionById(transactionId: Int): Transaction?

	@Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC")
	fun getAllTransactionsLive(userId: Int): LiveData<List<Transaction>>

	@Query("SELECT * FROM transactions WHERE userId = :userId AND isExpense = :isExpense ORDER BY date DESC")
	fun getTransactionsByTypeLive(userId: Int, isExpense: Boolean): LiveData<List<Transaction>>

	@Query("SELECT * FROM transactions WHERE userId = :userId AND categoryId = :categoryId ORDER BY date DESC")
	fun getTransactionsByCategoryLive(userId: Int, categoryId: Int): LiveData<List<Transaction>>

	@Query("SELECT * FROM transactions WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
	fun getTransactionsByDateRangeLive(userId: Int, startDate: Long, endDate: Long): LiveData<List<Transaction>>

	@Query("SELECT SUM(amount) FROM transactions WHERE userId = :userId AND isExpense = 1")
	fun getTotalExpensesLive(userId: Int): LiveData<Double?>

	@Query("SELECT SUM(amount) FROM transactions WHERE userId = :userId AND isExpense = 0")
	fun getTotalIncomeLive(userId: Int): LiveData<Double?>

	@Query("SELECT SUM(amount) FROM transactions WHERE userId = :userId AND isExpense = 1 AND date BETWEEN :startDate AND :endDate")
	fun getExpensesByDateRangeLive(userId: Int, startDate: Long, endDate: Long): LiveData<Double?>

	@Query("SELECT SUM(amount) FROM transactions WHERE userId = :userId AND isExpense = 0 AND date BETWEEN :startDate AND :endDate")
	fun getIncomeByDateRangeLive(userId: Int, startDate: Long, endDate: Long): LiveData<Double?>

	@RoomTransaction
	@Query("SELECT * FROM transactions ORDER BY date DESC")
	fun getAllTransactionsWithCategory(): LiveData<List<TransactionWithCategory>>

	@RoomTransaction
	@Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC")
	fun getAllWithCategoryLive(userId: Int): LiveData<List<TransactionWithCategory>>
}
