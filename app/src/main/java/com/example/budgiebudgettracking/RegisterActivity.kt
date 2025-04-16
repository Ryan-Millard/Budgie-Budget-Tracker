package com.example.budgiebudgettracking

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import android.content.Intent

class RegisterActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_register)

		// Find the register prompt TextView by its ID
		val loginPrompt = findViewById<TextView>(R.id.loginPrompt)

		// Set an OnClickListener on the TextView
		loginPrompt.setOnClickListener {
			startActivity(Intent(this, LoginActivity::class.java))
		}
	}
}
