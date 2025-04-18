package com.example.budgiebudgettracking.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.example.budgiebudgettracking.utils.SessionManager
import java.lang.IllegalArgumentException

class SessionViewModel(application: Application) : AndroidViewModel(application) {

	private val sessionManager = SessionManager.getInstance(application)

	private val _isLoggedIn = MutableLiveData<Boolean>()
	val isLoggedIn: LiveData<Boolean> = _isLoggedIn

	private val _isSessionExpired = MutableLiveData<Boolean>()
	val isSessionExpired: LiveData<Boolean> = _isSessionExpired

	init {
		checkLoginStatus()
	}

	fun checkLoginStatus() {
		_isLoggedIn.value = sessionManager.isLoggedIn()
		_isSessionExpired.value = sessionManager.isSessionExpired()
	}

	fun getUserEmail(): String {
		return sessionManager.getUserEmail()
	}

	fun logout() {
		sessionManager.logout()
		_isLoggedIn.value = false
	}

	class Factory(private val application: Application) : ViewModelProvider.Factory {
		override fun <T : ViewModel> create(modelClass: Class<T>): T {
			if (modelClass.isAssignableFrom(SessionViewModel::class.java)) {
				@Suppress("UNCHECKED_CAST")
				return SessionViewModel(application) as T
			}
			throw IllegalArgumentException("Unknown ViewModel class")
		}
	}
}
