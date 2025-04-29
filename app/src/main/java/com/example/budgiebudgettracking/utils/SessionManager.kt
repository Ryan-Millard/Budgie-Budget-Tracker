package com.example.budgiebudgettracking.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager private constructor(context: Context) {

	private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

	fun setUserId(id: Int) {
		prefs.edit().putInt("KEY_USER_ID", id).apply()
	}

	fun getUserId(): Int = prefs.getInt("KEY_USER_ID", 0)

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
		private const val KEY_USER_EMAIL = "user_email"
		private const val SESSION_DURATION = 24 * 60 * 60 * 1000L // 24 hours in milliseconds
	}
}

