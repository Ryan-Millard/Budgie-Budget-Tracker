package com.example.budgiebudgettracking.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.example.budgiebudgettracking.database.AppDatabase
import com.example.budgiebudgettracking.entities.MonthlyGoal
import com.example.budgiebudgettracking.entities.User
import com.example.budgiebudgettracking.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MonthlyBudgetViewModel(application: Application) : AndroidViewModel(application) {

	private val sessionManager = SessionManager.getInstance(application)
	private val database = AppDatabase.getDatabase(application)
	private val monthlyGoalDao = database.monthlyGoalDao()
	private val userDao = database.userDao()

	private val _currentUserId = MutableLiveData<Int>()

	val allGoals: LiveData<List<MonthlyGoal>> = _currentUserId.switchMap { userId ->
		monthlyGoalDao.getAllGoalsLive(userId)
	}

	private val _currentMonthGoal = MutableLiveData<MonthlyGoal?>()
	val currentMonthGoal: LiveData<MonthlyGoal?> = _currentMonthGoal

	private val _operationResult = MutableLiveData<Boolean>()
	val operationResult: LiveData<Boolean> = _operationResult

	init {
		viewModelScope.launch {
			val userId = getUserId()
			if (userId != null) {
				_currentUserId.postValue(userId)
			}
		}
	}

	fun loadGoalForMonth(userId: Int, yearMonth: String) {
		viewModelScope.launch {
			val goal = monthlyGoalDao.getGoalForMonth(userId, yearMonth)
			_currentMonthGoal.postValue(goal)
		}
	}

	fun upsertGoal(goal: MonthlyGoal) {
		viewModelScope.launch {
			try {
				monthlyGoalDao.insert(goal)
				_operationResult.postValue(true)
			} catch (e: Exception) {
				_operationResult.postValue(false)
			}
		}
	}

	fun deleteGoal(goal: MonthlyGoal) {
		viewModelScope.launch {
			try {
				monthlyGoalDao.delete(goal)
				_operationResult.postValue(true)
			} catch (e: Exception) {
				_operationResult.postValue(false)
			}
		}
	}

	fun loadUserByEmail(email: String, callback: (User?) -> Unit) {
		viewModelScope.launch(Dispatchers.IO) {
			val user = userDao.getUserByEmail(email)
			withContext(Dispatchers.Main) {
				callback(user)
			}
		}
	}

	/** Suspends to fetch the current user's ID */
	suspend fun getUserId(): Int? {
		val email = sessionManager.getUserEmail()
		if (email.isEmpty()) return null
		return userDao.getUserByEmail(email)?.id
	}

	class Factory(private val application: Application) : ViewModelProvider.Factory {
		@Suppress("UNCHECKED_CAST")
		override fun <T : ViewModel> create(modelClass: Class<T>): T {
			if (modelClass.isAssignableFrom(MonthlyBudgetViewModel::class.java)) {
				return MonthlyBudgetViewModel(application) as T
			}
			throw IllegalArgumentException("Unknown ViewModel class")
		}
	}
}
