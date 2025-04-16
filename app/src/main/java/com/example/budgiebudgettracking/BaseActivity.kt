package com.example.budgiebudgettracking

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

open class BaseActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		super.setContentView(R.layout.activity_base)
		setupBottomNavigation()

		// Set the correct selected item based on current activity
		updateSelectedNavigationItem()
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
					if (this !is MainActivity) {
						navigateToActivity(MainActivity::class.java)
					}
					true
				}
				R.id.nav_category -> {
					if (this !is CategoryActivity) {
						navigateToActivity(CategoryActivity::class.java)
					}
					true
				}
				R.id.nav_expense -> {
					if (this !is ExpenseActivity) {
						navigateToActivity(ExpenseActivity::class.java)
					}
					true
				}
				R.id.nav_budget -> {
					if (this !is MonthlyBudgetActivity) {
						navigateToActivity(MonthlyBudgetActivity::class.java)
					}
					true
				}
				R.id.nav_profile -> {
					if (this !is ProfileActivity) {
						navigateToActivity(ProfileActivity::class.java)
					}
					true
				}
				else -> false
			}
		}
	}

	/**
	 * Navigate to a new activity without transition animations
	 */
	protected fun <T : AppCompatActivity> navigateToActivity(activityClass: Class<T>) {
		val intent = Intent(this, activityClass)
		val options = ActivityOptionsCompat.makeCustomAnimation(this, 0, 0)
		startActivity(intent, options.toBundle())
	}

	private fun updateSelectedNavigationItem() {
		val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

		// Select the appropriate menu item based on the current activity
		val selectedItemId = when (this) {
			is MainActivity -> R.id.nav_home
			is CategoryActivity -> R.id.nav_category
			is ExpenseActivity -> R.id.nav_expense
			is MonthlyBudgetActivity -> R.id.nav_budget
			is ProfileActivity -> R.id.nav_profile
			else -> R.id.nav_home
		}

		// Set the selected item without triggering the listener
		bottomNavigationView.selectedItemId = selectedItemId
	}

	// Add this method to prevent activity recreation when selecting the current tab
	override fun onNewIntent(intent: Intent?) {
		super.onNewIntent(intent)
		setIntent(intent)
	}
}
