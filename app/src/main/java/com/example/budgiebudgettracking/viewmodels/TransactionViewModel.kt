package com.example.budgiebudgettracking.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.example.budgiebudgettracking.database.AppDatabase
import com.example.budgiebudgettracking.entities.Transaction
import com.example.budgiebudgettracking.entities.TransactionWithCategory
import com.example.budgiebudgettracking.utils.SessionManager
import kotlinx.coroutines.launch
import java.util.Calendar

class TransactionViewModel(application: Application) : AndroidViewModel(application) {

	// DAOs & Session
	private val transactionDao = AppDatabase.getDatabase(application).transactionDao()
	private val userDao        = AppDatabase.getDatabase(application).userDao()
	private val sessionManager = SessionManager.getInstance(application)

	// ───────────────────────────────────────────────────────────────────────────
	// Core LiveData streams
	private val _oneOffWithCategory    = MediatorLiveData<List<TransactionWithCategory>>()
	val oneOffWithCategory: LiveData<List<TransactionWithCategory>>
	get() = _oneOffWithCategory

	private val _recurringWithCategory = MediatorLiveData<List<TransactionWithCategory>>()
	val recurringWithCategory: LiveData<List<TransactionWithCategory>>
	get() = _recurringWithCategory

	private val _totalExpenses   = MediatorLiveData<Double>()
	val totalExpenses: LiveData<Double> = _totalExpenses

	private val _totalIncome     = MediatorLiveData<Double>()
	val totalIncome: LiveData<Double> = _totalIncome

	private val _monthlyExpenses = MediatorLiveData<Double>()
	val monthlyExpenses: LiveData<Double> = _monthlyExpenses

	private val _monthlyIncome   = MediatorLiveData<Double>()
	val monthlyIncome: LiveData<Double> = _monthlyIncome

	private val _operationResult = MutableLiveData<Boolean>()
	val operationResult: LiveData<Boolean> = _operationResult

	// ───────────────────────────────────────────────────────────────────────────
	init {
		// On init, load one-off vs recurring streams + totals/stats
		refreshAll()
	}

	/** Completely reloads all the core streams (one-off, recurring, totals, stats) */
	fun refreshAll() {
		viewModelScope.launch {
			val userId = getUserId() ?: return@launch

			// 1. One-off transactions by default
			val oneOffSource = transactionDao
			.getWithCategoryByRecurringLive(userId, isRecurring = false)
			_oneOffWithCategory.addSource(oneOffSource) { list ->
				_oneOffWithCategory.value = list
			}

			// 2. Recurring transactions
			val recurringSource = transactionDao
			.getWithCategoryByRecurringLive(userId, isRecurring = true)
			_recurringWithCategory.addSource(recurringSource) { list ->
				_recurringWithCategory.value = list
			}

			// 3. Total expenses & income
			val totalExpSrc = transactionDao.getTotalExpensesLive(userId)
			_totalExpenses.addSource(totalExpSrc) { _totalExpenses.value = it ?: 0.0 }

			val totalIncSrc = transactionDao.getTotalIncomeLive(userId)
			_totalIncome.addSource(totalIncSrc) { _totalIncome.value = it ?: 0.0 }

			// 4. Monthly stats
			loadMonthlyStatistics(userId)
		}
	}

	/** Returns LiveData filtered by date range AND recurring flag */
	fun getTransactionsByDateAndRecurring(
		startDate: Long,
		endDate: Long,
		isRecurring: Boolean
	): LiveData<List<TransactionWithCategory>> {
		val result = MediatorLiveData<List<TransactionWithCategory>>()
		viewModelScope.launch {
			val userId = getUserId() ?: return@launch
			val source = transactionDao
			.getWithCategoryByDateRangeAndRecurringLive(
				userId, startDate, endDate, isRecurring
			)
			result.addSource(source) { list -> result.value = list }
		}
		return result
	}

	// Returns a LiveData that emits all TransactionWithCategory for the current user whose transaction.date is in [startDate, endDate).
	fun getTransactionsWithCategoryByDateRange(
		startDate: Long,
		endDate: Long
	): LiveData<List<TransactionWithCategory>> {
		val result = MediatorLiveData<List<TransactionWithCategory>>()
		viewModelScope.launch {
			// 1. Lookup current user ID (suspending)
			val userId = getUserId() ?: return@launch

			// 2. Ask Room for a LiveData<List<TransactionWithCategory>>
			//    matching our date range
			val source = transactionDao
			.getWithCategoryByDateRangeLive(userId, startDate, endDate)

			// 3. Hook it up so that `result` mirrors `source`
			result.addSource(source) { list ->
				result.value = list
			}
		}
		return result
	}

	// ───────────────────────────────────────────────────────────────────────────
	// Basic date-range filter (no recurring flag)
	fun getTransactionsByDateRange(
		startDate: Long,
		endDate: Long
	): LiveData<List<Transaction>> {
		val result = MediatorLiveData<List<Transaction>>()
		viewModelScope.launch {
			val userId = getUserId() ?: return@launch
			val source = transactionDao.getTransactionsByDateRangeLive(userId, startDate, endDate)
			result.addSource(source) { result.value = it }
		}
		return result
	}

	// Filter by category
	fun getTransactionsByCategory(categoryId: Int): LiveData<List<Transaction>> {
		val result = MediatorLiveData<List<Transaction>>()
		viewModelScope.launch {
			val userId = getUserId() ?: return@launch
			val source = transactionDao.getTransactionsByCategoryLive(userId, categoryId)
			result.addSource(source) { result.value = it }
		}
		return result
	}

	// Fetch a single transaction
	fun getTransactionById(transactionId: Int, callback: (Transaction?) -> Unit) {
		viewModelScope.launch {
			val userId = getUserId() ?: return@launch
			val tx = transactionDao.getTransactionById(userId, transactionId)
			callback(tx)
		}
	}

	// Fetch with category
	fun getTransactionWithCategoryById(txId: Int): LiveData<TransactionWithCategory> {
		val result = MediatorLiveData<TransactionWithCategory>()
		viewModelScope.launch {
			val userId = getUserId() ?: return@launch
			val source = transactionDao.getTransactionWithCategoryById(userId, txId)
			result.addSource(source) { result.value = it }
		}
		return result
	}

	// ───────────────────────────────────────────────────────────────────────────
	// CRUD operations
	fun addTransaction(transaction: Transaction) {
		viewModelScope.launch {
			try {
				transactionDao.insert(transaction)
				_operationResult.postValue(true)
			} catch (e: Exception) {
				_operationResult.postValue(false)
			}
		}
	}

	fun addNewTransaction(
		amount: Double,
		description: String?,
		isExpense: Boolean,
		date: Long,
		categoryId: Int,
		receiptImagePath: String?
	) {
		viewModelScope.launch {
			val userId = getUserId() ?: return@launch
			val tx = Transaction(
				amount = amount,
				description = description,
				isExpense = isExpense,
				date = date,
				categoryId = categoryId,
				receiptImagePath = receiptImagePath,
				userId = userId
			)
			try {
				transactionDao.insert(tx)
				_operationResult.postValue(true)
			} catch (e: Exception) {
				_operationResult.postValue(false)
			}
		}
	}

	fun updateTransaction(transaction: Transaction) {
		viewModelScope.launch {
			try {
				transactionDao.update(transaction)
				_operationResult.postValue(true)
			} catch (e: Exception) {
				_operationResult.postValue(false)
			}
		}
	}

	fun deleteTransaction(transaction: Transaction) {
		viewModelScope.launch {
			try {
				transactionDao.delete(transaction)
				_operationResult.postValue(true)
			} catch (e: Exception) {
				_operationResult.postValue(false)
			}
		}
	}

	// ───────────────────────────────────────────────────────────────────────────
	// Helpers

	/** Loads expenses & income for the current calendar month */
	private fun loadMonthlyStatistics(userId: Int) {
		val cal = Calendar.getInstance().apply {
			set(Calendar.DAY_OF_MONTH, 1)
			set(Calendar.HOUR_OF_DAY,    0)
			set(Calendar.MINUTE,         0)
			set(Calendar.SECOND,         0)
			set(Calendar.MILLISECOND,    0)
		}
		val startOfMonth = cal.timeInMillis

		cal.add(Calendar.MONTH, 1)
		val startOfNext = cal.timeInMillis

		// monthly expenses
		val expSrc = transactionDao.getExpensesByDateRangeLive(userId, startOfMonth, startOfNext)
		_monthlyExpenses.addSource(expSrc) { _monthlyExpenses.value = it ?: 0.0 }

		// monthly income
		val incSrc = transactionDao.getIncomeByDateRangeLive(userId, startOfMonth, startOfNext)
		_monthlyIncome.addSource(incSrc) { _monthlyIncome.value = it ?: 0.0 }
	}

	/** Suspends to fetch the current user’s ID */
	private suspend fun getUserId(): Int? {
		val email = sessionManager.getUserEmail()
		if (email.isEmpty()) return null
		return userDao.getUserByEmail(email)?.id
	}

	// ───────────────────────────────────────────────────────────────────────────
	// Factory

	class Factory(private val application: Application) : ViewModelProvider.Factory {
		override fun <T : ViewModel> create(modelClass: Class<T>): T {
			if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
				@Suppress("UNCHECKED_CAST")
				return TransactionViewModel(application) as T
			}
			throw IllegalArgumentException("Unknown ViewModel class")
		}
	}
}
