package com.example.budgiebudgettracking.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.Transaction as RoomTransaction
import com.example.budgiebudgettracking.entities.Transaction
import com.example.budgiebudgettracking.entities.TransactionWithCategory

@Dao
interface TransactionDao {
	@Insert
	suspend fun insert(transaction: Transaction): Long

	@Update
	suspend fun update(transaction: Transaction)

	@Delete
	suspend fun delete(transaction: Transaction)

	@Query("SELECT * FROM transactions WHERE id = :transactionId AND userId = :userId")
	suspend fun getTransactionById(userId: Int, transactionId: Int): Transaction?

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

	@Query("SELECT SUM(amount) FROM transactions WHERE userId = :userId AND isExpense = 1 AND date BETWEEN :startDate AND :endDate")
	fun getTotalExpensesByDateRangeLive(userId: Int, startDate: Int, endDate: Int): LiveData<Double?>

	@Query("SELECT SUM(amount) FROM transactions WHERE userId = :userId AND isExpense = 1 AND strftime('%Y-%m', datetime(date / 1000, 'unixepoch')) = :yearMonth")
	suspend fun getTotalExpensesForMonth(userId: Int, yearMonth: String): Double?

	@Query("SELECT SUM(amount) FROM transactions WHERE userId = :userId AND isExpense = 0")
	fun getTotalIncomeLive(userId: Int): LiveData<Double?>

	@Query("SELECT SUM(amount) FROM transactions WHERE userId = :userId AND isExpense = 1 AND date BETWEEN :startDate AND :endDate")
	fun getExpensesByDateRangeLive(userId: Int, startDate: Long, endDate: Long): LiveData<Double?>

	@Query("SELECT SUM(amount) FROM transactions WHERE userId = :userId AND isExpense = 0 AND date BETWEEN :startDate AND :endDate")
	fun getIncomeByDateRangeLive(userId: Int, startDate: Long, endDate: Long): LiveData<Double?>

	@RoomTransaction
	@Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC")
	fun getAllWithCategoryLive(userId: Int): LiveData<List<TransactionWithCategory>>

	@RoomTransaction
	@Query("""
	SELECT * FROM transactions
	WHERE userId = :userId
	AND (
		(date BETWEEN :startDate AND :endDate)
		OR
		(:startDate BETWEEN startTime AND endTime)
		OR
		(:endDate BETWEEN startTime AND endTime)
	)
	ORDER BY date DESC
	""")
	fun getWithCategoryByDateRangeLive(userId: Int, startDate: Long, endDate: Long): LiveData<List<TransactionWithCategory>>

	@RoomTransaction
	@Query("SELECT * FROM transactions WHERE userId = :userId AND id = :txId")
	fun getTransactionWithCategoryById(userId: Int, txId: Int): LiveData<TransactionWithCategory>

	@RoomTransaction
	@Query("SELECT * FROM transactions WHERE userId = :userId AND isRecurring = :isRecurring ORDER BY date DESC")
	fun getWithCategoryByRecurringLive(userId: Int, isRecurring: Boolean): LiveData<List<TransactionWithCategory>>

	@RoomTransaction
	@Query("""
	SELECT * FROM transactions
	WHERE userId = :userId
	AND (
		(date BETWEEN :startDate AND :endDate)
		OR
		(:startDate BETWEEN startTime AND endTime)
		OR
		(:endDate BETWEEN startTime AND endTime)
	)
	AND isRecurring = :isRecurring
	ORDER BY date DESC
	""")
	fun getWithCategoryByDateRangeAndRecurringLive(
		userId: Int,
		startDate: Long,
		endDate: Long,
		isRecurring: Boolean
	): LiveData<List<TransactionWithCategory>>
}
