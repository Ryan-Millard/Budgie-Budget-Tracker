package com.example.budgiebudgettracking

import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

import com.example.budgiebudgettracking.database.AppDatabase
import com.example.budgiebudgettracking.utils.SessionManager

class LoginActivity : AppCompatActivity() {
	// UI elements
	private lateinit var emailInputLayout: TextInputLayout
	private lateinit var emailEditText: TextInputEditText
	private lateinit var passwordInputLayout: TextInputLayout
	private lateinit var passwordEditText: TextInputEditText
	private lateinit var loginButton: Button
	private lateinit var registerPrompt: TextView

	private lateinit var sessionManager: SessionManager

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_login)

		sessionManager = SessionManager.getInstance(applicationContext)

		// Initialize UI elements
		initializeViews()

		// Set up click listeners
		setupClickListeners()
	}

	private fun initializeViews() {
		emailInputLayout = findViewById(R.id.emailInputLayout)
		emailEditText = findViewById(R.id.emailEditText)
		passwordInputLayout = findViewById(R.id.passwordInputLayout)
		passwordEditText = findViewById(R.id.passwordEditText)
		loginButton = findViewById(R.id.loginButton)
		registerPrompt = findViewById(R.id.registerPrompt)
	}

	private fun setupClickListeners() {
		// Login button click listener
		loginButton.setOnClickListener {
			if (validateForm()) {
				// Process login
				loginUser()
			}
		}

		// Register prompt click listener
		registerPrompt.setOnClickListener {
			startActivity(Intent(this, RegisterActivity::class.java))
		}
	}

	private fun validateForm(): Boolean {
		var isValid = true

		// Validate email
		if (TextUtils.isEmpty(emailEditText.text)) {
			emailInputLayout.error = "Please enter your email"
			isValid = false
		} else if (!Patterns.EMAIL_ADDRESS.matcher(emailEditText.text.toString()).matches()) {
			emailInputLayout.error = "Please enter a valid email address"
			isValid = false
		} else {
			emailInputLayout.error = null
		}

		// Validate password
		if (TextUtils.isEmpty(passwordEditText.text)) {
			passwordInputLayout.error = "Please enter your password"
			isValid = false
		} else {
			passwordInputLayout.error = null
		}

		return isValid
	}

	private fun loginUser() {
		val email = emailEditText.text.toString().trim()
		val password = passwordEditText.text.toString()

		lifecycleScope.launch { // Use lifecycleScope.launch
			val userDao = AppDatabase.getDatabase(applicationContext).userDao()
			val user = userDao.login(email, password)
			runOnUiThread {
				if(user != null) {
					sessionManager.login(email)
					Toast.makeText(this@LoginActivity, "Login successful!", Toast.LENGTH_SHORT).show()
					startActivity(Intent(this@LoginActivity, MainActivity::class.java))
					finish()
				} else {
					Toast.makeText(this@LoginActivity, "Invalid email or password", Toast.LENGTH_SHORT).show()
				}
			}
		}
	}
}
