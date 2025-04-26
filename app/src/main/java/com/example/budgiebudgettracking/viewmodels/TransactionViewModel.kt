package com.example.budgiebudgettracking.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.example.budgiebudgettracking.database.AppDatabase
import com.example.budgiebudgettracking.entities.Transaction
import com.example.budgiebudgettracking.entities.TransactionWithCategory
import com.example.budgiebudgettracking.utils.SessionManager
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import java.lang.IllegalArgumentException

class TransactionViewModel(application: Application) : AndroidViewModel(application) {

	private val transactionDao = AppDatabase.getDatabase(application).transactionDao()
	private val userDao = AppDatabase.getDatabase(application).userDao()
	private val sessionManager = SessionManager.getInstance(application)

	private val _expenses = MediatorLiveData<List<Transaction>>()
	val expenses: LiveData<List<Transaction>> = _expenses

	private val _income = MediatorLiveData<List<Transaction>>()
	val income: LiveData<List<Transaction>> = _income

	private val _totalExpenses = MediatorLiveData<Double>()
	val totalExpenses: LiveData<Double> = _totalExpenses

	private val _totalIncome = MediatorLiveData<Double>()
	val totalIncome: LiveData<Double> = _totalIncome

	private val _operationResult = MutableLiveData<Boolean>()
	val operationResult: LiveData<Boolean> = _operationResult

	private val _monthlyExpenses = MediatorLiveData<Double>()
	val monthlyExpenses: LiveData<Double> = _monthlyExpenses

	private val _monthlyIncome = MediatorLiveData<Double>()
	val monthlyIncome: LiveData<Double> = _monthlyIncome

	private val _allTransactions = MediatorLiveData<List<Transaction>>()
	private val _allWithCategory = MediatorLiveData<List<TransactionWithCategory>>()
	val allWithCategory: LiveData<List<TransactionWithCategory>> = _allWithCategory

	init {
		loadData()
	}

	fun getTransactionsWithCategoryByDateRange(startDate: Long, endDate: Long): LiveData<List<TransactionWithCategory>> {
		val result = MediatorLiveData<List<TransactionWithCategory>>()
		viewModelScope.launch {
			val userId = getUserId() ?: return@launch
			val source = transactionDao.getWithCategoryByDateRangeLive(userId, startDate, endDate)
			result.addSource(source) { list -> result.value = list }
		}
		return result
	}

	private fun loadData() {
		viewModelScope.launch {
			val userId = getUserId() ?: return@launch

			// Load transactions with category
			val withCatSource = transactionDao.getAllWithCategoryLive(userId)
			_allWithCategory.addSource(withCatSource) { list ->
				_allWithCategory.value = list
			}

			// Load all transactions
			val allTransactionsSource = transactionDao.getAllTransactionsLive(userId)
			_allTransactions.addSource(allTransactionsSource) { result ->
				_allTransactions.value = result
			}

			// Load expenses
			val expensesSource = transactionDao.getTransactionsByTypeLive(userId, true)
			_expenses.addSource(expensesSource) { result ->
				_expenses.value = result
			}

			// Load income
			val incomeSource = transactionDao.getTransactionsByTypeLive(userId, false)
			_income.addSource(incomeSource) { result ->
				_income.value = result
			}

			// Load total expenses
			val totalExpensesSource = transactionDao.getTotalExpensesLive(userId)
			_totalExpenses.addSource(totalExpensesSource) { result ->
				_totalExpenses.value = result ?: 0.0
			}

			// Load total income
			val totalIncomeSource = transactionDao.getTotalIncomeLive(userId)
			_totalIncome.addSource(totalIncomeSource) { result ->
				_totalIncome.value = result ?: 0.0
			}

			// Load monthly statistics
			loadMonthlyStatistics(userId)
		}
	}

	private fun loadMonthlyStatistics(userId: Int) {
		val calendar = Calendar.getInstance()

		// Set to first day of current month
		calendar.set(Calendar.DAY_OF_MONTH, 1)
		calendar.set(Calendar.HOUR_OF_DAY, 0)
		calendar.set(Calendar.MINUTE, 0)
		calendar.set(Calendar.SECOND, 0)
		calendar.set(Calendar.MILLISECOND, 0)
		val startDate = calendar.timeInMillis

		// Set to first day of next month
		calendar.add(Calendar.MONTH, 1)
		val endDate = calendar.timeInMillis

		// Load monthly expenses
		val monthlyExpensesSource = transactionDao.getExpensesByDateRangeLive(userId, startDate, endDate)
		_monthlyExpenses.addSource(monthlyExpensesSource) { result ->
			_monthlyExpenses.value = result ?: 0.0
		}

		// Load monthly income
		val monthlyIncomeSource = transactionDao.getIncomeByDateRangeLive(userId, startDate, endDate)
		_monthlyIncome.addSource(monthlyIncomeSource) { result ->
			_monthlyIncome.value = result ?: 0.0
		}
	}

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

	fun getTransactionById(transactionId: Int, callback: (Transaction?) -> Unit) {
		viewModelScope.launch {
			val transaction = transactionDao.getTransactionById(transactionId)
			callback(transaction)
		}
	}

	fun getTransactionsByDateRange(startDate: Long, endDate: Long): LiveData<List<Transaction>> {
		val result = MediatorLiveData<List<Transaction>>()

		viewModelScope.launch {
			val userId = getUserId() ?: return@launch
			val source = transactionDao.getTransactionsByDateRangeLive(userId, startDate, endDate)
			result.addSource(source) { transactions ->
				result.value = transactions
			}
		}

		return result
	}

	fun getTransactionsByCategory(categoryId: Int): LiveData<List<Transaction>> {
		val result = MediatorLiveData<List<Transaction>>()

		viewModelScope.launch {
			val userId = getUserId() ?: return@launch
			val source = transactionDao.getTransactionsByCategoryLive(userId, categoryId)
			result.addSource(source) { transactions ->
				result.value = transactions
			}
		}

		return result
	}

	private suspend fun getUserId(): Int? {
		val email = sessionManager.getUserEmail()
		if (email.isEmpty()) return null

		return userDao.getUserByEmail(email)?.id
	}

	fun refresh() {
		loadData()
	}

	// in TransactionViewModel.kt
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
				amount           = amount,
				description      = description,
				isExpense        = isExpense,
				date             = date,
				categoryId       = categoryId,
				receiptImagePath = receiptImagePath,
				userId           = userId
			)
			transactionDao.insert(tx)
			_operationResult.postValue(true)
		}
	}

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
