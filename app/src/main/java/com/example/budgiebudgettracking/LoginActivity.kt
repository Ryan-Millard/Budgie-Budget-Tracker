package com.example.budgiebudgettracking

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import com.example.budgiebudgettracking.BaseActivity

class LoginActivity : BaseActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_login)

		// Find the register prompt TextView by its ID
		val registerPrompt = findViewById<TextView>(R.id.registerPrompt)

		// Set an OnClickListener on the TextView
		registerPrompt.setOnClickListener {
			navigateToActivity(RegisterActivity::class.java)
		}
	}
}
