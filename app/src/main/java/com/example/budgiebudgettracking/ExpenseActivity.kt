package com.example.budgiebudgettracking

import android.os.Bundle
import android.view.Gravity

import com.example.budgiebudgettracking.BaseActivity

class ExpenseActivity : BaseActivity(), FloatingActionButtonHandler {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_expense)

		createAndAttachFab(destination = ExpenseCalculatorActivity::class.java)
	}
}
