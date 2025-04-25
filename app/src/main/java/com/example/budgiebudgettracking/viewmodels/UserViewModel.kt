package com.example.budgiebudgettracking.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.example.budgiebudgettracking.database.AppDatabase
import com.example.budgiebudgettracking.entities.User
import com.example.budgiebudgettracking.utils.SessionManager
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class UserViewModel(application: Application) : AndroidViewModel(application) {

	private val userDao = AppDatabase.getDatabase(application).userDao()
	private val sessionManager = SessionManager.getInstance(application)

	private val _loginResult = MutableLiveData<User?>()
	val loginResult: LiveData<User?> = _loginResult

	private val _registrationResult = MutableLiveData<Boolean>()
	val registrationResult: LiveData<Boolean> = _registrationResult

	private val _userUpdateResult = MutableLiveData<Boolean>()
	val userUpdateResult: LiveData<Boolean> = _userUpdateResult

	fun login(email: String, password: String) {
		viewModelScope.launch {
			val user = userDao.login(email, password)
			if (user != null) {
				sessionManager.login(email)
			}
			_loginResult.postValue(user)
		}
	}

	fun registerUser(user: User) {
		viewModelScope.launch {
			try {
				userDao.insertUser(user)
				_registrationResult.postValue(true)
			} catch (e: Exception) {
				_registrationResult.postValue(false)
			}
		}
	}

	fun updateUser(user: User) {
		viewModelScope.launch {
			try {
				userDao.updateUser(user)
				_userUpdateResult.postValue(true)
			} catch (e: Exception) {
				_userUpdateResult.postValue(false)
			}
		}
	}

	fun getCurrentUser(callback: (User?) -> Unit) {
		if (!sessionManager.isLoggedIn()) {
			callback(null)
			return
		}

		val email = sessionManager.getUserEmail()
		if (email.isEmpty()) {
			callback(null)
			return
		}

		viewModelScope.launch {
			val user = userDao.getUserByEmail(email)
			callback(user)
		}
	}

	fun isLoggedIn(): Boolean {
		return sessionManager.isLoggedIn()
	}

	fun logout() {
		sessionManager.logout()
	}

	class Factory(private val application: Application) : ViewModelProvider.Factory {
		override fun <T : ViewModel> create(modelClass: Class<T>): T {
			if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
				@Suppress("UNCHECKED_CAST")
				return UserViewModel(application) as T
			}
			throw IllegalArgumentException("Unknown ViewModel class")
		}
	}
}
