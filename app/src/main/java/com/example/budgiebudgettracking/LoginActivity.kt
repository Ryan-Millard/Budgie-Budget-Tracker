package com.example.budgiebudgettracking

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class LoginActivity : BaseActivity() {
	// UI elements
	private lateinit var emailInputLayout: TextInputLayout
	private lateinit var emailEditText: TextInputEditText
	private lateinit var passwordInputLayout: TextInputLayout
	private lateinit var passwordEditText: TextInputEditText
	private lateinit var loginButton: Button
	private lateinit var registerPrompt: TextView

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_login)

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
			// Navigate to Register Activity using BaseActivity's method
			navigateToActivity(RegisterActivity::class.java)
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
		// Get form values
		val email = emailEditText.text.toString().trim()
		val password = passwordEditText.text.toString()

		// Here you would typically implement your authentication logic
		// For example, using Firebase Auth, a custom API, etc.

		// For demonstration purposes, let's assume login is successful
		if (email.isNotEmpty() && password.isNotEmpty()) {
			// Show success message
			Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()

			// Navigate to Dashboard or Main Activity
			val intent = Intent(this, MainActivity::class.java)
			intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
			startActivity(intent)
			finish()
		} else {
			// Show error message
			Toast.makeText(this, "Login failed. Please try again.", Toast.LENGTH_SHORT).show()
		}
	}
}
