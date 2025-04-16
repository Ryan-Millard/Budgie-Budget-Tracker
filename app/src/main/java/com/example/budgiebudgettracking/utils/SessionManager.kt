package com.example.budgiebudgettracking.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager private constructor(context: Context) {

	companion object {
		@Volatile
		private var INSTANCE: SessionManager? = null

		fun getInstance(context: Context): SessionManager {
			return INSTANCE ?: synchronized(this) {
				INSTANCE ?: SessionManager(context.applicationContext).also { INSTANCE = it }
			}
		}

		private const val PREF_NAME = "user_session"
		private const val KEY_IS_LOGGED_IN = "is_logged_in"
		private const val KEY_LOGIN_TIME = "login_time"
		private const val SESSION_DURATION = 24 * 60 * 60 * 1000L // 24 hours in milliseconds
	}

	private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

	fun login() {
		prefs.edit().apply {
			putBoolean(KEY_IS_LOGGED_IN, true)
			putLong(KEY_LOGIN_TIME, System.currentTimeMillis())
			apply()
		}
	}

	fun logout() {
		prefs.edit().clear().apply()
	}

	fun isLoggedIn(): Boolean {
		val isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
		val loginTime = prefs.getLong(KEY_LOGIN_TIME, 0L)
		val currentTime = System.currentTimeMillis()
		return isLoggedIn && (currentTime - loginTime) < SESSION_DURATION
	}

	fun isSessionExpired(): Boolean {
		val loginTime = prefs.getLong(KEY_LOGIN_TIME, 0L)
		val currentTime = System.currentTimeMillis()
		return (currentTime - loginTime) >= SESSION_DURATION
	}
}
