package com.example.budgiebudgettracking

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

open class BaseActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		super.setContentView(R.layout.activity_base)
		setupBottomNavigation()
	}

	override fun setContentView(layoutResID: Int) {
		val contentFrame = findViewById<FrameLayout>(R.id.content_frame)
		LayoutInflater.from(this).inflate(layoutResID, contentFrame, true)
	}

	private fun setupBottomNavigation() {
		val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
		bottomNavigationView.setOnItemSelectedListener { item ->
			when (item.itemId) {
				R.id.nav_home -> {
					// Handle navigation
					true
				}
				R.id.nav_category -> {
					// Handle navigation
					true
				}
				R.id.nav_expense -> {
					// Handle navigation
					true
				}
				R.id.nav_budget -> {
					// Handle navigation
					true
				}
				R.id.nav_profile -> {
					// Handle navigation
					true
				}
				// Add other navigation cases
				else -> false
			}
		}
	}
}
