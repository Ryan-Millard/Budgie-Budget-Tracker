package com.example.budgiebudgettracking

import android.widget.Toast
import android.content.Intent
import android.os.Bundle
import android.widget.Button

import com.example.budgiebudgettracking.utils.SessionManager

class ProfileActivity : BaseActivity() {
	private lateinit var sessionManager: SessionManager

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_profile)

		sessionManager = SessionManager.getInstance(applicationContext)

		// Launch EditProfileActivity when edit button is clicked
		val btnLogout = findViewById<Button>(R.id.btnLogout)
		btnLogout.setOnClickListener {
			sessionManager.logout()
			Toast.makeText(this@ProfileActivity, "Logged out successfully!", Toast.LENGTH_SHORT).show()
			startActivity(Intent(this, LoginActivity::class.java))
		}
	}
}
