package com.example.budgiebudgettracking

import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.content.Intent

import com.example.budgiebudgettracking.BaseActivity

class ExpenseActivity : BaseActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_expense)

		setupClickListeners()
	}

	private fun setupClickListeners() {
		val addExpenseFab = findViewById<FloatingActionButton>(R.id.addExpenseFab)
		addExpenseFab.setOnClickListener {
			startActivity(Intent(this, ExpenseCalculatorActivity::class.java))
		}
	}
}
