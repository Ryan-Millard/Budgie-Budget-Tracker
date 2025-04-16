package com.example.budgiebudgettracking

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import androidx.lifecycle.lifecycleScope

import com.example.budgiebudgettracking.database.AppDatabase
import com.example.budgiebudgettracking.entities.User
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
	// UI elements
	private lateinit var fullNameLayout: TextInputLayout
	private lateinit var etFullName: TextInputEditText
	private lateinit var emailLayout: TextInputLayout
	private lateinit var etEmail: TextInputEditText
	private lateinit var passwordLayout: TextInputLayout
	private lateinit var etPassword: TextInputEditText
	private lateinit var confirmPasswordLayout: TextInputLayout
	private lateinit var etConfirmPassword: TextInputEditText
	private lateinit var btnRegister: Button
	private lateinit var loginPrompt: TextView

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_register)

		// Initialize UI elements
		initializeViews()

		// Set up click listeners
		setupClickListeners()
	}

	private fun validateForm(): Boolean {
		var isValid = true

		// Validate full name
		if (TextUtils.isEmpty(etFullName.text)) {
			fullNameLayout.error = "Please enter your full name"
			isValid = false
		} else {
			fullNameLayout.error = null
		}

		// Validate email
		if (TextUtils.isEmpty(etEmail.text)) {
			emailLayout.error = "Please enter your email"
			isValid = false
		} else if (!Patterns.EMAIL_ADDRESS.matcher(etEmail.text.toString()).matches()) {
			emailLayout.error = "Please enter a valid email address"
			isValid = false
		} else {
			emailLayout.error = null
		}

		// Validate password
		if (TextUtils.isEmpty(etPassword.text)) {
			passwordLayout.error = "Please enter a password"
			isValid = false
		} else if (etPassword.text.toString().length < 8) {
			passwordLayout.error = "Password must be at least 8 characters"
			isValid = false
		} else {
			passwordLayout.error = null
		}

		// Validate confirm password
		if (TextUtils.isEmpty(etConfirmPassword.text)) {
			confirmPasswordLayout.error = "Please confirm your password"
			isValid = false
		} else if (etPassword.text.toString() != etConfirmPassword.text.toString()) {
			confirmPasswordLayout.error = "Passwords do not match"
			isValid = false
		} else {
			confirmPasswordLayout.error = null
		}

		return isValid
	}

	private fun registerUser() {
		val fullName = etFullName.text.toString().trim()
		val email = etEmail.text.toString().trim()
		val password = etPassword.text.toString()

		val user = User(fullName = fullName, email = email, password = password)

		lifecycleScope.launch {
			val userDao = AppDatabase.getDatabase(applicationContext).userDao()
			val existingUser = userDao.getUserByEmail(email)
			if (existingUser != null) {
				runOnUiThread {
					emailLayout.error = "Email already registered"
				}
			} else {
				userDao.insertUser(user)
				runOnUiThread {
					Toast.makeText(this@RegisterActivity, "Registration successful!", Toast.LENGTH_SHORT).show()
					startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
					finish()
				}
			}
		}
	}

	private fun initializeViews() {
		fullNameLayout = findViewById(R.id.fullNameLayout)
		etFullName = findViewById(R.id.etFullName)
		emailLayout = findViewById(R.id.emailLayout)
		etEmail = findViewById(R.id.etEmail)
		passwordLayout = findViewById(R.id.passwordLayout)
		etPassword = findViewById(R.id.etPassword)
		confirmPasswordLayout = findViewById(R.id.confirmPasswordLayout)
		etConfirmPassword = findViewById(R.id.etConfirmPassword)
		btnRegister = findViewById(R.id.btnRegister)
		loginPrompt = findViewById(R.id.loginPrompt)
	}

	private fun setupClickListeners() {
		// Register button click listener
		btnRegister.setOnClickListener {
			if (validateForm()) {
				// Process registration
				registerUser()
			}
		}

		// Login prompt click listener
		loginPrompt.setOnClickListener {
			// Navigate to Login Activity
			startActivity(Intent(this, LoginActivity::class.java))
			finish() // Optional: close this activity
		}
	}
}
